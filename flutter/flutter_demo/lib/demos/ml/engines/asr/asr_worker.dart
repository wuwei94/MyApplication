/// ASR 的后台推理 isolate —— 与 Android 页的「引擎串行队列 + 三级流水协程」对应。
///
/// ### 为什么单开一个 isolate
///
/// 一次窗口识别 = encoder 前向 + 最多几百步自回归解码，**同步阻塞**（音量大时
/// 单窗口就要数百毫秒到数秒）。跑在 UI 线程上，页面在整段转写期间完全不可交互。
/// Android 靠「引擎串行队列 + 三条协程」把推理挪出主线程，Flutter 对应的手段是
/// isolate：引擎构建、常驻、逐步解码、释放全部发生在 worker 内，主 isolate 只收发
/// 消息，UI 线程全程自由。
///
/// 解释器句柄**不能**跨 isolate 传递（原生指针），因此 worker 里自建整条引擎；
/// 音频采集（麦克风）必须留在主 isolate（平台通道），所以是**主 isolate 推 PCM、
/// worker 切窗口**的分工 —— 与 Android「麦克风源自己拉数据」方向相反，但块边界一致。
///
/// ### 三级流水落到哪里
///
/// Android 的 `transcribeAudio` 用两条 `Channel` 串起三条协程
/// （预处理 → 识别 → 后处理 + 刷 UI）。这里三级都留在 worker 内**顺序**执行：
/// 单窗口的预处理（FFT）与识别本来就串行，拆成三条流只会多出跨 isolate 的往返延迟；
/// 唯一需要跨出去的只有「结果文本」，一并走 `transcript` 消息。
///
/// ### 消息协议
///
/// ```
/// 主 -> worker（command port）:
///   ['load', String catalogJson, String modelKey, String modelsDir, bool gpu,
///            SendPort progress, SendPort reply]
///   ['transcribe-file', String wavPath, int intervalMs, int overlapMs, int maxDistance, SendPort reply]
///   ['mic-start', int intervalMs, int overlapMs, int maxDistance, SendPort reply]
///   ['mic-chunk', Int16List samples]
///   ['mic-end', SendPort reply]
///   ['close']
/// worker -> 主（reply port，随请求携带）:
///   ['loaded', int vocabSize, String backend, int windowMs]
///   ['started'] / ['done']
///   ['error', String message]
/// worker -> 主（progress port，load 时携带一次）:
///   ['transcript', String confirmed, String unconfirmed]
/// worker -> 主（main port，仅生命周期事件）:
///   ['command-port', SendPort]
///   ['closed']
/// ```
library;

import 'dart:async';
import 'dart:io';
import 'dart:isolate';
import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/asr/asr_audio_source.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_decoder.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_engine.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_merger.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_model_catalog.dart';

/// worker 启动参数
typedef AsrWorkerInit = ({SendPort mainPort});

/// 引擎装载结果（经 isolate 边界传回主 isolate）
typedef AsrEngineInfo = ({
  int vocabSize,
  String backend,
  int windowMs,
});

/// worker isolate 入口
Future<void> asrWorkerEntry(AsrWorkerInit init) async {
  final SendPort mainPort = init.mainPort;
  final ReceivePort commands = ReceivePort();
  mainPort.send(<Object>['command-port', commands.sendPort]);

  final _AsrWorkerSession session = _AsrWorkerSession();
  try {
    await for (final Object? message in commands) {
      if (message is! List<Object> || message.isEmpty) {
        continue;
      }
      final String command = message[0] as String;
      if (command == 'close') {
        break;
      }
      session.handle(command, message);
    }
    // 先回收原生实例再通知，避免主 isolate 拿到 'closed' 就 kill 时
    // 解释器还没关干净（句柄与 mmap 是进程级资源）
    session.dispose();
    commands.close();
    mainPort.send(<Object>['closed']);
  } catch (error) {
    session.dispose();
    mainPort.send(<Object>['error', '$error']);
  }
}

/// 一个 worker 内的会话：常驻引擎 + 当前音频源 + 当前后处理器
class _AsrWorkerSession {
  AsrEngine? _engine;
  AsrAudioSource? _source;
  AsrLevenshteinMerger? _merger;
  SendPort? _progress;

  /// 跨窗口累计的「已确认」文本（对应 Android 页的 `totalConfirmedText`）
  String _totalConfirmed = '';

  /// 最近一次回报的「未确认」文本，收尾时用来把它转正
  String _lastUnconfirmed = '';

  void handle(String command, List<Object> message) {
    switch (command) {
      case 'load':
        _load(message);
      case 'transcribe-file':
        _transcribeFile(message);
      case 'mic-start':
        _micStart(message);
      case 'mic-chunk':
        _micChunk(message);
      case 'mic-end':
        _micEnd(message);
      default:
        break;
    }
  }

