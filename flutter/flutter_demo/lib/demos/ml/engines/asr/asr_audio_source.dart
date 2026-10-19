/// 原始音频源 —— 与 Android `:modules:module_ml` 的 `AudioSource` /
/// `FileAudioSource` / `MicrophoneAudioSource` 一一对应。
///
/// 「从哪来」（文件 / 麦克风）与「怎么切窗口」在这里解耦，上层拿到的永远是
/// **带重叠的定长块** —— 因为官方支持的所有模型都**不是流式**模型，
/// 只能靠重叠窗口拼接。
///
/// ### 与 Android 的一处结构差异：拉 → 推
///
/// Android 的麦克风源从 `AudioRecord.read()` **拉**数据；而 Flutter 的录音插件
/// （`record`）是**推**模式（`onData` 流回调），且必须跑在主 isolate（平台通道）。
/// 因此：
/// - 文件源保持「拉」（[AsrWavFileSource.next]），且整个读盘过程在后台 isolate；
/// - 麦克风源改成「推」：[AsrMicrophoneWindowSource.feed] 喂入主 isolate 转过来的
///   PCM，返回这一步能产出的窗口。窗口切分与静音门限逻辑仍与 Android 逐行对齐，
///   只是驱动方向反过来。
library;

import 'dart:io';
import 'dart:math' as math;
import 'dart:typed_data';

/// 采样数 = 采样率 × 毫秒 / 1000（与 Android `getNumSamples(Duration)` 同口径）
int samplesFor(int samplingRate, int milliseconds) =>
    samplingRate * milliseconds ~/ 1000;

/// 把插件给的 16-bit PCM **字节流**解成短整型数组。
///
/// 平台（`record` 插件的 `onData`）给的是本机字节序的 PCM16，Android 上即小端。
/// 不直接 `bytes.buffer.asInt16List()`：插件返回的 `Uint8List` 在底层缓冲区里的
/// 偏移不保证 2 字节对齐，那样取视图会抛 `RangeError`。
///
/// 放在引擎层而不是页面里，是为了让它能被纯 Dart 单测直接覆盖。
Int16List shortsFromPcm16Bytes(Uint8List bytes) {
  final int count = bytes.length ~/ 2;
  final Int16List samples = Int16List(count);
  final ByteData view =
      ByteData.view(bytes.buffer, bytes.offsetInBytes, bytes.length);
  for (int i = 0; i < count; i++) {
    samples[i] = view.getInt16(i * 2, Endian.little);
  }
  return samples;
}

/// 依次产出音频块的源；[next] 返回 `null` 表示已结束
abstract interface class AsrAudioSource {
  int get samplingRate;

  /// 每块的采样数
  int get intervalSamples;

  /// 与上一块的重叠采样数，必须严格小于 [intervalSamples]
  int get overlapSamples;

  /// 取下一块；`null` 表示结束
  Float32List? next();

  void close();
}

/// 从 **16-bit PCM WAV** 文件里按「定长块 + 重叠」读取。
///
/// 与官方 `FileAudioSource` 一致的三条行为：
/// 1. 首块读满 `interval`；其后每块只读 `interval - overlap` 个新采样，
///    缓冲区左移后与上一块的重叠部分拼成完整一块。
/// 2. 读不满时**用 0 补齐**（当静音），最后一块因此可能比 interval 短 —— 这是有意的。
/// 3. 一旦读不到新数据就**直接结束**，不处理缓冲区里剩下的重叠采样：
///    官方注释说明「那些采样已被上一块处理过，重跑只会更差」。
///
/// 与官方的一处差异：官方只校验 `fmt ` 里的采样率与数据格式，**不支持非 16-bit／非单声道**，
/// 也不处理 chunk 的对齐补齐字节。本模块沿用这一限制（演示页面对输入的要求如实写在
/// 界面上），但对「块内越界的采样数」加了一次钳位 —— 官方在这种情况下会直接抛
/// `ArrayIndexOutOfBoundsException`，这里改为截断，坏文件不至于把页面打崩。
class AsrWavFileSource implements AsrAudioSource {
  AsrWavFileSource._(
    this._handle,
    this.samplingRate,
    this.intervalSamples,
    this.overlapSamples,
  ) : _step = intervalSamples - overlapSamples {
    _buffer = Float32List(intervalSamples);
    _raw = Uint8List(intervalSamples * _bytesPerSample);
  }

