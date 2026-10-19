import 'dart:io';
import 'dart:math' as math;
import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/ml_model_download.dart';
import 'package:flutter_demo/demos/ml/engines/ml_npy.dart';
import 'package:flutter_demo/demos/ml/engines/ml_tflite_helper.dart';
import 'package:flutter_demo/demos/ml/engines/ml_zip.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// 一次输入 feed：目标形状 + 数据字节
typedef MlTensorFeed = ({List<int> shape, Uint8List bytes});

/// 某张图的输出读取器，按**张量名后缀**定位下标
///
/// `getOutputTensor` 的顺序按 FlatBuffer 张量表排，与签名输出顺序不保证一致
/// （predictor 的三个输出在表里就是 0 / 2 / 1 交错），因此按名字寻址。
class MlGraphOutputs {
  MlGraphOutputs(this._interpreter);

  final Interpreter _interpreter;
  final Map<String, int> _indexes = <String, int>{};

  int _index(String suffix) => _indexes.putIfAbsent(
        suffix,
        () => MlTfliteHelper.outputIndexBySuffix(_interpreter, suffix),
      );

  Float32List float(String suffix) =>
      MlTfliteHelper.readFloatOutput(_interpreter, tensorIndex: _index(suffix));

  /// 读 Int32 输出。方法名不能叫 `int` —— 那会遮蔽类作用域里的 `int` 类型
  Int32List ints(String suffix) =>
      MlTfliteHelper.readIntOutput(_interpreter, tensorIndex: _index(suffix));
}

/// KittenTTS nano 0.8（StyleTTS2 + ISTFTNet，15M 参数，24 kHz）的端侧实现
/// —— 与 Android `KittenSynthesizer` 对应（官方
/// `samples/litert/text_to_speech_streaming/kotlin_cpu`）。
///
/// 三张图都是**动态序列长度**——同一套图跑任意长度的句子，不做 padding 分桶，
/// 因此算力随文本长度伸缩：
///
/// ```
/// 符号 id [1,N] + style [1,256] + speed [1]
///   -> predictor -> d [1,N,256], t_en [1,N,128], durations [N]
///   -> (host：按 durations 重复行) -> en [1,T,256], asr [1,T,128]
///   -> prosody  -> f0 [1,2T], noise [1,2T], harmonics [1,120T+1,22]
///   -> vocoder  -> 波形 [1,600T] @ 24 kHz
/// ```
///
/// 图之间唯一的「胶水」是按 duration 重复行（`en = repeat(d, durations)`），
/// 与参考 ONNX 图内的 `Loop` 逐位一致。
///
/// ### 三张图统一走经典 Interpreter
///
/// 官方这条示例同时用两套 API：predictor / prosody 用 `Interpreter`（它们的定长
/// LSTM 隐状态存在 TFLite *variable tensors* 里，CompiledModel 的加载器不接受），
/// 而 vocoder 用 `CompiledModel` + 一份 JNI 桥——因为 CompiledModel 的输出缓冲是
/// **调用前**一次性分配的，而 vocoder 的波形张量是 dynamic tensor，形状要等到
/// invoke 时才落定。`Interpreter` 本来就原生支持动态输出（`resizeInput` →
/// `allocateTensors` → `invoke` 之后直接读输出张量的实际形状），因此本实现
/// **三张图统一走 `Interpreter`**，不为一条示例页引入 NDK。
///
/// 各图输入 / 输出签名经 flatbuffer 解析实证核对（LiteRT 2.x 文件）：
/// - predictor：IN `style [1,256]` F32 / `speed [1]` F32 / `input_ids [1,N]` INT32；
///   OUT `StatefulPartitionedCall:0` = t_en `[1,N,128]` F32、
///   `:1` = durations `[N]` INT32、`:2` = d `[1,N,256]` F32；
/// - prosody：IN `style` / `en [1,T,256]`；OUT `:0` = f0 `[1,2T]`、`:1` = noise
///   `[1,2T]`、`:2` = harmonics `[1,120T+1,22]`（均 F32）；
/// - vocoder：IN `asr [1,T,128]` / `f0 [1,2T]` / `n [1,2T]` /
///   `har [1,120T+1,22]` / `style`；OUT `Identity` = waveform `[1,600T]`。
class KittenSynthesizer {
  /// 24 kHz 单声道浮点 PCM
  static const int sampleRate = 24000;

