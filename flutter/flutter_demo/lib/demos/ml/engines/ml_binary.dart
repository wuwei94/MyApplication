import 'dart:io';
import 'dart:typed_data';

/// 极简 NumPy `.npy` / `.npz` 读取器 + IEEE-754 半精度转换。
///
/// 与 Android `:modules:module_ml` 的 `Npy.kt` 一一对应：
/// - 只支持两种 dtype：小端 float32（`'<f4'`）与 float16（`'<f2'`）；
/// - `.npy` 头按「magic + 版本 + 头长 + ASCII 头」解析，元素个数从 `shape`
///   推出而不是用文件长度反算 —— 前者能查出「文件被截断」；
/// - `.npz` 是 zip 容器（每个条目是一个 `.npy`），用 dart:io 的
///   [ZLibCodec(raw: true)] 做 raw inflate，**不引第三方包**；
/// - 大表（如 622 MB 的 `text_embedding_fp16.npy`）走随机读句柄按行取数，
///   不整份读进内存。
abstract final class MlNpy {
  static const List<int> _magic = <int>[0x93, 0x4E, 0x55, 0x4D, 0x50, 0x59];

  /// 解析出的 `.npy` 头：元素 dtype 是否为 fp16、元素个数、裸数据起始偏移
  static ({bool isHalf, int count, int dataOffset}) header(Uint8List bytes) {
    if (bytes.length <= _magic.length + 4) {
      throw ArgumentError('.npy 过短（${bytes.length} 字节）');
    }
    for (int i = 0; i < _magic.length; i++) {
      if (bytes[i] != _magic[i]) {
        throw ArgumentError('.npy magic 不符');
      }
    }

    final int major = bytes[6];
    final int headerLength;
    final int headerStart;
    if (major >= 2) {
      headerLength = _littleEndianInt(bytes, 8);
      headerStart = 12;
    } else {
      headerLength = _littleEndianShort(bytes, 8);
      headerStart = 10;
    }
    if (headerLength <= 0 || headerStart + headerLength > bytes.length) {
      throw ArgumentError(
          '.npy header 越界（长度 $headerLength，可用 ${bytes.length - headerStart}）');
    }

    final String text =
        String.fromCharCodes(bytes.sublist(headerStart, headerStart + headerLength));
    final bool isHalf;
    if (text.contains("'<f2'")) {
      isHalf = true;
    } else if (text.contains("'<f4'")) {
      isHalf = false;
    } else {
      throw ArgumentError('.npy dtype 非浮点：$text');
    }
    if (text.contains("'fortran_order': True")) {
      throw ArgumentError('.npy 是 fortran order，未支持');
    }

    final RegExp shapeRegex = RegExp("'shape':\\s*\\(([^)]*)\\)");
    final Match? shapeMatch = shapeRegex.firstMatch(text);
    if (shapeMatch == null) {
      throw ArgumentError('.npy header 缺少 shape：$text');
    }
    int count = 1;
    for (final String part in shapeMatch.group(1)!.split(',')) {
      final int? dimension = int.tryParse(part.trim());
      if (dimension != null) {
        count *= dimension;
      }
    }
    return (isHalf: isHalf, count: count, dataOffset: headerStart + headerLength);
  }

  /// 把整份 `.npy` 读成 [Float32List]（fp16 逐元素上转）
  static Float32List load(File file) => floats(file.readAsBytesSync());

  /// 解析**无 `.npy` 头**的裸 float32 小端数据（如 Matcha 的 `emb.bin` 音素嵌入表）
  static Float32List rawFloats(Uint8List bytes) {
    if (bytes.length % 4 != 0) {
      throw ArgumentError('裸 float32 数据长度应为 4 的倍数，实际 ${bytes.length}');
    }
    final ByteData data = ByteData.sublistView(bytes);
    final Float32List out = Float32List(bytes.length ~/ 4);
    for (int i = 0; i < out.length; i++) {
      out[i] = data.getFloat32(i * 4, Endian.little);
    }
    return out;
  }