  /// 装载（或换代）引擎；旧引擎就地释放
  void _load(List<Object> message) {
    final SendPort reply = message[6] as SendPort;
    try {
      final String catalogJson = message[1] as String;
      final String modelKey = message[2] as String;
      final String modelsDir = message[3] as String;
      final bool gpu = message[4] as bool;
      _progress = message[5] as SendPort;

      _stopSource();
      final AsrEngine? previous = _engine;
      _engine = null;
      previous?.close();

      final AsrModelCatalog catalog = AsrModelCatalog.fromJson(catalogJson);
      final AsrModelConfig config = catalog.configOf(modelKey);
      final AsrEngine engine = AsrEngine.open(
        modelFile: File('$modelsDir/${config.modelFileName}'),
        tokenizerFile: File('$modelsDir/${config.tokenizerFileName}'),
        config: config,
        useGpu: gpu,
      );
      _engine = engine;
      reply.send(<Object>[
        'loaded',
        engine.tokenizer.vocabSize,
        engine.backendLabel,
        config.inputMilliseconds,
      ]);
    } catch (error) {
      reply.send(<Object>['error', '$error']);
    }
  }

  /// 转写一个 16 kHz 单声道 16-bit PCM 的 WAV 文件（整段一次跑完）
  void _transcribeFile(List<Object> message) {
    final SendPort reply = message[5] as SendPort;
    final AsrEngine? engine = _engine;
    if (engine == null) {
      reply.send(<Object>['error', '引擎尚未就绪']);
      return;
    }
    try {
      final String path = message[1] as String;
      final int intervalMs = message[2] as int;
      final int overlapMs = message[3] as int;
      final int maxDistance = message[4] as int;

      _stopSource();
      _totalConfirmed = '';
      final AsrWavFileSource source = AsrWavFileSource.open(
        File(path),
        samplingRate: AsrEngine.sampleRate,
        intervalMilliseconds: intervalMs,
        overlapMilliseconds: overlapMs,
      );
      _merger = AsrLevenshteinMerger(
        engine.tokenizer,
        overlapMs / intervalMs,
        maxLevenshteinDistance: maxDistance,
      );
      try {
        _drain(source, engine);
        _finish();
        reply.send(<Object>['done']);
      } finally {
        source.close();
        _source = null;
        _merger = null;
      }
    } catch (error) {
      reply.send(<Object>['error', '$error']);
    }
  }

  /// 开始实时录音转写：窗口切分留在 worker，PCM 由主 isolate 推过来
  ///
  /// [intervalMs] / [overlapMs] 由调用方算好 —— 官方是
  /// `interval = min(5 s, 模型窗口)`、`overlap = interval - 1 s`，
  /// 前者需要模型窗口（[AsrEngineInfo.windowMs]），只有主 isolate 那侧拿得到。
  void _micStart(List<Object> message) {
    final SendPort reply = message[4] as SendPort;
    final AsrEngine? engine = _engine;
    if (engine == null) {
      reply.send(<Object>['error', '引擎尚未就绪']);
      return;
    }
    try {
      final int intervalMs = message[1] as int;
      final int overlapMs = message[2] as int;
      final int maxDistance = message[3] as int;

      _stopSource();
      _totalConfirmed = '';
      final AsrMicrophoneWindowSource source = AsrMicrophoneWindowSource(
        samplingRate: AsrEngine.sampleRate,
        intervalMilliseconds: intervalMs,
        overlapMilliseconds: overlapMs,
      );
      _source = source;
      _merger = AsrLevenshteinMerger(
        engine.tokenizer,
        source.overlapSamples / source.intervalSamples,
        maxLevenshteinDistance: maxDistance,
      );
      reply.send(<Object>['started']);
    } catch (error) {
      reply.send(<Object>['error', '$error']);
    }
  }

  void _micChunk(List<Object> message) {
    final AsrEngine? engine = _engine;
    final AsrAudioSource? source = _source;
    final AsrLevenshteinMerger? merger = _merger;
    if (engine == null || source is! AsrMicrophoneWindowSource || merger == null) {
      return;
    }
    for (final Float32List window in source.feed(message[1] as Int16List)) {
      _consumeWindow(engine, merger, window);
    }
  }

  void _micEnd(List<Object> message) {
    final SendPort reply = message[1] as SendPort;
    _finish();
    _stopSource();
    reply.send(<Object>['done']);
  }

  /// 顺序跑完一个源的全部窗口（对应 Android 的三级流水，只是不拆线程）
  void _drain(AsrAudioSource source, AsrEngine engine) {
    final AsrLevenshteinMerger merger = _merger!;
    Float32List? features = source.next();
    while (features != null) {
      _consumeWindow(engine, merger, features);
      features = source.next();
    }
  }