  /// 8 个内置音色，顺序即音色表行序
  static const List<String> voices = <String>[
    'expr-voice-2-m',
    'expr-voice-2-f',
    'expr-voice-3-m',
    'expr-voice-3-f',
    'expr-voice-4-m',
    'expr-voice-4-f',
    'expr-voice-5-m',
    'expr-voice-5-f',
  ];

  /// 音色表每个音色的行数（每行对应一个文本长度）
  static const int styleRows = 400;

  /// 音色向量维度
  static const int styleDim = 256;

  /// [modelsDir] 模型目录绝对路径；null 表示自行经 [MlModelStore] 解析
  /// （主 isolate 路径）。后台 isolate 里 path_provider 的平台通道不可用，
  /// 由调用方把已解析好的目录传进来。
  KittenSynthesizer({String? modelsDir}) : _modelsDir = modelsDir;

  final String? _modelsDir;

  late final Interpreter _predictor;
  late final Interpreter _prosody;
  late final Interpreter _vocoder;

  /// `[voices.length, styleRows, styleDim]` 音色表（扁平 float32）
  late final Float32List _voiceTable;

  /// 三张图在磁盘上的总字节数，供界面显示「模型很小」这一卖点
  late final int modelBytes;

  bool _closed = false;

  /// 加载三张图与音色表。
  ///
  /// 构造中途失败时逐个回收**已建成的**解释器：`Interpreter` 持有原生内存，
  /// 抛异常时 GC 兜不住它们。
  Future<void> load() async {
    final List<Interpreter> created = <Interpreter>[];
    try {
      _predictor = await _loadModel('kitten_predictor_fp16.tflite');
      created.add(_predictor);
      _prosody = await _loadModel('kitten_prosody_fp16.tflite');
      created.add(_prosody);
      _vocoder = await _loadModel('kitten_vocoder_fp16.tflite');
      created.add(_vocoder);

      modelBytes = 0;
      for (final String name in const <String>[
        'kitten_predictor_fp16.tflite',
        'kitten_prosody_fp16.tflite',
        'kitten_vocoder_fp16.tflite',
      ]) {
        modelBytes += File(await _pathIn(name)).lengthSync();
      }

      // 音色表：voices.npz 端侧解包（官方推送的是 conversion/ 重打包的 voices.bin，
      // HF 仓库里只有 npz，因此与 Android 一样直接解 npz，省掉一份派生产物）
      final File npz = File(await _pathIn('voices.npz'));
      final Map<String, Uint8List> entries = MlZipReader.readNpz(npz);
      _voiceTable = Float32List(voices.length * styleRows * styleDim);
      for (int v = 0; v < voices.length; v++) {
        final Uint8List? npy = entries['${voices[v]}.npy'];
        if (npy == null) {
          throw StateError('音色表缺少 ${voices[v]}.npy'
              '（实际条目: ${entries.keys.toList()..sort()}）');
        }
        final Float32List values = MlNpy.floats(npy);
        if (values.length != styleRows * styleDim) {
          throw StateError('${voices[v]}.npy 形状不符：${values.length} 个 float，'
              '期望 ${styleRows * styleDim}');
        }
        _voiceTable.setRange(
          v * styleRows * styleDim,
          (v + 1) * styleRows * styleDim,
          values,
        );
      }
    } catch (error) {
      for (final Interpreter interpreter in created.reversed) {
        try {
          interpreter.close();
        } on Object catch (_) {
          // 回收失败不吞掉原始异常
        }
      }
      rethrow;
    }
  }

  /// 模型文件的绝对路径：优先用注入的目录，否则经 [MlModelStore] 解析
  Future<String> _pathIn(String fileName) async =>
      _modelsDir != null ? '$_modelsDir/$fileName' : MlModelStore.pathIn(fileName);

  Future<Interpreter> _loadModel(String fileName) async {
    final String path = await _pathIn(fileName);
    // 同步 stat：路径已由 pathIn 取得，存在性判定不涉及额外 IO 调度
    if (!File(path).existsSync()) {
      throw StateError('模型尚未下载: $fileName');
    }
    // 三张图全部 CPU（与 Android createInterpreterOptions 同口径：
    // availableProcessors.coerceIn(2,4) 线程 + XNNPACK）；Kitten 是纯 CPU 任务，
    // GPU delegate 对 LSTM variable tensors 也不接受
    final MlEngineBundle bundle = await MlTfliteHelper.createInterpreterFromFile(
      File(path),
      useGpu: false,
    );
    return bundle.interpreter;
  }