  /// 解析已在内存里的 `.npy` 内容（也用于 `.npz` 里的单个条目）
  static Float32List floats(Uint8List bytes) {
    final ({bool isHalf, int count, int dataOffset}) parsed = header(bytes);
    final ByteData data =
        ByteData.sublistView(bytes, parsed.dataOffset, bytes.length);
    final Float32List out = Float32List(parsed.count);
    if (parsed.isHalf) {
      if (data.lengthInBytes < parsed.count * 2) {
        throw ArgumentError(
            '.npy 数据不足：需 ${parsed.count} 个 fp16，实际 ${data.lengthInBytes ~/ 2}');
      }
      for (int i = 0; i < parsed.count; i++) {
        out[i] = halfToFloat(data.getUint16(i * 2, Endian.little));
      }
    } else {
      if (data.lengthInBytes < parsed.count * 4) {
        throw ArgumentError(
            '.npy 数据不足：需 ${parsed.count} 个 fp32，实际 ${data.lengthInBytes ~/ 4}');
      }
      for (int i = 0; i < parsed.count; i++) {
        out[i] = data.getFloat32(i * 4, Endian.little);
      }
    }
    return out;
  }

  /// 内存映射式的 fp16 大表句柄。
  ///
  /// 与 Kotlin `HalfTable` 对应：按行随机读，不整份进内存。用完后必须 [close] ——
  /// 文件描述符是进程级资源，漏关会一路累积到进程结束。
  static Future<MlHalfTable> mmapHalf(File file) async {
    final int fileLength = await file.length();
    final int probeLength = fileLength < 512 ? fileLength : 512;
    final Uint8List probe =
        await file.openRead(0, probeLength).fold<BytesBuilder>(
              BytesBuilder(),
              (BytesBuilder builder, List<int> chunk) => builder..add(chunk),
            ).then((BytesBuilder builder) => builder.takeBytes());
    final ({bool isHalf, int count, int dataOffset}) parsed = header(probe);
    if (!parsed.isHalf) {
      throw ArgumentError('期望 fp16 表：${file.path}');
    }
    return MlHalfTable(file, parsed.dataOffset, parsed.count);
  }

  /// 从 `.npz`（zip 容器，每个条目是一个 `.npy`）里取出指定的若干 fp32/fp16 数组
  static Future<Map<String, Float32List>> loadNpz(
      File file, List<String> names) async {
    final Uint8List bytes = await file.readAsBytes();
    final Map<String, Float32List> out = <String, Float32List>{};
    for (final String name in names) {
      final Uint8List? entry = _readZipEntry(bytes, '$name.npy');
      if (entry == null) {
        throw ArgumentError('${file.path} 中缺少 $name.npy');
      }
      out[name] = floats(entry);
    }
    return out;
  }

  /// IEEE 754 半精度（binary16）转单精度（binary32）。
  ///
  /// 与 Kotlin `Npy.halfToFloat` 同一套位运算语义：含次正规数与 NaN 展开。
  /// 入参是 16 位原始位模式（无符号）。
  static double halfToFloat(int bits) {
    final int sign = (bits & 0x8000) << 16;
    final int exponent = (bits >>> 10) & 0x1F;
    final int mantissa = bits & 0x3FF;
    final int expanded;
    if (exponent == 0) {
      if (mantissa == 0) {
        expanded = sign;
      } else {
        // 非规格化数：先规格化再按正常指数编码
        int m = mantissa;
        int shift = -1;
        while ((m & 0x400) == 0) {
          m <<= 1;
          shift++;
        }
        m &= 0x3FF;
        expanded = sign | ((127 - 15 - shift) << 23) | (m << 13);
      }
    } else if (exponent == 0x1F) {
      expanded = sign | 0x7F800000 | (mantissa << 13);
    } else {
      expanded = sign | ((exponent - 15 + 127) << 23) | (mantissa << 13);
    }
    final ByteData data = ByteData(4);
    data.setUint32(0, expanded, Endian.little);
    return data.getFloat32(0, Endian.little);
  }

  static int _littleEndianShort(Uint8List bytes, int offset) =>
      (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);

  static int _littleEndianInt(Uint8List bytes, int offset) =>
      _littleEndianShort(bytes, offset) | (_littleEndianShort(bytes, offset + 2) << 16);

  // ─────────────────── 极简 ZIP 读取（.npz 容器） ───────────────────