  /// 一个窗口：预处理 → 识别 → 后处理 → 回报文本
  void _consumeWindow(
    AsrEngine engine,
    AsrLevenshteinMerger merger,
    Float32List rawAudio,
  ) {
    final Float32List features = engine.audioPreprocessor.process(rawAudio);
    for (final AsrStep step in engine.recognize(features)) {
      final AsrDecodedText? decoded = merger.push(step.tokenId, step.timestamp);
      if (decoded == null) {
        continue;
      }
      _totalConfirmed = _mergeText(_totalConfirmed, decoded.confirmedText);
      _lastUnconfirmed = decoded.unconfirmedText;
      _progress?.send(<Object>[
        'transcript',
        _totalConfirmed,
        _lastUnconfirmed,
      ]);
      if (step.tokenId == AsrSpeech.endOfSequence) {
        break;
      }
    }
  }

  /// 收尾：把最后一段「未确认」也转正
  ///
  /// 对应 Android 页的 `binding.tvTranscription.text = ....toString()` —— 那句只是把
  /// 灰色 span 去掉，**文字本身保留**。因此这里不是丢掉未确认部分，而是把它并进
  /// 已确认文本、并把未确认段置空。
  void _finish() {
    _totalConfirmed = _mergeText(_totalConfirmed, _lastUnconfirmed);
    _lastUnconfirmed = '';
    _progress?.send(<Object>['transcript', _totalConfirmed, '']);
  }

  void _stopSource() {
    _source?.close();
    _source = null;
    _merger = null;
  }

  /// 与 Android 页的 `merge(previous, next)` 同口径：空串不参与拼接
  static String _mergeText(String previous, String next) {
    final List<String> parts = <String>[previous, next]
        .where((String part) => part.isNotEmpty)
        .toList();
    return parts.join(' ');
  }

  void dispose() {
    _stopSource();
    final AsrEngine? engine = _engine;
    _engine = null;
    engine?.close();
  }
}

/// 主 isolate 侧的 ASR 客户端。
///
/// 生命周期：页面初始化时 [start]（拉起 isolate），换模型 / 换后端时 [loadEngine]，
/// 退出时 [close]。
class AsrWorkerClient {
  AsrWorkerClient({required this.catalogJson, required this.modelsDir});

  /// `asr_model_metadata.json` 的原文；worker 里不能读 asset，因此在主 isolate 读好传入
  final String catalogJson;

  /// 模型目录绝对路径（页面初始化时经 `MlModelStore.dir` 解析好再传进来）
  final String modelsDir;

  Isolate? _isolate;
  SendPort? _commandPort;
  ReceivePort? _lifecyclePort;
  ReceivePort? _progressPort;

  /// 是否已拉起
  bool get isStarted => _commandPort != null;

  /// 引擎装载结果（`loadEngine` 成功后可得）
  AsrEngineInfo? info;

  /// 转写进度回调：`(已确认文本, 未确认文本)`
  void Function(String confirmed, String unconfirmed)? onTranscript;

  /// 拉起 worker isolate（不装载引擎；引擎由 [loadEngine] 按模型 + 后端装载）
  Future<void> start() async {
    if (_isolate != null) {
      return;
    }
    final ReceivePort lifecycle = ReceivePort();
    final ReceivePort progress = ReceivePort();
    _lifecyclePort = lifecycle;
    _progressPort = progress;
    progress.listen((Object? message) {
      if (message is! List<Object> || message.length < 3) {
        return;
      }
      if (message[0] == 'transcript') {
        onTranscript?.call(message[1] as String, message[2] as String);
      }
    });

    final Isolate isolate = await Isolate.spawn<AsrWorkerInit>(
      asrWorkerEntry,
      (mainPort: lifecycle.sendPort),
      debugName: 'asr-worker',
    );
    _isolate = isolate;

    final Completer<void> ready = Completer<void>();
    SendPort? commandPort;
    lifecycle.listen((Object? message) {
      if (ready.isCompleted) {
        return;
      }
      if (message is! List<Object> || message.isEmpty) {
        return;
      }
      switch (message[0] as String) {
        case 'command-port':
          commandPort = message[1] as SendPort;
          if (!ready.isCompleted) {
            _commandPort = commandPort;
            ready.complete();
          }
        case 'error':
          if (!ready.isCompleted) {
            ready.completeError(StateError('ASR worker 启动失败: ${message[1]}'));
          }
        default:
          break;
      }
    });

    try {
      // 兜底：worker 若起不来（OOM、平台限制），不能让页面初始化永远挂住
      await ready.future.timeout(const Duration(seconds: 60));
    } catch (error) {
      isolate.kill(priority: Isolate.immediate);
      lifecycle.close();
      progress.close();
      _isolate = null;
      _commandPort = null;
      _lifecyclePort = null;
      _progressPort = null;
      rethrow;
    }
  }

