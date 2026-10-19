import 'dart:io';
import 'dart:typed_data';

/// zip 中央目录里的一条目
class MlZipEntry {
  const MlZipEntry({
    required this.name,
    required this.compressedSize,
    required this.uncompressedSize,
    required this.dataOffset,
    required this.deflated,
  });

  final String name;

  /// 压缩后字节数（STORED 时等于未压缩大小）
  final int compressedSize;

  /// 未压缩字节数
  final int uncompressedSize;

  /// 条目数据在文件中的起始偏移（local header 之后）
  final int dataOffset;

  /// 是否 deflate 压缩（STORED 为 false）
  final bool deflated;
}

/// 极简 ZIP 读取器 —— 只服务端侧解析 `.npz`（NumPy 的 zip 容器）。
///
/// 与 Android 侧 `KittenVoiceTable` / `Npy` 用 `java.util.zip` 对应：Dart 核心库
/// **没有** zip 实现，而为这一个用例引入 `package:archive` 会平添一整棵依赖树，
/// 因此这里按 ZIP 规范手写读取路径（只读不解写）：
///
/// 1. **从中央目录枚举条目**（End of Central Directory → Central Directory →
///    Local Header），而不是顺序扫 local header：中央目录里每条都带
///    「压缩后 / 未压缩大小 + local header 偏移」，不依赖数据描述符（标志位 bit 3）；
/// 2. 只支持两种压缩方式：**STORED（0，直接拷）**与 **DEFLATED（8）**；
/// 3. DEFLATE 的解码走 `dart:io` 的 [ZLibDecoder]：该解码器只吃**带 2 字节头 +
///    adler32 尾**的 zlib 容器，而 zip 里存的是**裸 deflate 流**。这里补一个
///    `0x78 0x9C`（CM=8 / CINFO=7 / FLEVEL=2）头即可解 —— zlib 的 `inflate()`
///    在输入恰好在 deflate 流末尾结束时不会强校验 adler32（实测：补头不补尾
///    可正常解出全部字节；补尾反而会因校验路径差异报 "bad data"）。
///    voices.npz 本身是 `np.savez` 写的 **STORED** 条目，deflate 分支是为
///    `savez_compressed` 形态的 `.npz`（如 Qwen3-TTS 的
///    `text_projection_fp32.npz`）预留的。
class MlZipReader {
  MlZipReader(this._bytes);

  final Uint8List _bytes;

  /// 枚举全部条目（名字 -> 条目）
  Map<String, MlZipEntry> readEntries() {
    // 必须按视图自身的 offset/length 取 ByteData：调用方传进来的完全可能是
    // `Uint8List.sublistView`（npz 条目内容就是），裸 `.buffer.asByteData()`
    // 会连偏移一起忽略，字段全部读错位。
    final ByteData data =
        _bytes.buffer.asByteData(_bytes.offsetInBytes, _bytes.lengthInBytes);
    final int eocd = _findEocd();
    final int entryCount = data.getUint16(eocd + 10, Endian.little);
    final int centralDirOffset = data.getUint32(eocd + 16, Endian.little);

    final Map<String, MlZipEntry> entries = <String, MlZipEntry>{};
    int cursor = centralDirOffset;
    for (int i = 0; i < entryCount; i++) {
      if (cursor + 46 > _bytes.length ||
          data.getUint32(cursor, Endian.little) != 0x02014b50) {
        throw StateError('zip 中央目录签名不符（偏移 $cursor）');
      }
      final int method = data.getUint16(cursor + 10, Endian.little);
      if (method != 0 && method != 8) {
        throw StateError('zip 条目使用不支持的压缩方式: $method');
      }
      final int compressedSize = data.getUint32(cursor + 20, Endian.little);
      final int uncompressedSize = data.getUint32(cursor + 24, Endian.little);
      final int nameLength = data.getUint16(cursor + 28, Endian.little);
      final int extraLength = data.getUint16(cursor + 30, Endian.little);
      final int commentLength = data.getUint16(cursor + 32, Endian.little);
      final int localOffset = data.getUint32(cursor + 42, Endian.little);
      final String name = _decodeName(cursor + 46, nameLength);

      // local header：签名(4) + 版本(2) + 标志(2) + 方法(2) + 时间(2) + 日期(2)
      //   + crc(4) + csize(4) + usize(4) + nameLen(2) + extraLen(2) = 30 字节固定头
      if (localOffset + 30 > _bytes.length ||
          data.getUint32(localOffset, Endian.little) != 0x04034b50) {
        throw StateError('zip local header 签名不符（条目 $name）');
      }
      final int localNameLength = data.getUint16(localOffset + 26, Endian.little);
      final int localExtraLength = data.getUint16(localOffset + 28, Endian.little);
      final int dataOffset = localOffset + 30 + localNameLength + localExtraLength;

      entries[name] = MlZipEntry(
        name: name,
        compressedSize: compressedSize,
        uncompressedSize: uncompressedSize,
        dataOffset: dataOffset,
        deflated: method == 8,
      );
      cursor += 46 + nameLength + extraLength + commentLength;
    }
    return entries;
  }

