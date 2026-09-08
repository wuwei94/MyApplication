import 'dart:async';

/// 动态自适应打字机流控引擎（TypewriterEngine）
///
/// 对标 Android module_markdown 的 `TypewriterEngine`（Kotlin 协程实现），
/// 在 Dart 单线程事件循环下保持一致的业务逻辑：
///
/// 核心机制：
/// 1. 动态时钟与缓冲区自适应调速（Fluid Adaptive Rate）：
///    - 当网络突发一大段文字（缓冲区积压 > 80 字符）时，自动提速（20ms / 2字追赶）；
///    - 当缓冲区积压中等（30~80 字符）时，适度加速（22ms / 1字紧跟推流）；
///    - 当缓冲区平缓（< 30 字符）时，轻快出字（30ms / 1字 呼吸感）；
/// 2. 标点呼吸停顿节奏：
///    - 句末重标点（句号 / 感叹号 / 问号 / 换行）停顿 160ms；
///    - 句中轻标点（逗号 / 顿号 / 分号 / 冒号）停顿 80ms；
///    - 积压 > 50 字符时只取 1/3 停顿，避免拖慢追赶节奏；
/// 3. 完善的生命周期控制：
///    - 支持即时 feed 追加、complete 结束通知、pause 暂停、resume 恢复、
///      skipToFinish 一键秒出全部以及 reset 重置。
enum TypewriterState { idle, typing, paused, completed }

class TypewriterEngine {
  /// 文本更新监听（text：当前已出字全文，isFinished：是否已全部输出完成）
  void Function(String text, bool isFinished)? onTextUpdate;

  /// 流控性能指标监听（积压量、当前单字耗时、引擎状态）
  void Function(int backlog, int speedMs, TypewriterState state)? onMetrics;

  final StringBuffer _pendingBuffer = StringBuffer();
  final StringBuffer _outputBuffer = StringBuffer();

  bool _feedCompleted = false;
  TypewriterState _state = TypewriterState.idle;

  /// 循环令牌：每次 start / reset / skipToFinish 自增，
  /// 用于让旧调度循环在下一次检查点安全退出，避免竞态。
  int _runId = 0;

  bool get isTyping => _state == TypewriterState.typing;

  /// 启动打字机调度循环（清空上一轮状态）
  void start() {
    reset();
    _state = TypewriterState.typing;
    final int runId = ++_runId;
    unawaited(_runLoop(runId));
  }

  /// 接收网络推流 chunk
  void feed(String chunk) {
    if (chunk.isEmpty) return;
    _pendingBuffer.write(chunk);
    if (_state == TypewriterState.idle) {
      _state = TypewriterState.typing;
    }
  }

  /// 标记网络推流已全部到达
  void complete() {
    _feedCompleted = true;
  }

  /// 暂停打字
  void pause() {
    if (_state == TypewriterState.typing) {
      _state = TypewriterState.paused;
    }
  }

  /// 恢复打字
  void resume() {
    if (_state == TypewriterState.paused) {
      _state = TypewriterState.typing;
    }
  }

  /// 一键跳过打字过程，立即将所有积压文字全部显示并标记完成
  void skipToFinish() {
    // 让旧调度循环退出
    _runId++;
    _outputBuffer.write(_pendingBuffer);
    _pendingBuffer.clear();
    _feedCompleted = true;
    _state = TypewriterState.completed;
    final String fullText = _outputBuffer.toString();
    onTextUpdate?.call(fullText, true);
    onMetrics?.call(0, 0, TypewriterState.completed);
  }

  /// 重置清空打字机引擎状态
  void reset() {
    // 让旧调度循环退出
    _runId++;
    _pendingBuffer.clear();
    _outputBuffer.clear();
    _feedCompleted = false;
    _state = TypewriterState.idle;
    onMetrics?.call(0, 0, TypewriterState.idle);
  }

  /// 核心调度循环
  Future<void> _runLoop(int runId) async {
    while (_runId == runId) {
      String? textToEmit;
      bool isFinished = false;
      int currentDelayMs = 30;
      TypewriterState currentState = _state;

      final int currentBacklog = _pendingBuffer.length;

      if (currentState == TypewriterState.paused) {
        // 暂停状态，静默等待
        currentDelayMs = 50;
      } else if (_pendingBuffer.isEmpty) {
        if (_feedCompleted) {
          _state = TypewriterState.completed;
          currentState = TypewriterState.completed;
          isFinished = true;
          textToEmit = _outputBuffer.toString();
        } else {
          currentDelayMs = 20;
        }
      } else {
        // 自适应出字算法（轻快自然：约 33~45 字/秒）
        int step;
        if (currentBacklog > 80) {
          step = 2;
          currentDelayMs = 20; // 积压追赶模式：约 100 字/秒
        } else if (currentBacklog > 30) {
          step = 1;
          currentDelayMs = 22; // 适度加速：约 45 字/秒
        } else {
          step = 1;
          currentDelayMs = 30; // 基础轻快出字：约 33 字/秒
        }

        // 取出 step 个字符（single-pass：先快照再裁剪写回）
        final String snapshot = _pendingBuffer.toString();
        final int actualStep = step < snapshot.length ? step : snapshot.length;
        final String chunk = snapshot.substring(0, actualStep);
        _pendingBuffer
          ..clear()
          ..write(snapshot.substring(actualStep));
        _outputBuffer.write(chunk);

        // 检查最后一个字符是否为标点，按级别增加呼吸停顿
        if (chunk.isNotEmpty) {
          final int pauseMs = _punctuationPause(chunk[chunk.length - 1]);
          if (pauseMs > 0) {
            currentDelayMs = currentBacklog > 50
                ? currentDelayMs + pauseMs ~/ 3
                : pauseMs;
          }
        }

        textToEmit = _outputBuffer.toString();
      }

      // 派发回调
      if (textToEmit != null) {
        onTextUpdate?.call(textToEmit, isFinished);
      }
      onMetrics?.call(currentBacklog, currentDelayMs, currentState);

      if (isFinished) {
        break;
      }

      await Future<void>.delayed(Duration(milliseconds: currentDelayMs));
    }
  }

  /// 根据标点符号级别计算呼吸停顿毫秒数：
  /// - 句末重标点（句号、感叹号、问号、换行）：160ms
  /// - 句中轻标点（逗号、顿号、分号、冒号）：80ms
  int _punctuationPause(String char) {
    const String heavy = '。！？.!?\n';
    const String light = '，、；：,;:';
    if (heavy.contains(char)) return 160;
    if (light.contains(char)) return 80;
    return 0;
  }
}