  /// 打开文件并跳过除 `fmt ` / `data` 之外的 chunk，返回时句柄正停在 `data` 数据起点
  ///
  /// [samplingRate] 会被用于校验文件头 —— 全链路统一 16 kHz，文件不符时立刻报错
  /// 而不是喂进去产出乱码。
  factory AsrWavFileSource.open(
    File file, {
    required int samplingRate,
    required int intervalMilliseconds,
    required int overlapMilliseconds,
  }) {
    final int intervalSamples = samplesFor(samplingRate, intervalMilliseconds);
    final int overlapSamples = samplesFor(samplingRate, overlapMilliseconds);
    if (intervalSamples <= overlapSamples) {
      throw ArgumentError('interval（$intervalMilliseconds ms）必须严格长于 overlap（$overlapMilliseconds ms）');
    }
    final RandomAccessFile handle = file.openSync();
    try {
      _skipToDataChunk(handle, samplingRate);
      return AsrWavFileSource._(
        handle,
        samplingRate,
        intervalSamples,
        overlapSamples,
      );
    } catch (_) {
      handle.closeSync();
      rethrow;
    }
  }

  final RandomAccessFile _handle;

  @override
  final int samplingRate;

  @override
  final int intervalSamples;

  @override
  final int overlapSamples;

  final int _step;
  late final Float32List _buffer;
  late final Uint8List _raw;
  bool _firstChunk = true;
  bool _finished = false;

  static const int _bytesPerSample = 2;

  /// 16-bit PCM 归一化系数
  static const double _maxPcmValue = 32768.0;

  @override
  Float32List? next() {
    if (_finished) {
      return null;
    }
    final int bytesToRead = (_firstChunk ? intervalSamples : _step) * _bytesPerSample;
    final int readBytes = _readFully(_raw, bytesToRead);
    if (readBytes <= 0) {
      _finished = true;
      return null;
    }

    final int newSamples = readBytes ~/ _bytesPerSample;
    final int sampleIndexStart = _firstChunk ? 0 : overlapSamples;
    final int writable = intervalSamples - sampleIndexStart;
    final int copyCount = newSamples < writable ? newSamples : writable;
    final ByteData view = ByteData.view(_raw.buffer, _raw.offsetInBytes, readBytes);
    for (int i = 0; i < copyCount; i++) {
      _buffer[sampleIndexStart + i] =
          view.getInt16(i * _bytesPerSample, Endian.little) / _maxPcmValue;
    }
    for (int i = sampleIndexStart + copyCount; i < _buffer.length; i++) {
      // 读不满的部分补零：当作静音（官方同样如此）
      _buffer[i] = 0.0;
    }
    _firstChunk = false;

    final Float32List chunk = Float32List.fromList(_buffer);
    // 缓冲区左移：把尾部 step 个采样搬到最前面，供下一块拼接
    _buffer.setRange(0, overlapSamples, _buffer, _step);
    return chunk;
  }

  @override
  void close() {
    _finished = true;
    try {
      _handle.closeSync();
    } catch (_) {
      // 已关闭 / 句柄失效：释放阶段的异常不该往上冒
    }
  }

  /// 读满 [length] 字节；返回实际读到的字节数（0 表示已到末尾）
  int _readFully(Uint8List target, int length) {
    int total = 0;
    while (total < length) {
      // readIntoSync(buffer, start, end)：把数据写进 [start, end)，最多 end-start 字节
      final int read = _handle.readIntoSync(target, total, length);
      if (read <= 0) {
        break;
      }
      total += read;
    }
    return total;
  }

  /// 跳过除 `fmt ` / `data` 之外的 chunk，返回时句柄正停在 `data` 块的数据起点
  static void _skipToDataChunk(RandomAccessFile handle, int samplingRate) {
    final Uint8List riffHeader = Uint8List(12);
    _readExactly(handle, riffHeader, 12, '不是合法 WAV：文件过短');
    if (_ascii(riffHeader, 0, 4) != 'RIFF' || _ascii(riffHeader, 8, 4) != 'WAVE') {
      throw const FormatException('不是合法的 RIFF/WAVE 文件');
    }

    while (true) {
      final Uint8List chunkHeader = Uint8List(8);
      _readExactly(handle, chunkHeader, 8, '不是合法 WAV：chunk 头被截断');
      final String chunkId = _ascii(chunkHeader, 0, 4);
      final int chunkSize =
          ByteData.view(chunkHeader.buffer).getUint32(4, Endian.little);

      if (chunkId == 'fmt ') {
        final Uint8List fmtData = Uint8List(chunkSize);
        _readExactly(handle, fmtData, chunkSize, '不是合法 WAV：fmt 块被截断');
        final ByteData fmt = ByteData.view(fmtData.buffer);
        final int audioFormat = fmt.getUint16(0, Endian.little);
        if (audioFormat != 1) {
          throw FormatException('仅支持 PCM（1）音频，实际为 $audioFormat');
        }
        final int fileSampleRate = fmt.getUint32(4, Endian.little);
        if (fileSampleRate != samplingRate) {
          throw FormatException('采样率不符：要求 $samplingRate，文件为 $fileSampleRate');
        }
      } else if (chunkId == 'data') {
        return;
      } else {
        handle.setPositionSync(handle.positionSync() + chunkSize);
      }
    }
  }

