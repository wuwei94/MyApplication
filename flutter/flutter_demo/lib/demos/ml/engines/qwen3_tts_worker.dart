/// Qwen3-TTS 的后台合成 isolate。
///
/// ### 为什么必须单开 isolate
///
/// 本页是三个语音合成页里最重的一个：四张图合计约 940 MB、KV cache 约 470 MB，
/// 一次合成要在宿主循环里跑几百步 decode + MTP + 分块 codec。整个过程是
/// **同步阻塞**的，放进 UI 线程会让页面在数十秒里完全无响应 —— 而 Android 侧
/// 明确保留了可点的「停止」按钮（虽然前向不可中断，但界面要能立刻响应并丢弃结果）。
///
/// Android 靠「引擎串行队列 + 协程」把推理挪出主线程，Flutter 没有等价的
/// 后台串行队列，isolate 是唯一对等物：图与表的构建、常驻、逐帧合成、关闭
/// 全部发生在 worker 内，主 isolate 只收发消息。
///
/// ### 消息协议
///
/// ```
/// 主 -> worker（command port）:
///   ['synth', String text, String language, bool greedy, int? seed, SendPort reply]
///   ['close']
/// worker -> 主（main port）:
///   ['command-port', SendPort]
///   ['ready', int tokenizerSize, String graphNames, int loadMs, List<String> mismatches]
///   ['progress', int frames]
///   ['error', String message]      // 构建失败，worker 直接退场
///   ['closed']
/// worker -> 主（reply port）:
///   ['audio', Float32List audio, int frames, int prefillMs, int talkerMs, int mtpMs, int codecMs]
///   ['error', String message]      // 单次合成失败，worker 继续存活
/// ```
library;

import 'dart:async';
import 'dart:io';
import 'dart:isolate';
import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/qwen3_tts_engine.dart';

/// worker 启动参数
///
/// 分词器参考向量（`assets/ml/tokenizer_test_vectors.json`）由主 isolate 读好后
/// 一并传入 —— 后台 isolate 里 `rootBundle` 不可用（没有平台信使），
/// 而这份向量只有 1 KB，随 spawn 参数走最省事。
typedef QwenWorkerInit = ({
  SendPort mainPort,
  String modelsDir,
  List<List<Object>> vectors,
});

/// 一次合成结果（经 isolate 边界传回主 isolate）
typedef QwenSynthResult = ({
  Float32List audio,
  int frames,
  int prefillMs,
  int talkerMs,
  int mtpMs,
  int codecMs,
});

/// 引擎自检结果
typedef QwenEngineInfo = ({
  int tokenizerSize,
  String graphNames,
  int loadMs,
  List<String> mismatches,
});

/// worker isolate 入口：常驻四张图，按请求合成后把 PCM 回传
Future<void> qwenTtsWorkerEntry(QwenWorkerInit init) async {
  final SendPort mainPort = init.mainPort;
  final ReceivePort commands = ReceivePort();
  mainPort.send(<Object>['command-port', commands.sendPort]);

  final Qwen3TtsEngine engine;
  try {
    final Stopwatch stopwatch = Stopwatch()..start();
    engine = Qwen3TtsEngine.open(Directory(init.modelsDir));
    // 自检与参考向量比对在同一套图上跑；只读文件，不改模型状态
    final List<String> mismatches = engine.findTokenizerMismatches(
      <({String text, Int32List ids})>[
        for (final List<Object> vector in init.vectors)
          (text: vector[0] as String, ids: vector[1] as Int32List),
      ],
    );
    mainPort.send(<Object>[
      'ready',
      engine.tokenizerSize,
      engine.graphNames,
      stopwatch.elapsedMilliseconds,
      mismatches,
    ]);
  } catch (error) {
    // 构建失败：如实把原因带回主 isolate，worker 直接退场
    mainPort.send(<Object>['error', '$error']);
    commands.close();
    return;
  }

  try {
    bool closing = false;
    await for (final Object? message in commands) {
      if (message is! List<Object> || message.isEmpty) continue;
      final String command = message[0] as String;
      if (command == 'close') {
        closing = true;
        break;
      }
      if (command != 'synth' || message.length < 6) continue;

      final SendPort reply = message[5] as SendPort;
      try {
        final Qwen3TtsResult result = engine.synthesize(
          message[1] as String,
          language: message[2] as String,
          greedy: message[3] as bool,
          seed: message[4] as int?,
          // 回调在 worker 线程（本 isolate）上，直接发消息给主 isolate
          onFrame: (int frames) => mainPort.send(<Object>['progress', frames]),
        );
        reply.send(<Object>[
          'audio',
          result.audio,
          result.frames,
          result.prefillMs,
          result.talkerMs,
          result.mtpMs,
          result.codecMs,
        ]);
      } catch (error) {
        // 单次失败不拖垮 worker：把原因回报，之后仍可再合成
        reply.send(<Object>['error', '$error']);
      }
    }
    if (closing) {
      // 先回收原生实例再通知，避免主 isolate 拿到 'closed' 就 kill 时
      // 解释器还没关干净（句柄与 mmap 是进程级资源）
      engine.close();
      commands.close();
      mainPort.send(<Object>['closed']);
    }
  } catch (error) {
    mainPort.send(<Object>['error', '$error']);
  }
}

/// 主 isolate 侧的 Qwen3-TTS 客户端
///
/// 生命周期：页面初始化时 [start]（异步等 worker 就绪），退出时 [close]。
/// [synthesize] 每次请求新建 reply port、收到回复即弃（端口是一次性资源，
/// 泄漏会累积到进程结束）；逐帧进度走常驻的 main port 分发。
class QwenTtsWorkerClient {
  QwenTtsWorkerClient({required this.modelsDir, required this.vectors});

