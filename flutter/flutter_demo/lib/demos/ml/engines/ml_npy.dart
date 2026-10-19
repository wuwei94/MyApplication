import 'dart:io';
import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/ml_zip.dart';

/// 解析出的 `.npy` 头
class MlNpyHeader {
  const MlNpyHeader({
    required this.isHalf,
    required this.count,
    required this.dataOffset,
  });

  /// 元素是否为 fp16（否则 fp32）
  final bool isHalf;

  /// 元素个数（由 `shape` 各维相乘得出）
  final int count;

  /// 裸数据起始偏移
  final int dataOffset;
}

/// 极简 NumPy `.npy` / `.npz` 浮点读取器 —— 与 Android 侧 `Npy.kt` 对应。
///
/// 端侧 TTS / 分类任务有若干「宿主侧张量」是以 `.npy`（或打包成 `.npz`）分发的
/// （KittenTTS 音色表 `voices.npz`、Qwen3-TTS 的文本/编解码嵌入表）。这些文件
/// 不进模型图解析路径，因此在 Dart 侧按 NumPy 格式手写解析：
///
/// ```
/// magic `\x93NUMPY`(6) + 版本(2) + header 长度(v1.0 uint16 / v2.0 uint32)
///   + ASCII header `{'descr': '<f4', 'fortran_order': False, 'shape': (400, 256), }`
///   + 裸数据（小端）
/// ```
///
/// 只支持两种 dtype：**小端 float32（`<f4`）与 float16（`<f2`）**，与 Android
/// `Npy` 同口径。元素个数从 `shape` 推出而非 `(文件长 - 偏移) / 元素大小` 反算 ——
/// 前者能查出「文件被截断」，后者会把截断后的长度当成合法形状静默读歪。
class MlNpy {
  const MlNpy._();

  /// `.npy` magic：`\x93NUMPY`
  static const List<int> _magic = <int>[0x93, 0x4E, 0x55, 0x4D, 0x50, 0x59];

  /// 解析 `.npy` 头（[bytes] 至少覆盖整个 header；调用方传整份文件即可）
  static MlNpyHeader header(Uint8List bytes) {
    if (bytes.length <= _magic.length + 4) {
      throw StateError('.npy 过短（${bytes.length} 字节）');
    }
    for (int i = 0; i < _magic.length; i++) {
      if (bytes[i] != _magic[i]) {
        throw StateError('.npy magic 不符');
      }
    }

    final int major = bytes[6];
    final int headerLength;
    final int headerStart;
    if (major >= 2) {
      headerLength = _uint32(bytes, 8);
      headerStart = 12;
    } else {
      headerLength = _uint16(bytes, 8);
      headerStart = 10;
    }
    if (headerLength <= 0 || headerStart + headerLength > bytes.length) {
      throw StateError('.npy header 越界（长度 $headerLength，可用 ${bytes.length - headerStart}）');
    }

    final String text = String.fromCharCodes(
      Uint8List.sublistView(bytes, headerStart, headerStart + headerLength),
    );
    final bool isHalf;
    if (text.contains("'<f2'")) {
      isHalf = true;
    } else if (text.contains("'<f4'")) {
      isHalf = false;
    } else {
      throw StateError('.npy dtype 非受支持的浮点类型: $text');
    }
    if (text.contains("'fortran_order': True")) {
      throw StateError('.npy 是 fortran order，未支持');
    }

    final int shapeStart = text.indexOf("'shape':");
    if (shapeStart < 0) {
      throw StateError('.npy header 缺少 shape: $text');
    }
    final int open = text.indexOf('(', shapeStart);
    final int close = text.indexOf(')', shapeStart);
    if (open < 0 || close < 0 || close < open) {
      throw StateError('.npy header 的 shape 无法解析: $text');
    }
    int count = 1;
    for (final String part in text.substring(open + 1, close).split(',')) {
      final int? dimension = int.tryParse(part.trim());
      if (dimension != null) {
        count *= dimension;
      }
    }
    return MlNpyHeader(
      isHalf: isHalf,
      count: count,
      dataOffset: headerStart + headerLength,
    );
  }

  /// 解析已在内存里的 `.npy` 内容为 `Float32List`（fp16 逐元素上转）
  static Float32List floats(Uint8List bytes) {
    final MlNpyHeader parsed = header(bytes);
    final ByteData data = bytes.buffer.asByteData(
      bytes.offsetInBytes + parsed.dataOffset,
      bytes.length - parsed.dataOffset,
    );
    final Float32List out = Float32List(parsed.count);
    if (parsed.isHalf) {
      if (data.lengthInBytes < parsed.count * 2) {
        throw StateError('.npy 数据不足：需 ${parsed.count} 个 fp16，'
            '实际 ${data.lengthInBytes ~/ 2}');
      }
      for (int i = 0; i < parsed.count; i++) {
        out[i] = halfToFloat(data.getInt16(i * 2, Endian.little));
      }
    } else {
      if (data.lengthInBytes < parsed.count * 4) {
        throw StateError('.npy 数据不足：需 ${parsed.count} 个 fp32，'
            '实际 ${data.lengthInBytes ~/ 4}');
      }
      for (int i = 0; i < parsed.count; i++) {
        out[i] = data.getFloat32(i * 4, Endian.little);
      }
    }
    return out;
  }

  /// 从文件读入整份 `.npy` 并解析（小表走这条路径；大表走 [MlNpyHalfTable]）
  static Float32List loadFile(File file) => floats(file.readAsBytesSync());