  /// 取出并解压某个条目的内容
  Uint8List readEntry(MlZipEntry entry) {
    if (!entry.deflated) {
      return Uint8List.sublistView(
        _bytes,
        entry.dataOffset,
        entry.dataOffset + entry.compressedSize,
      );
    }
    final Uint8List framed = Uint8List(entry.compressedSize + 2);
    // zlib 容器头：CM=8（deflate）、CINFO=7（32 KB 窗口）、FLEVEL=2（默认级别）
    framed[0] = 0x78;
    framed[1] = 0x9C;
    framed.setRange(
      2,
      2 + entry.compressedSize,
      Uint8List.sublistView(_bytes, entry.dataOffset, entry.dataOffset + entry.compressedSize),
    );
    final List<int> decoded = ZLibDecoder().convert(framed);
    if (decoded.length != entry.uncompressedSize) {
      throw StateError('zip 条目 ${entry.name} 解压后长度不符：'
          '期望 ${entry.uncompressedSize}，实得 ${decoded.length}');
    }
    return Uint8List.fromList(decoded);
  }

  String _decodeName(int offset, int length) {
    final List<int> codes = <int>[];
    for (int i = 0; i < length; i++) {
      codes.add(_bytes[offset + i]);
    }
    return String.fromCharCodes(codes);
  }

  /// 定位 End of Central Directory 记录（签名 `PK\x05\x06`，注释最长 64 KB）
  int _findEocd() {
    const int minSize = 22;
    if (_bytes.length < minSize) {
      throw StateError('文件过小，不是合法 zip（${_bytes.length} 字节）');
    }
    const int maxComment = 65535;
    final int searchStart =
        _bytes.length < minSize + maxComment ? 0 : _bytes.length - minSize - maxComment;
    for (int i = _bytes.length - minSize; i >= searchStart; i--) {
      if (_bytes[i] == 0x50 &&
          _bytes[i + 1] == 0x4B &&
          _bytes[i + 2] == 0x05 &&
          _bytes[i + 3] == 0x06) {
        return i;
      }
    }
    throw StateError('zip 中找不到 End of Central Directory 记录');
  }

  /// 从文件读入并解析（内部缓冲整份文件；`.npz` 都是数 MB 级，可接受）
  static MlZipReader fromFile(File file) => MlZipReader(file.readAsBytesSync());

  /// 便捷入口：读取 `.npz`，返回 `条目名 -> 内容字节`
  static Map<String, Uint8List> readNpz(File file) {
    final MlZipReader reader = fromFile(file);
    final Map<String, Uint8List> out = <String, Uint8List>{};
    reader.readEntries().forEach((String name, MlZipEntry entry) {
      out[name] = reader.readEntry(entry);
    });
    return out;
  }
}
