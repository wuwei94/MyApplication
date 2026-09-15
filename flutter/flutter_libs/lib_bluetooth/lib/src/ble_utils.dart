import 'dart:math';
import 'dart:typed_data';

/// BLE 数据处理与协议编解码工具
class BleUtils {
  BleUtils._();

  /// 将 Byte 字节列表转换为大写的十六进制字符串
  ///
  /// 例如 [0xAA, 0x01, 0xFF] -> "AA 01 FF"
  static String bytesToHex(List<int> bytes, {String separator = ' '}) {
    return bytes
        .map((int b) => (b & 0xFF).toRadixString(16).padLeft(2, '0').toUpperCase())
        .join(separator);
  }

  /// 将十六进制字符串解析为字节列表
  ///
  /// 例如 "AA 01 FF" 或 "AA01FF" -> [0xAA, 0x01, 0xFF]
  static List<int> hexToBytes(String hex) {
    final String cleanHex = hex.replaceAll(RegExp(r'[^0-9a-fA-F]'), '');
    final List<int> bytes = <int>[];
    for (int i = 0; i < cleanHex.length; i += 2) {
      if (i + 1 < cleanHex.length) {
        final String byteStr = cleanHex.substring(i, i + 2);
        bytes.add(int.parse(byteStr, radix: 16));
      }
    }
    return bytes;
  }

  /// 依据单包有效载荷上限（MTU - 3）对长字节数据进行切片切割（Chunking）
  ///
  /// [bytes] 原始待发数据
  /// [payloadLimit] 每包允许的最大字节数（通常为 MTU - 3）
  static List<Uint8List> chunkBytes(List<int> bytes, int payloadLimit) {
    if (payloadLimit <= 0) {
      throw ArgumentError('payloadLimit must be greater than 0');
    }
    final List<Uint8List> chunks = <Uint8List>[];
    final int totalLength = bytes.length;
    for (int start = 0; start < totalLength; start += payloadLimit) {
      final int end = min(start + payloadLimit, totalLength);
      chunks.add(Uint8List.fromList(bytes.sublist(start, end)));
    }
    return chunks;
  }

  /// 计算 CRC16-CCITT 校验码（硬件通信常用）
  static int calculateCrc16(List<int> data) {
    int crc = 0xFFFF;
    for (final int b in data) {
      crc ^= (b & 0xFF) << 8;
      for (int i = 0; i < 8; i++) {
        if ((crc & 0x8000) != 0) {
          crc = ((crc << 1) ^ 0x1021) & 0xFFFF;
        } else {
          crc = (crc << 1) & 0xFFFF;
        }
      }
    }
    return crc;
  }
}