  /// 从 zip 字节流中取出指定条目内容。
  ///
  /// 走「中央目录 → 本地头 → 数据」的标准路径，支持 stored(0) 与 deflate(8)：
  /// deflate 用 [ZLibCodec(raw: true)] 解压。只读不写，不处理加密/分卷等
  /// `.npz` 用不到的特性。
  static Uint8List? _readZipEntry(Uint8List bytes, String entryName) {
    // 定位 EOCD（End Of Central Directory）：签名 0x06054b50，从尾部往前找
    int eocd = -1;
    for (int i = bytes.length - 22; i >= 0; i--) {
      if (_u32(bytes, i) == 0x06054b50) {
        eocd = i;
        break;
      }
    }
    if (eocd < 0) {
      throw ArgumentError('zip 缺少 EOCD');
    }
    final int entryCount = _u16(bytes, eocd + 10);
    int centralOffset = _u32(bytes, eocd + 16);

    for (int i = 0; i < entryCount; i++) {
      if (centralOffset + 46 > bytes.length ||
          _u32(bytes, centralOffset) != 0x02014b50) {
        throw ArgumentError('zip 中央目录损坏');
      }
      final int compression = _u16(bytes, centralOffset + 10);
      final int compressedSize = _u32(bytes, centralOffset + 20);
      final int nameLength = _u16(bytes, centralOffset + 28);
      final int extraLength = _u16(bytes, centralOffset + 30);
      final int commentLength = _u16(bytes, centralOffset + 32);
      final int localOffset = _u32(bytes, centralOffset + 42);
      final String name = String.fromCharCodes(
          bytes.sublist(centralOffset + 46, centralOffset + 46 + nameLength));

      if (name == entryName) {
        if (_u32(bytes, localOffset) != 0x04034b50) {
          throw ArgumentError('zip 本地头签名不符：$name');
        }
        final int localNameLength = _u16(bytes, localOffset + 26);
        final int localExtraLength = _u16(bytes, localOffset + 28);
        final int dataStart =
            localOffset + 30 + localNameLength + localExtraLength;
        final Uint8List raw =
            bytes.sublist(dataStart, dataStart + compressedSize);
        if (compression == 0) {
          return raw;
        }
        if (compression == 8) {
          return Uint8List.fromList(ZLibCodec(raw: true).decode(raw));
        }
        throw ArgumentError('zip 不支持的压缩方式：$compression');
      }
      centralOffset += 46 + nameLength + extraLength + commentLength;
    }
    return null;
  }

  static int _u16(Uint8List bytes, int offset) =>
      (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);

  static int _u32(Uint8List bytes, int offset) =>
      _u16(bytes, offset) | (_u16(bytes, offset + 2) << 16);
}

/// 随机读 fp16 表：按行取数，[get] 返回第 [index] 行转换后的 float32。
class MlHalfTable {
  MlHalfTable(this._file, this._dataOffset, this.count);

  final File _file;
  final int _dataOffset;

  /// 元素总数（shape 各维之积）
  final int count;

  RandomAccessFile? _handle;

  Future<void> _ensureOpen() async {
    _handle ??= await _file.open();
  }

  /// 取第 [index] 个元素（fp16 上转为 float32）
  Future<double> get(int index) async {
    await _ensureOpen();
    final RandomAccessFile handle = _handle!;
    await handle.setPosition(_dataOffset + index * 2);
    final Uint8List two = await handle.read(2);
    return MlNpy.halfToFloat(two[0] | (two[1] << 8));
  }

  /// 同步取第 [index] 个元素（调用方需确保句柄已打开或自行串行化）
  double getSync(int index, RandomAccessFile handle) {
    handle.setPositionSync(_dataOffset + index * 2);
    final Uint8List two = handle.readSync(2);
    return MlNpy.halfToFloat(two[0] | (two[1] << 8));
  }

  /// 打开句柄（批量读取前先打开，随后用 [getSync] 逐行取）
  Future<RandomAccessFile> open() async {
    await _ensureOpen();
    return _handle!;
  }

  /// 关闭句柄
  Future<void> close() async {
    await _handle?.close();
    _handle = null;
  }
}
