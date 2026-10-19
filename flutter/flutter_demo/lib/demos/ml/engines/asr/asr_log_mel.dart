/// **librosa 兼容的 log-Mel 频谱** —— 与 Android 侧 `LogMel` 一一对应。
///
/// Android 侧刻意没有复刻官方依赖的 jlibrosa（那是个「构建期从 GitHub 拉二进制」的
/// 外部 JAR，不可复现），而是按 **librosa 的语义**重写：等价于
/// `librosa.feature.melspectrogram(y, sr, n_fft, hop_length, window="hann",
/// center=True, pad_mode="constant", power=2.0, n_mels, fmin=0, fmax=sr/2,
/// htk=False, norm="slaney")`，再逐元素取 `ln(x + 2⁻²⁴)`。
///
/// 本文件是那份实现的 Dart 直译，两处上游取舍一并沿用：
/// 1. `nFFT` **先向上取到 2 的幂**（whisper 的 400 → 512）再做 FFT；
/// 2. 噪声底 `2⁻²⁴` 是官方常量，不是 librosa 的默认（librosa 用 `amin=1e-10`）。
library;

import 'dart:math' as math;
import 'dart:typed_data';

/// log-Mel 计算（全部为不依赖平台状态的静态方法，便于单测对拍 librosa 的 golden 向量）
abstract final class AsrLogMel {
  /// `2⁻²⁴`：官方 `MelSpectroProcessor.LOG_ZERO_GUARD_VALUE`，防止 `ln(0)`
  static final double logZeroGuard = math.pow(2.0, -24).toDouble();

  /// 计算 log-Mel 频谱：返回 `[nMels][frames]`，值为 `ln(mel + 2⁻²⁴)`
  ///
  /// [waveform] 是已做预加重的单声道波形（采样率由调用方保证为 16 kHz）。
  static List<Float32List> spectrogram(
    Float32List waveform,
    int sampleRate,
    int nFft,
    int nMels,
    int hopLength,
  ) {
    if (nFft <= 0 || (nFft & (nFft - 1)) != 0) {
      throw ArgumentError.value(nFft, 'nFft', '必须是 2 的幂');
    }
    if (hopLength <= 0) {
      throw ArgumentError.value(hopLength, 'hopLength', '必须为正');
    }

    final int frames = frameCount(waveform.length, nFft, hopLength);
    final List<Float32List> power =
        _powerSpectrogram(waveform, nFft, hopLength, frames);
    final List<Float32List> filters = melFilterBank(sampleRate, nFft, nMels);

    return List<Float32List>.generate(nMels, (int m) {
      final Float32List row = filters[m];
      final Float32List out = Float32List(frames);
      for (int f = 0; f < frames; f++) {
        double sum = 0.0;
        for (int k = 0; k < row.length; k++) {
          final double weight = row[k];
          if (weight != 0.0) {
            sum += weight * power[k][f];
          }
        }
        out[f] = math.log(sum + logZeroGuard);
      }
      return out;
    });
  }

  /// `center=True` 时的帧数：`1 + len(y) / hop`
  ///
  /// 两侧各补 `nFft / 2` 个零（librosa 1.x 的 `pad_mode="constant"` 默认值）。
  static int frameCount(int sampleCount, int nFft, int hopLength) {
    if (sampleCount <= 0) {
      return 0;
    }
    return 1 + sampleCount ~/ hopLength;
  }

  /// 周期 Hann 窗（`scipy.signal.get_window("hann", n, fftbins=True)` 的等价形式）
  static Float32List hannWindow(int n) {
    final Float32List window = Float32List(n);
    for (int i = 0; i < n; i++) {
      window[i] = 0.5 - 0.5 * math.cos(2.0 * math.pi * i / n);
    }
    return window;
  }

  /// slaney 归一化的 mel 滤波器组，形状 `[nMels][nFft/2 + 1]`
  ///
  /// 对应 `librosa.filters.mel(sr, n_fft, n_mels, fmin=0, fmax=sr/2, htk=False,
  /// norm="slaney")`。
  static List<Float32List> melFilterBank(
    int sampleRate,
    int nFft,
    int nMels, {
    double fMin = 0.0,
    double? fMax,
  }) {
    final double top = fMax ?? sampleRate / 2.0;
    final int bins = nFft ~/ 2 + 1;

    // librosa: np.linspace(0, sr / 2, 1 + n_fft // 2)
    final Float32List fftFreqs = Float32List(bins);
    for (int i = 0; i < bins; i++) {
      fftFreqs[i] = i * (sampleRate / 2.0) / (bins - 1);
    }

    final double melMin = hzToMel(fMin);
    final double melMax = hzToMel(top);
    final Float32List melF = Float32List(nMels + 2);
    for (int i = 0; i < nMels + 2; i++) {
      melF[i] = melToHz(melMin + (melMax - melMin) * i / (nMels + 1));
    }
    final Float32List fDiff = Float32List(nMels + 1);
    for (int i = 0; i < nMels + 1; i++) {
      fDiff[i] = melF[i + 1] - melF[i];
    }

    return List<Float32List>.generate(nMels, (int m) {
      final double lowerDiff = fDiff[m];
      final double upperDiff = fDiff[m + 1];
      final double enorm = 2.0 / (melF[m + 2] - melF[m]);
      final Float32List row = Float32List(bins);
      for (int k = 0; k < bins; k++) {
        // librosa: lower = -ramps[i] / fdiff[i]，ramps[i] = mel_f[i] - fft_freqs
        //          upper = ramps[i+2] / fdiff[i+1]
        final double lower = (fftFreqs[k] - melF[m]) / lowerDiff;
        final double upper = (melF[m + 2] - fftFreqs[k]) / upperDiff;
        final double triangle = math.min(lower, upper);
        row[k] = (triangle > 0.0 ? triangle : 0.0) * enorm;
      }
      return row;
    });
  }