  /// 一句的合成结果：24 kHz 浮点 PCM，外加各阶段耗时供界面展示
  ({Float32List audio, int tokens, int frames, int synthMs}) synthesize(
    List<int> symbolIds, {
    required int voice,
    required int textLength,
    double speed = 1.0,
  }) {
    final Stopwatch started = Stopwatch()..start();

    final Float32List style = Float32List(styleDim);
    _styleFor(voice, textLength, style);

    // predictor：[0] + ids + [0] -> 逐 token 特征 + 整数时长
    final int tokenCount = symbolIds.length + 2;
    final Int32List ids = Int32List(tokenCount);
    ids[0] = 0;
    for (int i = 0; i < symbolIds.length; i++) {
      ids[i + 1] = symbolIds[i];
    }
    ids[tokenCount - 1] = 0;

    final MlGraphOutputs predictorOut = _run(
      _predictor,
      <String, Object>{
        'style': _floatFeed(<int>[1, styleDim], style),
        'speed': _floatFeed(<int>[1], Float32List.fromList(<double>[speed])),
        'input_ids': _intFeed(<int>[1, tokenCount], ids),
      },
    );
    final Float32List tEn = predictorOut.float('StatefulPartitionedCall:0'); // [1,N,128]
    final Int32List durations = predictorOut.ints('StatefulPartitionedCall:1'); // [N]
    final Float32List d = predictorOut.float('StatefulPartitionedCall:2'); // [1,N,256]

    // 按时长重复每个 token 的特征行（上游 ONNX 图内 Loop 的等价 host 实现）
    int frames = 0;
    for (final int duration in durations) {
      frames += duration;
    }
    if (frames <= 0) {
      throw StateError('模型预测的时长全为 0，无法合成');
    }
    final Float32List en = _repeatRows(d, durations, styleDim);
    final Float32List asr = _repeatRows(tEn, durations, 128);

    // prosody：整句的谐波 F0 / 噪声 / 谐波包络
    final MlGraphOutputs prosodyOut = _run(
      _prosody,
      <String, Object>{
        'style': _floatFeed(<int>[1, styleDim], style),
        'en': _floatFeed(<int>[1, frames, styleDim], en),
      },
    );
    final Float32List f0 = prosodyOut.float('StatefulPartitionedCall:0'); // [1,2T]
    final Float32List noise = prosodyOut.float('StatefulPartitionedCall:1'); // [1,2T]
    final Float32List harmonics =
        prosodyOut.float('StatefulPartitionedCall:2'); // [1,120T+1,22]

    // vocoder（ISTFTNet）：对齐后的文本特征 + 韵律 -> 波形 [1,600T]
    final Float32List waveform = _vocode(
      asr: asr,
      frames: frames,
      f0: f0,
      noise: noise,
      harmonics: harmonics,
      style: style,
    );

    // 上游 pip 包会裁掉每段末尾的合成尾巴
    final int sampleCount =
        math.max(waveform.length - tailTrim, math.min(minSamples, waveform.length));
    final Float32List audio = Float32List(sampleCount);
    for (int i = 0; i < sampleCount; i++) {
      audio[i] = waveform[i].clamp(-1.0, 1.0);
    }

    return (
      audio: audio,
      tokens: tokenCount,
      frames: frames,
      synthMs: started.elapsedMilliseconds,
    );
  }

  /// 用本句的动态形状跑 vocoder
  ///
  /// 官方这里调 JNI 的 `runDynamic` 并**显式传入**输出形状 `[1, 600T]`；
  /// `Interpreter` 路径不需要——`allocateTensors` 之后 invoke，张量形状就在
  /// 运行时落定，[MlGraphOutputs] 直接按缓冲区的实际长度读取。
  Float32List _vocode({
    required Float32List asr,
    required int frames,
    required Float32List f0,
    required Float32List noise,
    required Float32List harmonics,
    required Float32List style,
  }) {
    final MlGraphOutputs out = _run(
      _vocoder,
      <String, Object>{
        'asr': _floatFeed(<int>[1, frames, 128], asr),
        'f0': _floatFeed(<int>[1, f0.length], f0),
        'n': _floatFeed(<int>[1, noise.length], noise),
        'har': _floatFeed(<int>[1, harmonics.length ~/ harDim, harDim], harmonics),
        'style': _floatFeed(<int>[1, styleDim], style),
      },
    );
    return out.float('Identity');
  }