  /// 装载（或换代）引擎：模型 + 分词器都已在本地，构建可能要数十秒
  Future<AsrEngineInfo> loadEngine(String modelKey, {required bool gpu}) async {
    final SendPort? commandPort = _commandPort;
    final SendPort? progressPort = _progressPort?.sendPort;
    if (commandPort == null || progressPort == null) {
      throw StateError('ASR worker 尚未启动');
    }
    final ReceivePort reply = ReceivePort();
    try {
      commandPort.send(<Object>[
        'load',
        catalogJson,
        modelKey,
        modelsDir,
        gpu,
        progressPort,
        reply.sendPort,
      ]);
      final Object? message = await reply.first.timeout(
        // 最大的 parakeet 约 614 MB，建图 + 分词器解析在低端机上要留足时间
        const Duration(seconds: 300),
      );
      if (message is! List<Object> || message.isEmpty) {
        throw StateError('ASR worker 返回了无法识别的消息');
      }
      if (message[0] == 'error') {
        throw StateError(message[1] as String);
      }
      final AsrEngineInfo loaded = (
        vocabSize: message[1] as int,
        backend: message[2] as String,
        windowMs: message[3] as int,
      );
      info = loaded;
      return loaded;
    } finally {
      reply.close();
    }
  }

  /// 转写一个 WAV 文件（整段跑完才返回）
  Future<void> transcribeFile(
    String wavPath, {
    required int intervalMs,
    required int overlapMs,
    required int maxDistance,
  }) async {
    final String? error = await _request(<Object>[
      'transcribe-file',
      wavPath,
      intervalMs,
      overlapMs,
      maxDistance,
    ], timeout: const Duration(minutes: 10));
    if (error != null) {
      throw StateError(error);
    }
  }

  /// 开始实时录音转写（后续用 [feedMicrophone] 推 PCM）
  Future<void> startMicrophone({
    required int intervalMs,
    required int overlapMs,
    required int maxDistance,
  }) async {
    final String? error = await _request(<Object>[
      'mic-start',
      intervalMs,
      overlapMs,
      maxDistance,
    ], timeout: const Duration(seconds: 60));
    if (error != null) {
      throw StateError(error);
    }
  }

  /// 推一段原始 16-bit PCM（单声道、16 kHz）。**不等回复**：这是高频单向消息，
  /// 等回复会让采集侧的节奏受推理速度牵制（与 Android 的 Channel 缓冲同义，
  /// 只是 isolate 邮箱天然带缓冲）。
  void feedMicrophone(Int16List samples) {
    _commandPort?.send(<Object>['mic-chunk', samples]);
  }

  /// 结束实时转写
  Future<void> stopMicrophone() async {
    final String? error = await _request(
      <Object>['mic-end'],
      timeout: const Duration(seconds: 60),
    );
    if (error != null) {
      throw StateError(error);
    }
  }

  /// 发一条带 reply 的请求；返回 `null` 表示成功，否则是错误信息
  Future<String?> _request(
    List<Object> body, {
    required Duration timeout,
  }) async {
    final SendPort? commandPort = _commandPort;
    if (commandPort == null) {
      throw StateError('ASR worker 尚未启动');
    }
    final ReceivePort reply = ReceivePort();
    try {
      commandPort.send(<Object>[...body, reply.sendPort]);
      final Object? message = await reply.first.timeout(timeout);
      if (message is! List<Object> || message.isEmpty) {
        return 'ASR worker 返回了无法识别的消息';
      }
      if (message[0] == 'error') {
        return message[1] as String;
      }
      return null;
    } finally {
      reply.close();
    }
  }

  /// 关闭 worker：先请它自行回收原生实例，超时再强杀
  Future<void> close() async {
    final Isolate? isolate = _isolate;
    final SendPort? commandPort = _commandPort;
    final ReceivePort? lifecycle = _lifecyclePort;
    final ReceivePort? progress = _progressPort;
    _isolate = null;
    _commandPort = null;
    _lifecyclePort = null;
    _progressPort = null;
    if (isolate == null) {
      return;
    }

    if (commandPort != null && lifecycle != null) {
      try {
        commandPort.send(<Object>['close']);
        // 等 worker 报 'closed'；它先 close() 引擎再发通知，因此超时要给够
        await lifecycle
            .firstWhere((Object? message) =>
                message is List<Object> && message.isNotEmpty && message[0] == 'closed')
            .timeout(const Duration(seconds: 60));
      } on Object catch (_) {
        // 超时或端口已关：强杀兜底。worker 是纯计算 isolate，杀掉不丢用户数据
        isolate.kill(priority: Isolate.immediate);
      }
    } else {
      isolate.kill(priority: Isolate.immediate);
    }
    lifecycle?.close();
    progress?.close();
  }
}