  /// `librosa.hz_to_mel(freq, htk=False)`（Slaney 公式）
  static double hzToMel(double freq) => freq >= _minLogHz
      ? _minLogMel + math.log(freq / _minLogHz) / _logStep
      : (freq - _fMin) / _fSp;

  /// `librosa.mel_to_hz(mel, htk=False)`
  static double melToHz(double mel) => mel >= _minLogMel
      ? _minLogHz * math.exp(_logStep * (mel - _minLogMel))
      : _fMin + _fSp * mel;

  /// 向上取到 2 的幂（官方 `getSmallestPowerOfTwoGreaterOrEqualTo`，n 已是 2 的幂时取自身）
  static int nextPowerOfTwo(int n) {
    if (n > 0 && (n & (n - 1)) == 0) {
      return n;
    }
    int p = 1;
    while (p < n) {
      p <<= 1;
    }
    return p;
  }

  /// 原地 radix-2 复数 FFT（[re] / [im] 等长且为 2 的幂）
  static void fft(Float32List re, Float32List im) {
    final int n = re.length;
    if (n != im.length) {
      throw ArgumentError('实部与虚部长度必须一致：$n vs ${im.length}');
    }
    if (n <= 0 || (n & (n - 1)) != 0) {
      throw ArgumentError.value(n, 'n', 'FFT 长度必须是 2 的幂');
    }

    int j = 0;
    for (int i = 1; i < n; i++) {
      int bit = n >> 1;
      while ((j & bit) != 0) {
        j ^= bit;
        bit >>= 1;
      }
      j |= bit;
      if (i < j) {
        final double tr = re[i];
        re[i] = re[j];
        re[j] = tr;
        final double ti = im[i];
        im[i] = im[j];
        im[j] = ti;
      }
    }

    int length = 2;
    while (length <= n) {
      final int half = length ~/ 2;
      // 预计算本级的旋转因子，避免逐次复乘累积误差
      final Float64List cosTable = Float64List(half);
      final Float64List sinTable = Float64List(half);
      for (int k = 0; k < half; k++) {
        cosTable[k] = math.cos(-2.0 * math.pi * k / length);
        sinTable[k] = math.sin(-2.0 * math.pi * k / length);
      }
      int blockStart = 0;
      while (blockStart < n) {
        for (int k = 0; k < half; k++) {
          final int evenIndex = blockStart + k;
          final int oddIndex = evenIndex + half;
          final double wr = cosTable[k];
          final double wi = sinTable[k];
          final double oddRe = re[oddIndex];
          final double oddIm = im[oddIndex];
          final double tr = oddRe * wr - oddIm * wi;
          final double ti = oddRe * wi + oddIm * wr;
          re[oddIndex] = re[evenIndex] - tr;
          im[oddIndex] = im[evenIndex] - ti;
          re[evenIndex] += tr;
          im[evenIndex] += ti;
        }
        blockStart += length;
      }
      length <<= 1;
    }
  }

  /// `|STFT|²`，形状 `[nFft/2 + 1][frames]`
  static List<Float32List> _powerSpectrogram(
    Float32List waveform,
    int nFft,
    int hopLength,
    int frames,
  ) {
    final int bins = nFft ~/ 2 + 1;
    final Float32List window = hannWindow(nFft);
    final int pad = nFft ~/ 2;
    final Float32List re = Float32List(nFft);
    final Float32List im = Float32List(nFft);
    final List<Float32List> result =
        List<Float32List>.generate(bins, (_) => Float32List(frames));

    for (int f = 0; f < frames; f++) {
      final int start = f * hopLength - pad;
      for (int i = 0; i < nFft; i++) {
        final int index = start + i;
        re[i] = (index >= 0 && index < waveform.length)
            ? waveform[index] * window[i]
            : 0.0;
        im[i] = 0.0;
      }
      fft(re, im);
      for (int k = 0; k < bins; k++) {
        final double power = re[k] * re[k] + im[k] * im[k];
        result[k][f] = power.isNaN ? 0.0 : power;
      }
    }
    return result;
  }

  static const double _fMin = 0.0;
  static const double _fSp = 200.0 / 3.0;
  static const double _minLogHz = 1000.0;
  static final double _minLogMel = (_minLogHz - _fMin) / _fSp;
  static final double _logStep = math.log(6.4) / 27.0;
}
