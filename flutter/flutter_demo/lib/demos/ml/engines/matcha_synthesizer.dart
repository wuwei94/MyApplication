import 'dart:io';
import 'dart:math' as math;
import 'dart:typed_data';

import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_demo/demos/ml/engines/ml_binary.dart';
import 'package:flutter_demo/demos/ml/engines/ml_model_download.dart';
import 'package:flutter_demo/demos/ml/engines/ml_tflite_helper.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// Matcha-TTS 端侧声学模型 —— 与 Android `MatchaSynthesizer` 对应
/// （官方 `samples/litert/text_to_speech/kotlin_cpu_gpu`）。
///
/// 三张重图跑在 `tflite_flutter` 的 `Interpreter` 上，**所有随机 / 顺序逻辑都留在 host 侧**：
/// FiLM 声码器是时域 GAN（不含 FFT），这正是整条合成链路能停留在 GPU 上的原因。
///
/// ```
/// 音素 id --(host: 嵌入 + 补位)--> text_encoder(--> mu, logw)
///         --(host: 时长展开 + length regulator)--> mu_y[1,80,T]
///         --(host: Euler ODE 循环, N 步)-->
///             decoder x N  (x_t, mu_y, sin_emb(t), mask) --> v
///         --(host: 反归一化)--> mel --> vocoder --> 波形[T*256]
/// ```
///
/// 三个关键设计（照搬官方，改动会破坏时间对齐）：
/// 1. **固定形状**（[MAX_TEXT] 个音素 / [MAX_MEL] 帧），靠**运行时浮点 mask** 把补位位置
///    变成空操作（加性注意力偏置），因此一张编译好的图能吃任意长度输入；
/// 2. **decoder 走 CPU、text encoder 与 vocoder 走 GPU**：官方注释指出 diffusers transformer
///    在 Mali ML Drift delegate 上存在算子融合缺陷（整图里残差相关性塌到 0.006，而同一个
///    transformer 单独成图时是 0.98）——这是融合 bug 而非算子问题，因此 CPU 上反而精确；
/// 3. 模型**不入包**，与官方一致地按需下载到应用私有目录（见 [MlRemoteModels.matcha]）。
///
/// 各图输入/输出签名经 flatbuffer 解析实证核对（LiteRT 2.x 文件）：
/// - textenc：IN `[1,256,192]` embeds / `[1,1,256]` text_mask；
///   OUT `[1,80,256]` mu（通道优先）/ `[1,1,256]` log_durations；
/// - decoder：IN `[1,80,512]` x / `[1,80,512]` mu_y / `[1,160]` sin_emb / `[1,1,512]` mel_mask；
///   OUT `[1,80,512]` velocity；
/// - vocoder：IN `[1,80,512]` mel；OUT `[1,131072]` waveform。
class MatchaSynthesizer {
  MatchaSynthesizer();