  /// 模型目录绝对路径（页面初始化时经 [MlModelStore] 解析好再传进来）
  final String modelsDir;

  /// 分词器参考向量，随 spawn 参数传入 worker
  final List<List<Object>> vectors;

  Isolate? _isolate;
  SendPort? _commandPort;
  ReceivePort? _lifecyclePort;

  /// 引擎自检信息（worker 就绪后可得）
  QwenEngineInfo? info;

  /// 当前合成的逐帧进度回调；由 [synthesize] 在调用期间挂上
  void Function(int frames)? _onProgress;

  bool get isReady => _commandPort != null;

  /// 启动 worker 并等到引擎就绪；失败时抛出（消息来自 worker 的 'error'）
  Future<void> start() async {
    if (_isolate != null) return;
    final ReceivePort lifecycle = ReceivePort();
    _lifecyclePort = lifecycle;
    // 生命周期端口要服务多轮监听（就绪等待、进度分发、关闭等待），
    // ReceivePort 是单订阅流，必须转成广播流才能重复 listen
    final Stream<Object?> events = lifecycle.asBroadcastStream();
    final Isolate isolate = await Isolate.spawn<QwenWorkerInit>(
      qwenTtsWorkerEntry,
      (
        mainPort: lifecycle.sendPort,
        modelsDir: modelsDir,
        vectors: vectors,
      ),
      debugName: 'qwen3-tts-worker',
    );
    _isolate = isolate;

    final Completer<void> ready = Completer<void>();
    SendPort? commandPort;
    // 不随 ready 取消订阅：'progress' 与 'closed' 还要走同一条流。
    // 订阅句柄不保存 —— 端口在 [_teardown] 里关闭，流随之结束、订阅自动完成
    events.listen((Object? message) {
      if (message is! List<Object> || message.isEmpty) return;
      switch (message[0] as String) {
        case 'command-port':
          commandPort = message[1] as SendPort;
        case 'ready':
          info = (
            tokenizerSize: message[1] as int,
            graphNames: message[2] as String,
            loadMs: message[3] as int,
            mismatches: (message[4] as List<Object?>).cast<String>(),
          );
          _commandPort = commandPort;
          if (!ready.isCompleted) {
            ready.complete();
          }
        case 'progress':
          _onProgress?.call(message[1] as int);
        case 'error':
          if (!ready.isCompleted) {
            ready.completeError(StateError('Qwen3-TTS worker 构建失败: ${message[1]}'));
          }
        default:
          break;
      }
    });

    try {
      // 兜底：worker 若起不来（OOM、平台限制），不能让初始化永远挂住。
      // 四张图约 940 MB，构建本身就要数十秒，超时给宽一些
      await ready.future.timeout(const Duration(seconds: 300));
    } catch (error) {
      await _teardown();
      rethrow;
    }
  }

  /// 合成一段文本（在 worker isolate 上执行，不阻塞 UI 线程）
  ///
  /// [onProgress] 在合成期间随逐帧进度被调用（**在主 isolate 上**）。
  Future<QwenSynthResult> synthesize(
    String text, {
    required String language,
    bool greedy = false,
    int? seed,
    void Function(int frames)? onProgress,
  }) async {
    final SendPort? commandPort = _commandPort;
    if (commandPort == null) {
      throw StateError('Qwen3-TTS worker 尚未就绪');
    }
    final ReceivePort reply = ReceivePort();
    _onProgress = onProgress;
    try {
      commandPort.send(<Object?>[
        'synth',
        text,
        language,
        greedy,
        seed,
        reply.sendPort,
      ]);
      // 兜底：几十秒到几分钟的合成，超时给足；worker 被系统杀死时不能让页面永远停住
      final Object? message =
          await reply.first.timeout(const Duration(minutes: 15));
      if (message is! List<Object> || message.isEmpty) {
        throw StateError('Qwen3-TTS worker 返回了无法识别的消息');
      }
      switch (message[0] as String) {
        case 'audio':
          return (
            audio: message[1] as Float32List,
            frames: message[2] as int,
            prefillMs: message[3] as int,
            talkerMs: message[4] as int,
            mtpMs: message[5] as int,
            codecMs: message[6] as int,
          );
        case 'error':
          throw StateError(message[1] as String);
        default:
          throw StateError('Qwen3-TTS worker 返回了无法识别的消息');
      }
    } finally {
      _onProgress = null;
      reply.close();
    }
  }

  /// 关闭 worker：先请它自行回收原生实例，超时再强杀
  Future<void> close() async {
    final Isolate? isolate = _isolate;
    final SendPort? commandPort = _commandPort;
    final ReceivePort? lifecycle = _lifecyclePort;
    if (isolate == null) return;

    final Stream<Object?>? events = lifecycle?.asBroadcastStream();
    if (commandPort != null && events != null) {
      try {
        commandPort.send(<Object>['close']);
        // 等 worker 报 'closed'；它先 close() 引擎再发通知，因此超时要给够
        // （引擎关闭要释放约 1.4 GB 的映射与缓冲区）
        await events
            .firstWhere((Object? message) =>
                message is List<Object> &&
                message.isNotEmpty &&
                message[0] == 'closed')
            .timeout(const Duration(seconds: 60));
      } on Object catch (_) {
        // 超时或端口已关：强杀兜底。worker 是纯计算 isolate，杀掉不丢用户数据
        isolate.kill(priority: Isolate.immediate);
      }
    } else {
      isolate.kill(priority: Isolate.immediate);
    }
    await _teardown();
  }

  Future<void> _teardown() async {
    _isolate = null;
    _commandPort = null;
    _onProgress = null;
    _lifecyclePort?.close();
    _lifecyclePort = null;
  }
}