  /// `np.repeat(x[0], durations, axis=0)`，`x` 是行优先的 `[1,N,dim]`
  Float32List _repeatRows(Float32List x, Int32List durations, int dim) {
    int total = 0;
    for (final int duration in durations) {
      total += duration;
    }
    final Float32List out = Float32List(total * dim);
    int write = 0;
    for (int row = 0; row < durations.length; row++) {
      for (int r = 0; r < durations[row]; r++) {
        out.setRange(write, write + dim, x, row * dim);
        write += dim;
      }
    }
    return out;
  }

  /// 取某个音色针对 [textLength] 的 style 行
  ///
  /// 行号取 `min(字符数, styleRows - 1)`，与上游 pip 包一致（超出表长时钳到末行）。
  void _styleFor(int voice, int textLength, Float32List out) {
    final int row = math.min(math.max(textLength, 0), styleRows - 1);
    final int offset = (voice * styleRows + row) * styleDim;
    out.setRange(0, styleDim, _voiceTable, offset);
  }

  // ─────────────────── 动态形状驱动 ───────────────────

  /// 一次 feed：目标形状 + 数据字节
  MlTensorFeed _floatFeed(List<int> shape, Float32List data) =>
      (shape: shape, bytes: data.buffer.asUint8List(data.offsetInBytes, data.lengthInBytes));

  MlTensorFeed _intFeed(List<int> shape, Int32List data) =>
      (shape: shape, bytes: data.buffer.asUint8List(data.offsetInBytes, data.lengthInBytes));

  /// 用一句的实际形状驱动一张图：逐个输入 `resizeInput` → 分配 → invoke
  ///
  /// 输入按**规范化张量名**匹配（`serving_default_x:0` 与 `x` 都归一到 `x`），
  /// 因此同一条代码路径既能驱动带签名的图（predictor / prosody），也能驱动不带
  /// 签名的图（vocoder 的输入就叫 `asr` / `f0` / `n` / `har` / `style`）。
  ///
  /// `resetVariableTensors` 是**必须**的：这两张图的 LSTM 隐状态存放在 variable
  /// tensors 中并跨调用保留。形状变化会因重新分配而顺带重置，但**长度相同的两次
  /// 合成不会**——第二次会从上一次遗留的末状态开始，把预测时长带偏
  /// （复现方式：同一句话连说两遍）。注意走的是 helper 里的 FFI 直调版本，
  /// 原因见 [MlTfliteHelper.resetVariableTensors] 的注释。
  MlGraphOutputs _run(Interpreter interpreter, Map<String, Object> feeds) {
    bool resized = false;
    final List<Tensor> inputs = interpreter.getInputTensors();
    // 与 inputs 逐下标对齐：先按名字匹配取到 feed，再统一写数据
    final List<MlTensorFeed> ordered = <MlTensorFeed>[];
    for (int i = 0; i < inputs.length; i++) {
      final String name = MlTfliteHelper.canonicalTensorName(inputs[i].name);
      final Object? raw = feeds[name];
      if (raw is! MlTensorFeed) {
        throw StateError('缺少输入张量: $name');
      }
      ordered.add(raw);
      if (!_shapeEquals(inputs[i].shape, raw.shape)) {
        interpreter.resizeInputTensor(i, raw.shape);
        resized = true;
      }
    }
    if (resized) {
      interpreter.allocateTensors();
    }
    // resizeInputTensor 会清空 tflite_flutter 内部的张量缓存，因此写数据前必须
    // 重新取一遍输入张量（否则拿到的是 resize 前形状的旧包装，data setter 的
    // 字节数校验会直接抛错）
    final List<Tensor> freshInputs = interpreter.getInputTensors();
    for (int i = 0; i < freshInputs.length; i++) {
      freshInputs[i].data = ordered[i].bytes;
    }
    MlTfliteHelper.resetVariableTensors(interpreter);
    interpreter.invoke();
    return MlGraphOutputs(interpreter);
  }

  static bool _shapeEquals(List<int> a, List<int> b) {
    if (a.length != b.length) return false;
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) return false;
    }
    return true;
  }

  void close() {
    if (_closed) return;
    _closed = true;
    _predictor.close();
    _prosody.close();
    _vocoder.close();
  }

  // ─────────────────── 常量（与 Android 逐项一致） ───────────────────

  /// predictor / prosody 输出的维度
  static const int asrDim = 128;
  static const int dDim = 256;
  static const int harDim = 22;

  /// 上游 pip 包会从每段末尾裁掉这么多采样点
  static const int tailTrim = 5000;
  static const int minSamples = 1200;
}
