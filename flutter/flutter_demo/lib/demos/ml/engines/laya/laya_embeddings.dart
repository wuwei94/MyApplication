/// Laya 只读 token 嵌入表 —— 与 Android `LayaEmbeddings` 对应。
///
/// 表布局：`[256000, 768]` 行主序 float16（`token_embeddings_fp16.bin`，393 MB），
/// 内存映射由调用方持有的 [RandomAccessFile] 按行随机读完成，不整份进堆。
/// 图接收聚合后的 float32 嵌入，含 PAD 行。
library;

import 'dart:io';
import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/laya/laya_json.dart';
import 'package:flutter_demo/demos/ml/engines/ml_binary.dart';

class LayaEmbeddings {
  LayaEmbeddings(File tableFile, File metadataFile) {
    final Map<String, Object?> metadata =
        layaAsObject(layaParse(metadataFile.readAsStringSync()));
    this.metadata = metadata;

    final List<Object?> rawShape = layaAsArray(metadata['shape']);
    final List<int> shape = rawShape.map((Object? value) => (value as num).toInt()).toList();
    if (shape.length != 2 || shape[0] != vocabularySize || shape[1] != width) {
      throw ArgumentError('Expected token table shape [256000, 768]，实际 $shape');
    }
    if (metadata['dtype'] != 'float16') {
      throw ArgumentError('Expected float16 token table');
    }
    if (metadata['byte_order'] != 'little') {
      throw ArgumentError('Expected little-endian token table');
    }
    if (!tableFile.existsSync() || tableFile.lengthSync() != sizeBytes) {
      throw ArgumentError(
          'Invalid token table size: ${tableFile.lengthSync()} B; expected $sizeBytes B');
    }
    final Object? declaredSize = metadata['size_bytes'];
    if (declaredSize is num && declaredSize.toInt() != sizeBytes) {
      throw ArgumentError('Token table metadata size mismatch');
    }
    final Object? sha = metadata['sha256'];
    if (sha is! String || sha.length != 64 || !RegExp(r'^[0-9a-f]{64}$').hasMatch(sha)) {
      throw ArgumentError('Invalid token table SHA256');
    }
    sha256 = sha;
    _tableFile = tableFile;
  }

  /// 多语 checkpoint 中的词表行数。
  static const int vocabularySize = 256000;

  /// 每个 token 行的 float16 值个数。
  static const int width = 768;

  /// 填充位置聚合的是真实嵌入行，而非零向量。
  static const int padId = 0;

  /// 完整的行主序 binary16 表大小。
  static const int sizeBytes = 393216000;

  /// 表布局与 checkpoint 来源。
  late final Map<String, Object?> metadata;

  /// 元数据中声明的表哈希；由安装器校验文件内容。
  late final String sha256;

  late final File _tableFile;
  RandomAccessFile? _handle;

  /// 打开句柄；批量 gather 前调用，用完 [close]。
  Future<RandomAccessFile> open() async {
    _handle ??= await _tableFile.open();
    return _handle!;
  }

  /// 填充行主序 `[1, window, 768]`；ids 之后的位置用 token id 0，而非零向量。
  ///
  /// 逐行随机读：每行 768 个 fp16 = 1536 B，窗口 256 即 256 次读。
  Future<Float32List> gather(
    List<int> ids,
    int window,
    Float32List destination,
  ) async {
    if (window < ids.length) {
      throw ArgumentError('Sequence exceeds embedding window $window');
    }
    if (destination.length != window * width) {
      throw ArgumentError('Embedding destination must contain window * 768 float32 values');
    }
    for (final int id in ids) {
      if (id < 0 || id >= vocabularySize) {
        throw ArgumentError('Token id out of range: $id');
      }
    }
    final RandomAccessFile handle = _handle ?? await open();
    final Uint8List row = Uint8List(width * 2);
    int output = 0;
    for (int position = 0; position < window; position++) {
      final int token = position < ids.length ? ids[position] : padId;
      await handle.setPosition(token * width * 2);
      int read = 0;
      while (read < row.length) {
        final int chunk = await handle.readInto(row, read, row.length - read);
        if (chunk <= 0) break;
        read += chunk;
      }
      if (read != row.length) {
        throw StateError('嵌入表读取不完整：token $token 只读到 $read 字节');
      }
      for (int i = 0; i < width; i++) {
        destination[output++] = MlNpy.halfToFloat(row[i * 2] | (row[i * 2 + 1] << 8));
      }
    }
    return destination;
  }

  /// 关闭句柄（表内容由文件系统持有，与 GC 无关）
  Future<void> close() async {
    await _handle?.close();
    _handle = null;
  }
}
