/// KittenTTS 的后台合成 isolate —— 与 Android 页的 producer 协程对应。
///
/// ### 为什么单开一个 isolate
///
/// 合成是**同步阻塞**调用（三张图依次 invoke，短句数百毫秒、长句数秒）。若跑在
/// UI 线程，页面会在单句合成期间完全无法响应——而「流式」页的全部意义就在于
/// 边播边合成、随时可停。Android 侧靠 producer/消费者一对协程（合成跑在引擎
/// 串行队列上）实现，Flutter 没有等价的「后台串行队列」，但 isolate 有：
/// 整条引擎构建、常驻、关闭都发生在 worker 内，主 isolate 只收发消息，
/// UI 线程全程自由。
///
/// 解释器句柄**不能**跨 isolate 传递（原生指针），因此 worker 里自建引擎；
/// 音素化不进来（见 [KittenWorkerClient]），worker 只吃符号 id。
///
/// ### 消息协议
///
/// ```
/// 主 -> worker（command port）:
///   ['synth', List<int> symbolIds, int voice, int textLength, SendPort reply]
///   ['close']
/// worker -> 主（reply port，随请求携带）:
///   ['audio', Float32List audio, int tokens, int frames, int synthMs]
///   ['error', String message]
/// worker -> 主（main port，仅生命周期事件）:
///   ['command-port', SendPort]
///   ['ready', int modelBytes]
///   ['error', String message]      // 构建失败
///   ['closed']
/// ```
library;

import 'dart:async';
import 'dart:isolate';
import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/kitten_synthesizer.dart';

/// worker 启动参数
typedef KittenWorkerInit = ({SendPort mainPort, String modelsDir});

/// 一次合成结果（经 isolate 边界传回主 isolate）
typedef KittenSynthResult = ({
  Float32List audio,
  int tokens,
  int frames,
  int synthMs,
});

/// worker isolate 入口：常驻 Kitten 三张图，按句合成后把 PCM 回传
Future<void> kittenWorkerEntry(KittenWorkerInit init) async {
  final SendPort mainPort = init.mainPort;
  final ReceivePort commands = ReceivePort();
  mainPort.send(<Object>['command-port', commands.sendPort]);

  KittenSynthesizer? synthesizer;
  try {
    synthesizer = KittenSynthesizer(modelsDir: init.modelsDir);
    await synthesizer.load();
    mainPort.send(<Object>['ready', synthesizer.modelBytes]);
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
      if (command != 'synth' || message.length < 5) continue;

      final SendPort reply = message[4] as SendPort;
      try {
        final List<int> symbolIds = (message[1] as List<Object?>).cast<int>();
        final int voice = message[2] as int;
        final int textLength = message[3] as int;
        final KittenSynthResult result = synthesizer.synthesize(
          symbolIds,
          voice: voice,
          textLength: textLength,
        );
        reply.send(<Object>[
          'audio',
          result.audio,
          result.tokens,
          result.frames,
          result.synthMs,
        ]);
      } catch (error) {
        // 单句失败不应拖垮 worker：把原因回报，后续句子仍可继续
        reply.send(<Object>['error', '$error']);
      }
    }
    if (closing) {
      // 先回收原生实例再通知，避免主 isolate 拿到 'closed' 就 kill 时
      // 解释器还没关干净（句柄与 mmap 是进程级资源）
      synthesizer.close();
      commands.close();
      mainPort.send(<Object>['closed']);
    }
  } catch (error) {
    mainPort.send(<Object>['error', '$error']);
  }
}

/// 主 isolate 侧的 Kitten 合成客户端。
///
/// 生命周期：页面初始化时 [start]（异步等 worker 就绪），退出时 [close]。
/// [synthesize] 每句一次请求，内部新建 reply port、收到回复即弃（端口是
/// 一次性资源，泄漏会累积到进程结束）。
class KittenWorkerClient {
  KittenWorkerClient({required this.modelsDir});

  /// 模型目录绝对路径（页面初始化时经 [MlModelStore] 解析好再传进来）
  final String modelsDir;

  Isolate? _isolate;
  SendPort? _commandPort;
  ReceivePort? _lifecyclePort;

  /// 是否已就绪
  bool get isReady => _commandPort != null;

  /// 三张图在磁盘上的总字节数（worker 就绪后可得）
  int modelBytes = 0;