  /// 一次合成结果：PCM 浮点波形 + 供界面展示的计时信息
  ({Float32List audio, int frames, int steps, int ms}) synthesize(
    List<int> phonemeIds, {
    int nSteps = defaultSteps,
    int? seed,
  }) {
    final Stopwatch started = Stopwatch()..start();

    // 穿插空音素（id 0）、补齐到 MAX_TEXT，并构造文本 mask
    final int textLength = math.min(phonemeIds.length * 2 + 1, maxText);
    final Int32List ids = Int32List(maxText);
    int writeIndex = 1;
    for (final int id in phonemeIds) {
      if (writeIndex >= maxText) break;
      ids[writeIndex] = id;
      writeIndex += 2;
    }
    final Float32List textMask = Float32List(maxText);
    for (int i = 0; i < maxText; i++) {
      textMask[i] = i < textLength ? 1.0 : 0.0;
    }

    // host 侧音素嵌入查表 -> [1, MAX_TEXT, N_CHANNELS]
    final Float32List embedded = Float32List(maxText * nChannels);
    for (int t = 0; t < maxText; t++) {
      final int row = ids[t] * nChannels;
      for (int c = 0; c < nChannels; c++) {
        embedded[t * nChannels + c] = _embeddingTable[row + c];
      }
    }

    // text encoder（GPU）-> mu[1,80,T], logw[1,1,T]
    MlTfliteHelper.writeFloatInput(_textEncoder, 0, embedded);
    MlTfliteHelper.writeFloatInput(_textEncoder, 1, textMask);
    _textEncoder.invoke();
    final Float32List mu = MlTfliteHelper.readFloatOutput(_textEncoder, tensorIndex: 0);
    final Float32List logDurations =
        MlTfliteHelper.readFloatOutput(_textEncoder, tensorIndex: 1);

    // 时长 -> 累积 -> length regulator（mu_y）
    final Float32List durations = Float32List(maxText);
    for (int i = 0; i < maxText; i++) {
      durations[i] = _ceil(math.exp(logDurations[i]) * textMask[i]) * lengthScale;
    }
    final Float32List cumulative = Float32List(maxText);
    double total = 0.0;
    for (int i = 0; i < maxText; i++) {
      total += durations[i];
      cumulative[i] = total;
    }
    final int melLength = math.min(math.max(total.toInt(), 1), maxMel);

    final Float32List muY = Float32List(nFeats * maxMel);
    int phonemeIndex = 0;
    for (int frame = 0; frame < melLength; frame++) {
      while (phonemeIndex < maxText - 1 && cumulative[phonemeIndex] <= frame) {
        phonemeIndex++;
      }
      for (int c = 0; c < nFeats; c++) {
        muY[c * maxMel + frame] = mu[c * maxText + phonemeIndex];
      }
    }
    final Float32List melMask = Float32List(maxMel);
    for (int i = 0; i < maxMel; i++) {
      melMask[i] = i < melLength ? 1.0 : 0.0;
    }

    // Euler ODE: x_{k+1} = x_k + dt * decoder(x_k, mu_y, sin_emb(t), mask)
    //
    // 随机源与 Kotlin `java.util.Random` 不同源（Dart 用 Box-Muller 产生高斯噪声），
    // 因此同一 seed 下波形与 Android 不完全逐位一致；合成链路本身一致。
    final math.Random random = math.Random(seed ?? DateTime.now().microsecondsSinceEpoch);
    final Float32List x = Float32List(nFeats * maxMel);
    for (int c = 0; c < nFeats; c++) {
      for (int frame = 0; frame < melLength; frame++) {
        x[c * maxMel + frame] = _nextGaussian(random);
      }
    }
    final double dt = 1.0 / nSteps;
    double time = 0.0;
    for (int step = 0; step < nSteps; step++) {
      MlTfliteHelper.writeFloatInput(_decoder, 0, x);
      MlTfliteHelper.writeFloatInput(_decoder, 1, muY);
      MlTfliteHelper.writeFloatInput(_decoder, 2, sinusoidalPositionEmbedding(time));
      MlTfliteHelper.writeFloatInput(_decoder, 3, melMask);
      _decoder.invoke();
      final Float32List velocity =
          MlTfliteHelper.readFloatOutput(_decoder, tensorIndex: 0);
      for (int i = 0; i < x.length; i++) {
        x[i] += dt * velocity[i];
      }
      time += dt;
    }

    // 反归一化 + 零填充 -> mel -> vocoder（GPU）-> 波形
    final Float32List mel = Float32List(nFeats * maxMel);
    for (int c = 0; c < nFeats; c++) {
      for (int frame = 0; frame < melLength; frame++) {
        final int i = c * maxMel + frame;
        mel[i] = x[i] * melStd + melMean;
      }
    }
    MlTfliteHelper.writeFloatInput(_vocoder, 0, mel);
    _vocoder.invoke();
    final Float32List waveform =
        MlTfliteHelper.readFloatOutput(_vocoder, tensorIndex: 0);
    final int sampleCount = melLength * hop;
    final Float32List audio = Float32List(sampleCount);
    for (int i = 0; i < sampleCount; i++) {
      audio[i] = waveform[i].clamp(-1.0, 1.0);
    }

    final int elapsed = started.elapsedMilliseconds;
    return (
      audio: audio,
      frames: melLength,
      steps: nSteps,
      ms: elapsed,
    );
  }

  // ─────────────────── 引擎装配 ───────────────────

  late final Float32List _embeddingTable;

  late final Interpreter _textEncoder;
  late final Interpreter _decoder;
  late final Interpreter _vocoder;

  bool _gpuFallback = false;
  bool _closed = false;

  /// 是否有图从 GPU 回退到了 CPU（构造期置位，供界面如实展示）
  bool get gpuFallback => _gpuFallback;

