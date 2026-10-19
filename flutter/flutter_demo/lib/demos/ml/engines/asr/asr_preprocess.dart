/// ASR 的音频预处理：原始波形 → 模型输入特征 —— 与 Android 侧
/// `MelSpectroProcessor` / `DummyAudioProcessor` 一一对应。
///
/// 官方有两个实现：真算 log-Mel 的 [AsrMelSpectroProcessor]，以及**原样返回**的
/// [AsrDummyPreprocessor]。后者不是摆设 —— Moonshine 系列的图直接吃原始波形
/// （官方 README：「Except moonshine which gets raw audio as input」）。
///
/// 预处理链路**逐行为对齐官方**：预加重 → 取 2 的幂 FFT → log-Mel → 按 `normType`
/// 归一化 → 按 `transpose` 打包成 `[nFrames × nMels]`。
library;

import 'dart:math' as math;
import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/asr/asr_log_mel.dart';

/// log-Mel 频谱配置（对应官方 `LogMelSpectroConfig`，字段名与 `asr_model_metadata.json` 一致）
class AsrLogMelConfig {
  const AsrLogMelConfig({
    required this.nFft,
    this.nMels = 80,
    this.hopLength = 160,
    this.nFrames = 0,
    this.transpose = false,
    this.preemphasis = 0.0,
    this.normType = 'standard',
  });

  /// FFT 长度。**会被向上取到 2 的幂**（whisper 的 400 → 512），与官方一致
  final int nFft;
  final int nMels;
  final int hopLength;

  /// 输出帧数；`<= 0` 表示跟随实际帧数
  final int nFrames;

  /// 是否转置成 `[frames][mels]`（Parakeet-CTC 需要）
  final bool transpose;

  /// 预加重系数；`<= 0` 表示不做
  final double preemphasis;

  /// `"whisper"` 走 whisper 的 log10→ln 缩放 + 裁剪归一化，其余走逐 mel 的 z-score
  final String normType;

  /// 从 `asr_model_metadata.json` 里的一项解析
  static AsrLogMelConfig fromJson(Map<String, Object?> json) => AsrLogMelConfig(
        nFft: (json['nFFT'] as num?)?.toInt() ?? 0,
        nMels: (json['nMels'] as num?)?.toInt() ?? 80,
        hopLength: (json['hopLength'] as num?)?.toInt() ?? 160,
        nFrames: (json['nFrames'] as num?)?.toInt() ?? 0,
        transpose: json['transpose'] as bool? ?? false,
        preemphasis: (json['preemphasis'] as num?)?.toDouble() ?? 0.0,
        normType: json['normType'] as String? ?? 'standard',
      );
}

/// 原始波形 → 模型输入特征
abstract class AsrPreprocessor {
  Float32List process(Float32List rawSpeech);
}

/// 官方 `MelSpectroProcessor` 的等价实现
class AsrMelSpectroProcessor implements AsrPreprocessor {
  AsrMelSpectroProcessor(this.samplingRate, this.config);

  final int samplingRate;
  final AsrLogMelConfig config;

  @override
  Float32List process(Float32List rawSpeech) {
    if (rawSpeech.isEmpty) {
      return Float32List(0);
    }
    final Float32List audio = config.preemphasis > 0.0
        ? applyPreemphasis(rawSpeech, config.preemphasis)
        : rawSpeech;

    final int paddedNfft = AsrLogMel.nextPowerOfTwo(config.nFft);
    final List<Float32List> melSpec = AsrLogMel.spectrogram(
      audio,
      samplingRate,
      paddedNfft,
      config.nMels,
      config.hopLength,
    );
    final int validFrames = rawSpeech.length ~/ config.hopLength;

    if (config.normType == 'whisper') {
      _normalizeWhisper(melSpec, validFrames);
    } else {
      _normalizeStandard(melSpec, validFrames);
    }

    final int nFrames = config.nFrames > 0 ? config.nFrames : melSpec[0].length;
    final Float32List result = Float32List(nFrames * config.nMels);
    for (int m = 0; m < melSpec.length; m++) {
      final Float32List row = melSpec[m];
      for (int f = 0; f < row.length; f++) {
        if (f >= nFrames) {
          break;
        }
        // 超出有效帧的部分一律填零：图的输入是定长，多出来的槽位必须是静音
        final double value = f < validFrames ? row[f] : 0.0;
        if (config.transpose) {
          result[f * config.nMels + m] = value;
        } else {
          result[m * nFrames + f] = value;
        }
      }
    }
    return result;
  }

  /// `y[n] = x[n] - coeff * x[n-1]`，首元素保持原值
  static Float32List applyPreemphasis(Float32List audio, double coeff) {
    if (audio.isEmpty) {
      return Float32List(0);
    }
    final Float32List result = Float32List(audio.length);
    result[0] = audio[0];
    for (int i = 1; i < audio.length; i++) {
      result[i] = audio[i] - coeff * audio[i - 1];
    }
    return result;
  }

  /// Whisper 的归一化：先把 ln 值除以 `ln(10)` 换回 log10 口径，
  /// 再在**全局**最大值下裁剪到 `max-8`，最后线性映射到 `[0, 1]`（`(x-clip+4)/4`）
  static void _normalizeWhisper(List<Float32List> melSpec, int validFrames) {
    double maxVal = double.negativeInfinity;
    for (final Float32List row in melSpec) {
      for (int f = 0; f < validFrames; f++) {
        row[f] = row[f] / _ln10;
        if (row[f] > maxVal) {
          maxVal = row[f];
        }
      }
    }
    final double clipVal = maxVal - 8.0;
    for (final Float32List row in melSpec) {
      for (int f = 0; f < validFrames; f++) {
        row[f] = (math.max(row[f], clipVal) + 4.0) / 4.0;
      }
    }
  }

  /// 逐 mel 的 z-score，统计量只覆盖有效帧（样本方差，除以 `n-1`）
  static void _normalizeStandard(List<Float32List> melSpec, int validFrames) {
    final int count = melSpec.length;
    final Float64List means = Float64List(count);
    final Float64List stds = Float64List(count);
    for (int m = 0; m < count; m++) {
      final Float32List row = melSpec[m];
      double sum = 0.0;
      for (int f = 0; f < validFrames; f++) {
        sum += row[f];
      }
      means[m] = sum / validFrames;
      double sumSq = 0.0;
      for (int f = 0; f < validFrames; f++) {
        final double diff = row[f] - means[m];
        sumSq += diff * diff;
      }
      stds[m] = math.sqrt(sumSq / math.max(1, validFrames - 1));
    }
    for (int m = 0; m < count; m++) {
      final Float32List row = melSpec[m];
      final double mean = means[m];
      final double std = stds[m] + _epsilon;
      for (int f = 0; f < validFrames; f++) {
        row[f] = (row[f] - mean) / std;
      }
    }
  }

  static final double _ln10 = math.log(10.0);
  static const double _epsilon = 1e-5;
}

/// 不做任何预处理，直接返回输入（Moonshine 这类「吃原始波形」的图用）
class AsrDummyPreprocessor implements AsrPreprocessor {
  const AsrDummyPreprocessor();

  @override
  Float32List process(Float32List rawSpeech) => rawSpeech;
}