  /// 启动 worker 并等到引擎就绪；失败时抛出（消息来自 worker 的 'error'）
  Future<void> start() async {
    if (_isolate != null) return;
    final ReceivePort lifecycle = ReceivePort();
    _lifecyclePort = lifecycle;
    // 生命周期端口要先后服务两轮监听（就绪等待 + 关闭等待），
    // ReceivePort 是单订阅流，必须转成广播流才能重复 listen
    final Stream<Object?> events = lifecycle.asBroadcastStream();
    final Isolate isolate = await Isolate.spawn<KittenWorkerInit>(
      kittenWorkerEntry,
      (mainPort: lifecycle.sendPort, modelsDir: modelsDir),
      debugName: 'kitten-tts-worker',
    );
    _isolate = isolate;

    final Completer<void> ready = Completer<void>();
    SendPort? commandPort;
    // 不随 ready 取消订阅：'closed' 还要走同一条流
    events.listen((Object? message) {
      if (ready.isCompleted) return;
      if (message is! List<Object> || message.isEmpty) return;
      switch (message[0] as String) {
        case 'command-port':
          commandPort = message[1] as SendPort;
        case 'ready':
          modelBytes = message[1] as int;
          _commandPort = commandPort;
          ready.complete();
        case 'error':
          ready.completeError(StateError('Kitten worker 构建失败: ${message[1]}'));
        default:
          break;
      }
    });

    try {
      // 兜底：worker 若起不来（OOM、平台限制），不能让初始化永远挂住
      await ready.future.timeout(const Duration(seconds: 120));
    } catch (error) {
      isolate.kill(priority: Isolate.immediate);
      lifecycle.close();
      _isolate = null;
      _lifecyclePort = null;
      rethrow;
    }
  }

  /// 合成一句（在 worker isolate 上执行，不阻塞 UI 线程）
  ///
  /// [textLength] 是原句的字符数，用于挑选音色表的行（长度条件化音色）。
  Future<KittenSynthResult> synthesize(
    List<int> symbolIds, {
    required int voice,
    required int textLength,
  }) async {
    final SendPort? commandPort = _commandPort;
    if (commandPort == null) {
      throw StateError('Kitten worker 尚未就绪');
    }
    final ReceivePort reply = ReceivePort();
    try {
      commandPort.send(<Object>[
        'synth',
        symbolIds,
        voice,
        textLength,
        reply.sendPort,
      ]);
      final Object? message = await reply.first
          // 兜底：worker 若被系统杀死（OOM 等），不能让页面永远停在「合成中」
          .timeout(const Duration(seconds: 180));
      if (message is! List<Object> || message.isEmpty) {
        throw StateError('Kitten worker 返回了无法识别的消息');
      }
      switch (message[0] as String) {
        case 'audio':
          return (
            audio: message[1] as Float32List,
            tokens: message[2] as int,
            frames: message[3] as int,
            synthMs: message[4] as int,
          );
        case 'error':
          throw StateError(message[1] as String);
        default:
          throw StateError('Kitten worker 返回了无法识别的消息');
      }
    } finally {
      reply.close();
    }
  }

  /// 关闭 worker：先请它自行回收原生实例，超时再强杀
  Future<void> close() async {
    final Isolate? isolate = _isolate;
    final SendPort? commandPort = _commandPort;
    final ReceivePort? lifecycle = _lifecyclePort;
    _isolate = null;
    _commandPort = null;
    _lifecyclePort = null;
    if (isolate == null) return;

    if (commandPort != null && lifecycle != null) {
      final Stream<Object?> events = lifecycle.asBroadcastStream();
      try {
        commandPort.send(<Object>['close']);
        // 等 worker 报 'closed'；它先 close() 解释器再发通知，因此超时要给够
        await events
            .firstWhere((Object? message) =>
                message is List<Object> && message.isNotEmpty && message[0] == 'closed')
            .timeout(const Duration(seconds: 10));
      } on Object catch (_) {
        // 超时或端口已关：强杀兜底。worker 是纯计算 isolate，杀掉不丢用户数据
        isolate.kill(priority: Isolate.immediate);
      }
    } else {
      isolate.kill(priority: Isolate.immediate);
    }
    lifecycle?.close();
  }
}