  /// 实际生效的后端描述，供界面如实展示（某张图回退到 CPU 时会被标注出来）
  String get acceleratorSummary {
    final String gpu = _gpuFallback ? 'GPU 不可用，已回退 CPU' : 'GPU';
    return 'text encoder / vocoder: $gpu | decoder: CPU';
  }

  /// 加载音素嵌入表（assets）与三张图。
  ///
  /// 构造中途失败时逐个回收**已建成的**解释器：`Interpreter` 持有原生内存，
  /// 抛异常时 GC 兜不住它们。
  Future<void> load() async {
    final List<Interpreter> created = <Interpreter>[];
    try {
      // 音素嵌入表，随 assets 分发（体积很小，官方同样放在 assets）。
      // emb.bin 是**无 npy 头**的裸 float32 小端表（178 × 192）
      final ByteData raw = await rootBundle.load('assets/ml/emb.bin');
      _embeddingTable = MlNpy.rawFloats(
        raw.buffer.asUint8List(raw.offsetInBytes, raw.lengthInBytes),
      );
      if (_embeddingTable.length != symbolCount * nChannels) {
        throw StateError('嵌入表行数应为 $symbolCount × $nChannels，'
            '实际 ${_embeddingTable.length ~/ nChannels}');
      }

      _textEncoder = await _loadModel('matcha_textenc_fp16.tflite', useGpu: true);
      created.add(_textEncoder);
      _decoder = await _loadModel('matcha_decoder_fp16.tflite', useGpu: false);
      created.add(_decoder);
      _vocoder = await _loadModel('matcha_vocoder_fp16.tflite', useGpu: true);
      created.add(_vocoder);
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

  /// 从应用私有目录加载一张图
  ///
  /// 后端分配与官方一致。GPU 不可用时（模拟器、无兼容驱动的设备）回退到 CPU，
  /// 只是慢一些，不影响结果正确性——因此不必让整页失败。
  Future<Interpreter> _loadModel(String fileName, {required bool useGpu}) async {
    final String path = await MlModelStore.pathIn(fileName);
    // 同步 stat：路径已由 pathIn 取得，存在性判定不涉及额外 IO 调度
    if (!File(path).existsSync()) {
      throw StateError('模型尚未下载: $fileName');
    }
    final MlEngineBundle bundle = await MlTfliteHelper.createInterpreterFromFile(
      File(path),
      useGpu: useGpu,
    );
    if (useGpu && bundle.engineLabel.contains('CPU')) {
      _gpuFallback = true;
    }
    return bundle.interpreter;
  }

  void close() {
    if (_closed) return;
    _closed = true;
    _textEncoder.close();
    _decoder.close();
    _vocoder.close();
  }

  // ─────────────────── 数值工具 ───────────────────

  /// Matcha SinusoidalPosEmb（无权重）：t -> [TIME_DIM] = [sin(...) | cos(...)]
  static Float32List sinusoidalPositionEmbedding(double t, {double scale = 1000.0}) {
    final int half = timeDim ~/ 2;
    final Float32List embedding = Float32List(timeDim);
    final double logScale = -math.log(10000.0) / (half - 1);
    for (int i = 0; i < half; i++) {
      final double angle = scale * t * math.exp(i * logScale);
      embedding[i] = math.sin(angle);
      embedding[half + i] = math.cos(angle);
    }
    return embedding;
  }

  /// Box-Muller 变换：均匀随机 -> 标准正态（Kotlin 侧是 java.util.Random.nextGaussian）
  static double _nextGaussian(math.Random random) {
    final double u1 = random.nextDouble();
    final double u2 = random.nextDouble();
    if (u1 <= 0.0) return 0.0;
    return math.sqrt(-2.0 * math.log(u1)) * math.cos(2.0 * math.pi * u2);
  }

  static double _ceil(double value) => value.ceilToDouble();

  // ─────────────────── 常量（与 Android 逐项一致） ───────────────────

  static const int maxText = 256;
  static const int maxMel = 512;
  static const int nFeats = 80;
  static const int nChannels = 192;
  static const int hop = 256;
  static const int sampleRate = 22050;
  static const double lengthScale = 0.95;
  static const double melMean = -5.536622;
  static const double melStd = 2.116101;
  static const int timeDim = 160;
  static const int defaultSteps = 10;
  static const int symbolCount = 178;
}