  /// 读满 [length] 字节，读不满就按 [message] 报错
  static void _readExactly(
    RandomAccessFile handle,
    Uint8List target,
    int length,
    String message,
  ) {
    int total = 0;
    while (total < length) {
      final int read = handle.readIntoSync(target, total, length);
      if (read <= 0) {
        throw FormatException(message);
      }
      total += read;
    }
  }

  static String _ascii(Uint8List bytes, int start, int length) =>
      String.fromCharCodes(bytes, start, start + length);
}

/// 麦克风实时音频源（**推**模式）。
///
/// 与官方 `MicrophoneAudioSource` 一致：
/// - 固定按 `interval - overlap`（[stepSamples]）步进，缓冲区左移后产出完整一块；
/// - **每步做一次 RMS 静音判定**（阈值 0.005），判定为静音时整块写 0 ——
///   官方用这一招抑制底噪被识别成幻听文本。
///
/// 与官方「读到多少算多少」的一个细节差异：录音插件的回调分片大小由平台决定
/// （可能比一个 step 小得多），因此这里把不足一个 step 的采样攒起来
/// （[_pending]），凑够一个 step 才产出一块。块边界因此与 Android 完全一致，
/// 只是到达时间不同。
class AsrMicrophoneWindowSource implements AsrAudioSource {
  AsrMicrophoneWindowSource({
    required this.samplingRate,
    required int intervalMilliseconds,
    required int overlapMilliseconds,
  })  : intervalSamples = samplesFor(samplingRate, intervalMilliseconds),
        overlapSamples = samplesFor(samplingRate, overlapMilliseconds),
        _step = samplesFor(samplingRate, intervalMilliseconds) -
            samplesFor(samplingRate, overlapMilliseconds) {
    if (intervalSamples <= overlapSamples) {
      throw ArgumentError('interval（$intervalMilliseconds ms）必须严格长于 overlap（$overlapMilliseconds ms）');
    }
    // 多留一个 step：产出时新采样从尾部写入、随后整体左移（与 Android 同布局）
    _buffer = Float32List(intervalSamples + _step);
  }

  @override
  final int samplingRate;

  @override
  final int intervalSamples;

  @override
  final int overlapSamples;

  int get stepSamples => _step;

  final int _step;
  late final Float32List _buffer;

  /// 已收到但还不够一个 step 的采样（已归一化）
  final List<double> _pending = <double>[];

  bool _closed = false;

  static const double _maxPcmValue = 32768.0;

  /// 静音判定阈值（官方常量）
  static const double silenceThreshold = 0.005;

  /// 喂入一段原始 16-bit PCM（单声道、16 kHz），返回这一步产出的全部窗口
  ///
  /// 返回的是**长度 `interval + step`** 的缓冲（与 Android `MicrophoneAudioSource`
  /// 产出的形状一致）；超出一个模型窗口的部分由
  /// `padOrTruncateFeatures` 取尾部裁掉。
  List<Float32List> feed(Int16List samples) {
    final List<Float32List> windows = <Float32List>[];
    if (_closed || samples.isEmpty) {
      return windows;
    }
    for (final int sample in samples) {
      _pending.add(sample / _maxPcmValue);
    }
    while (_pending.length >= _step) {
      double sumSquares = 0.0;
      for (int i = 0; i < _step; i++) {
        final double sample = _pending[i];
        sumSquares += sample * sample;
      }
      final double rms = math.sqrt(sumSquares / _step);
      final bool isSilent = rms < silenceThreshold;

      final int sampleStartIndex = _buffer.length - _step;
      for (int i = 0; i < _step; i++) {
        _buffer[sampleStartIndex + i] = isSilent ? 0.0 : _pending[i];
      }
      _pending.removeRange(0, _step);

      windows.add(Float32List.fromList(_buffer));

      // 缓冲区左移一个 step
      _buffer.setRange(0, _buffer.length - _step, _buffer, _step);
    }
    return windows;
  }

  @override
  Float32List? next() => null;

  @override
  void close() {
    _closed = true;
    _pending.clear();
  }
}