  /// 从 `.npz`（zip 容器，每个条目一份 `.npy`）解出指定的若干数组
  ///
  /// 条目名按 NumPy 的落盘约定补 `.npy` 后缀（`savez(w1=...)` 存成 `w1.npy`），
  /// 与 Android `Npy.loadNpz` 同口径。只解 [names] 里点名的条目 —— `.npz` 里可能
  /// 只有一部分是要用的，全解会白白多占一份内存。
  static Map<String, Float32List> loadNpz(File file, List<String> names) {
    final Map<String, Uint8List> entries = MlZipReader.readNpz(file);
    final Map<String, Float32List> out = <String, Float32List>{};
    for (final String name in names) {
      final Uint8List? raw = entries['$name.npy'];
      if (raw == null) {
        throw StateError('${file.path} 里没有条目 $name.npy（现有：${entries.keys.join(', ')}）');
      }
      out[name] = floats(raw);
    }
    return out;
  }

  /// IEEE 754 半精度（int16 位型）转单精度 —— 与 Android `Npy.halfToFloat` 同算法
  static double halfToFloat(int half) {
    final int bits = half & 0xFFFF;
    final int sign = (bits & 0x8000) << 16;
    final int exponent = (bits >>> 10) & 0x1F;
    final int mantissa = bits & 0x3FF;
    if (exponent == 0) {
      if (mantissa == 0) {
        return _floatFromBits(sign);
      }
      // 非规格化数：先规格化再按正常指数编码
      int m = mantissa;
      int shift = -1;
      while ((m & 0x400) == 0) {
        m <<= 1;
        shift++;
      }
      m &= 0x3FF;
      return _floatFromBits(sign | ((127 - 15 - shift) << 23) | (m << 13));
    }
    if (exponent == 0x1F) {
      return _floatFromBits(sign | 0x7F800000 | (mantissa << 13));
    }
    return _floatFromBits(sign | ((exponent - 15 + 127) << 23) | (mantissa << 13));
  }

  /// 按 IEEE 754 位型构造 float —— 与 Kotlin `Float.fromBits` 对应
  static double _floatFromBits(int bits) {
    final ByteData view = ByteData(4);
    view.setUint32(0, bits, Endian.little);
    return view.getFloat32(0, Endian.little);
  }

  static int _uint16(Uint8List bytes, int offset) =>
      (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);

  static int _uint32(Uint8List bytes, int offset) =>
      _uint16(bytes, offset) | (_uint16(bytes, offset + 2) << 16);
}

/// 大体积 fp16 表的**按需**随机读取器
///
/// Qwen3-TTS 的文本嵌入表是 622 MB 的 fp16（`<f2`）：整份读进堆意味着 622 MB
/// 常驻，转成 fp32 还要再翻一倍 —— 而一次合成就只用其中几十行。
/// 因此这里只解析头部，数据段挂在 [RandomAccessFile] 上按行随机读。
///
/// 与 Android 侧 `Npy.mmapHalf` 的对应关系：Android 走 mmap（虚拟内存映射，
/// 不进堆、由内核按页换入）；Dart 没有等价的 mmap 入口，改用「保持文件句柄 +
/// 按需 seek/read」。代价是每次查表多一次 syscall（一行 1024~2048 个 fp16，
/// 读取在微秒量级），收益是不必为一张表付出 622 MB 常驻内存。
class MlNpyHalfTable {
  MlNpyHalfTable._(this._file, this._dataOffset, this.count);

  final RandomAccessFile _file;

  /// 裸数据段起始偏移（头部之后）
  final int _dataOffset;

  /// 元素总个数（由 `shape` 各维相乘得出）
  final int count;

  /// 打开并解析头部；**不**读数据段
  static MlNpyHalfTable open(File file) {
    if (!file.existsSync()) {
      throw StateError('文件不存在：${file.path}');
    }
    final RandomAccessFile handle = file.openSync();
    try {
      // 头部连 shape 文本一起通常几百字节，4 KB 足够覆盖整个 header
      final Uint8List head = handle.readSync(4096);
      final MlNpyHeader header = MlNpy.header(head);
      if (!header.isHalf) {
        throw StateError('${file.path} 不是 fp16 表（本读取器只服务半精度大表）');
      }
      return MlNpyHalfTable._(handle, header.dataOffset, header.count);
    } catch (_) {
      handle.closeSync();
      rethrow;
    }
  }

  /// 读 [sourceOffset] 起的 [length] 个 fp16 元素，上转为单精度写入 [out] 的 [outOffset] 处
  void readInto(Float32List out, int outOffset, int sourceOffset, int length) {
    if (sourceOffset < 0 || length < 0 || sourceOffset + length > count) {
      throw StateError('读越界：$sourceOffset + $length > $count');
    }
    if (outOffset < 0 || outOffset + length > out.length) {
      throw StateError('目标越界：$outOffset + $length > ${out.length}');
    }
    _file.setPositionSync(_dataOffset + sourceOffset * 2);
    final Uint8List raw = _file.readSync(length * 2);
    if (raw.length < length * 2) {
      throw StateError('文件被截断：需 ${length * 2} 字节，仅得 ${raw.length}');
    }
    final ByteData view = ByteData.view(raw.buffer, raw.offsetInBytes, raw.length);
    for (int i = 0; i < length; i++) {
      out[outOffset + i] = MlNpy.halfToFloat(view.getInt16(i * 2, Endian.little));
    }
  }

  void close() {
    _file.closeSync();
  }
}
