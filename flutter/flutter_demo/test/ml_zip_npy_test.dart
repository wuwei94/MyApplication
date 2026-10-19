import 'dart:io';
import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/ml_npy.dart';
import 'package:flutter_demo/demos/ml/engines/ml_zip.dart';
import 'package:test/test.dart';

/// `MlNpy` / `MlZipReader` 的对拍测试。
///
/// 全部 fixture 在内存里合成（.npy 头 + .zip 三张表手写），不依赖仓库外部文件：
/// 这两个解析器服务的是「运行时下载的宿主张量」（voices.npz / 文本嵌入表），
/// 解析错一位的后果是音色表整行偏移 —— 这类错误真机听感上只会表现为
/// 「音色怪异」，极难定位，因此用测试把格式契约钉死。
void main() {
  group('MlNpy.halfToFloat', () {
    // IEEE 754 半精度已知值（含非规格化、规格化边界、无穷）
    final Map<int, double> known = <int, double>{
      0x0000: 0.0,
      0x8000: -0.0,
      0x3C00: 1.0,
      0xBC00: -1.0,
      0x3800: 0.5,
      0x3555: 0.333251953125,
      0x0001: 5.960464477539063e-8, // 最小非规格化正数 2^-24
      0x0400: 6.103515625e-5, // 最小规格化正数 2^-14
      0x7BFF: 65504.0, // 最大有限值
      0x7C00: double.infinity,
      0xFC00: double.negativeInfinity,
      0x03FF: 6.097555160522461e-5, // 最大非规格化 (1023/1024)·2^-14
    };
    known.forEach((int bits, double expected) {
      test('0x${bits.toRadixString(16).padLeft(4, '0').toUpperCase()}', () {
        expect(MlNpy.halfToFloat(bits), expected);
      });
    });

    test('NaN 位型返回 NaN（不论 payload）', () {
      expect(MlNpy.halfToFloat(0x7E00).isNaN, isTrue);
      expect(MlNpy.halfToFloat(0xFE01).isNaN, isTrue);
    });

    test('fp16 往返 fp32 已知换算（0x3555 = 1365/4096）', () {
      // 0x3555：指数位 13、尾数 341 → (1 + 341/1024)·2^-2 = 1365/4096，
      // 该值在 fp16 与 fp32 里都可精确表示，往返无损
      expect(MlNpy.halfToFloat(0x3555), 1365 / 4096);
    });
  });

  group('MlNpy.floats', () {
    Uint8List buildNpy({
      required bool isHalf,
      required List<double> values,
      bool v2 = false,
      int shape = 0, // 0 = 一维；否则二维 (shape, values.length ~/ shape)
    }) {
      final List<int> dict = isHalf
          ? "'{'descr': '<f2', 'fortran_order': False, 'shape': ${_shapeText(values.length, shape)}, }'"
              .codeUnits
          : "'{'descr': '<f4', 'fortran_order': False, 'shape': ${_shapeText(values.length, shape)}, }'"
              .codeUnits;
      final int headerLength = _padTo64(dict.length);
      final ByteData out = ByteData(
        (v2 ? 12 : 10) + headerLength + values.length * (isHalf ? 2 : 4),
      );
      const List<int> magic = <int>[0x93, 0x4E, 0x55, 0x4D, 0x50, 0x59];
      for (int i = 0; i < 6; i++) {
        out.setUint8(i, magic[i]);
      }
      out.setUint8(6, v2 ? 2 : 1);
      out.setUint8(7, 0);
      if (v2) {
        out.setUint32(8, headerLength, Endian.little);
      } else {
        out.setUint16(8, headerLength, Endian.little);
      }
      final int headerStart = v2 ? 12 : 10;
      for (int i = 0; i < headerLength; i++) {
        out.setUint8(headerStart + i, i < dict.length ? dict[i] : 0x20);
      }
      out.setUint8(headerStart + headerLength - 1, 0x0A); // numpy 以换行收尾
      int offset = headerStart + headerLength;
      for (final double value in values) {
        if (isHalf) {
          out.setInt16(offset, _floatToHalf(value), Endian.little);
          offset += 2;
        } else {
          out.setFloat32(offset, value, Endian.little);
          offset += 4;
        }
      }
      return out.buffer.asUint8List();
    }

    test('fp32 v1.0 一维按位解析', () {
      final Float32List floats = MlNpy.floats(buildNpy(
        isHalf: false,
        values: const <double>[0.5, -1.25, 3.14159, 1e-8, -0.0],
      ));
      expect(floats.length, 5);
      expect(floats[0], 0.5);
      expect(floats[1], -1.25);
      expect(floats[2], closeTo(3.14159, 1e-6));
      // fp32 存不下 double 的 1e-8，读回的是 fp32 最近值
      expect(floats[3], closeTo(1e-8, 1e-15));
      expect(floats[4].isNaN, isFalse);
      expect(floats[4].isNegative, isTrue); // -0.0 保留符号
      expect(floats[4] == 0.0, isTrue); // -0.0 == 0.0
    });

    test('fp32 二维 shape 各维相乘得到元素数', () {
      final List<double> values =
          List<double>.generate(12, (int i) => i * 0.25);
      final Float32List floats = MlNpy.floats(buildNpy(
        isHalf: false,
        values: values,
        shape: 3,
      ));
      expect(floats.length, 12);
      expect(floats[11], 2.75);
    });

    test('fp16 逐元素上转', () {
      final Float32List floats = MlNpy.floats(buildNpy(
        isHalf: true,
        values: const <double>[1.0, -0.5, 0.333251953125, 65504.0],
      ));
      expect(floats.length, 4);
      expect(floats[0], 1.0);
      expect(floats[1], -0.5);
      expect(floats[2], 0.333251953125);
      expect(floats[3], 65504.0);
    });

    test('v2.0 头（uint32 长度）同样可解析', () {
      final Float32List floats = MlNpy.floats(buildNpy(
        isHalf: false,
        values: const <double>[1.5, 2.5],
        v2: true,
      ));
      expect(floats, <double>[1.5, 2.5]);
    });

    test('数据被截断时抛 StateError（不静默按长度反算）', () {
      final Uint8List full = buildNpy(
        isHalf: false,
        values: const <double>[1.0, 2.0, 3.0, 4.0],
      );
      final Uint8List truncated = Uint8List.sublistView(full, 0, full.length - 6);
      expect(() => MlNpy.floats(truncated), throwsStateError);
    });

    test('magic 不符抛 StateError', () {
      final Uint8List bad = Uint8List.fromList(<int>[
        0x00,
        0x4E,
        0x55,
        0x4D,
        0x50,
        0x59,
        1,
        0,
        118,
        0,
        ...List<int>.filled(118, 0x20),
        ...List<int>.filled(16, 0),
      ]);
      expect(() => MlNpy.floats(bad), throwsStateError);
    });

    test('fortran order 拒绝解析', () {
      final List<int> dict =
          "{'descr': '<f4', 'fortran_order': True, 'shape': (2,), }".codeUnits;
      final int headerLength = _padTo64(dict.length);
      final ByteData out = ByteData(10 + headerLength + 8);
      const List<int> magic = <int>[0x93, 0x4E, 0x55, 0x4D, 0x50, 0x59];
      for (int i = 0; i < 6; i++) {
        out.setUint8(i, magic[i]);
      }
      out.setUint8(6, 1);
      out.setUint16(8, headerLength, Endian.little);
      for (int i = 0; i < headerLength; i++) {
        out.setUint8(10 + i, i < dict.length ? dict[i] : 0x20);
      }
      expect(() => MlNpy.floats(out.buffer.asUint8List()), throwsStateError);
    });
  });

  group('MlZipReader', () {
    /// 手写 zip：STORED 与 DEFLATED 两种条目混合，再挂中央目录 + EOCD
    Uint8List buildZip(Map<String, List<int>> payloads,
        {Set<String> deflate = const <String>{}}) {
      final Map<String, List<int>> stored = <String, List<int>>{};
      payloads.forEach((String name, List<int> data) {
        stored[name] =
            deflate.contains(name) ? _rawDeflate(data) : data;
      });

      final BytesBuilder local = BytesBuilder(copy: false);
      final Map<String, int> offsets = <String, int>{};
      stored.forEach((String name, List<int> body) {
        offsets[name] = local.length;
        final List<int> nameBytes = name.codeUnits;
        local.add(_u32(0x04034b50));
        local.add(_u16(20)); // version needed
        local.add(_u16(0)); // flags
        local.add(_u16(deflate.contains(name) ? 8 : 0)); // method
        local.add(_u16(0)); // time
        local.add(_u16(0)); // date
        local.add(_u32(_crc32(payloads[name]!)));
        local.add(_u32(body.length));
        local.add(_u32(payloads[name]!.length));
        local.add(_u16(nameBytes.length));
        local.add(_u16(0)); // extra len
        local.add(nameBytes);
        local.add(body);
      });

      final BytesBuilder central = BytesBuilder(copy: false);
      stored.forEach((String name, List<int> body) {
        final List<int> nameBytes = name.codeUnits;
        central.add(_u32(0x02014b50));
        central.add(_u16(20)); // version made by
        central.add(_u16(20)); // version needed
        central.add(_u16(0)); // flags
        central.add(_u16(deflate.contains(name) ? 8 : 0));
        central.add(_u16(0));
        central.add(_u16(0));
        central.add(_u32(_crc32(payloads[name]!)));
        central.add(_u32(body.length));
        central.add(_u32(payloads[name]!.length));
        central.add(_u16(nameBytes.length));
        central.add(_u16(0)); // extra
        central.add(_u16(0)); // comment
        central.add(_u16(0)); // disk number
        central.add(_u16(0)); // internal attrs
        central.add(_u32(0)); // external attrs
        central.add(_u32(offsets[name]!));
        central.add(nameBytes);
      });

      final BytesBuilder all = BytesBuilder(copy: false);
      // takeBytes() 会清空 builder，local 长度必须先捕获
      final int localBytes = local.length;
      all.add(local.takeBytes());
      all.add(central.takeBytes());
      all.add(_u32(0x06054b50));
      all.add(_u16(0)); // disk
      all.add(_u16(0)); // central dir start disk
      all.add(_u16(stored.length)); // entries on this disk
      all.add(_u16(stored.length)); // total entries
      all.add(_u32(0)); // central dir size（下面按实际字节重算）
      all.add(_u32(0)); // central dir offset
      all.add(_u16(0)); // comment length
      final Uint8List bytes = all.takeBytes();
      final int realCentralStart = localBytes;
      final int realCentralSize = bytes.length - 22 - realCentralStart;
      // 经字节下标回写，不经 `.buffer`：takeBytes() 可能返回底层 chunk 的
      // 偏移视图，`.buffer.asByteData()` 会写到视图以外的位置
      _putU32(bytes, bytes.length - 10, realCentralSize);
      _putU32(bytes, bytes.length - 6, realCentralStart);
      return bytes;
    }

    test('STORED 条目原样取出', () {
      final Uint8List zip = buildZip(<String, List<int>>{
        'a.npy': List<int>.generate(300, (int i) => i % 256),
        'b.npy': <int>[1, 2, 3],
      });
      final Map<String, MlZipEntry> entries =
          MlZipReader(zip).readEntries();
      expect(entries.keys.toList()..sort(), <String>['a.npy', 'b.npy']);
      final Uint8List a = MlZipReader(zip).readEntry(entries['a.npy']!);
      expect(a.length, 300);
      expect(a[299], 299 % 256);
      expect(entries['b.npy']!.uncompressedSize, 3);
      expect(entries['b.npy']!.deflated, isFalse);
    });

    test('DEFLATED 条目解压还原（zlib 补头路径）', () {
      // 内容取可压缩但非平凡的模式，确保 deflate 流真的产生多段
      final List<int> data =
          List<int>.generate(4096, (int i) => (i * 31 + (i >> 8)) % 256);
      final Uint8List zip = buildZip(<String, List<int>>{
        'voice.npy': data,
      }, deflate: <String>{'voice.npy'});
      final MlZipReader reader = MlZipReader(zip);
      final MlZipEntry entry = reader.readEntries()['voice.npy']!;
      expect(entry.deflated, isTrue);
      expect(entry.compressedSize < entry.uncompressedSize, isTrue);
      final Uint8List out = reader.readEntry(entry);
      expect(out.length, data.length);
      for (int i = 0; i < data.length; i++) {
        expect(out[i], data[i], reason: '偏移 $i 处不符');
      }
    });

    test('npz 里的 .npy 可继续被 MlNpy 解析（端到端）', () {
      final List<double> values = <double>[0.25, -0.75, 1.5];
      final Uint8List npy = _buildNpyBytes(values);
      final Directory dir = Directory.systemTemp.createTempSync('ml_zip_test');
      try {
        final File file = File('${dir.path}/voices.npz');
        file.writeAsBytesSync(buildZip(<String, List<int>>{
          'expr-voice-5-m.npy': npy,
        }));
        final Map<String, Uint8List> read = MlZipReader.readNpz(file);
        expect(read.keys.single, 'expr-voice-5-m.npy');
        expect(MlNpy.floats(read.values.single), values);
      } finally {
        dir.deleteSync(recursive: true);
      }
    });

    test('非 zip 内容抛 StateError', () {
      expect(
        () => MlZipReader(Uint8List.fromList(List<int>.filled(64, 0x41)))
            .readEntries(),
        throwsStateError,
      );
    });
  });
}

String _shapeText(int count, int dims) {
  if (dims == 0) {
    return '($count,)';
  }
  return '($dims, ${count ~/ dims})';
}

/// header 按 numpy 惯例补齐到 64 字节对齐（含末尾换行）
int _padTo64(int dictLength) {
  int length = dictLength + 1; // +1 给换行
  while ((10 + length) % 64 != 0) {
    length++;
  }
  return length;
}

/// fp32 -> fp16 位型（只用于 fixture 构造；就近实现避免引测试助手）
int _floatToHalf(double value) {
  final Float32List f = Float32List(1)..[0] = value;
  final Uint8List b = f.buffer.asUint8List();
  final int bits = (b[3] << 24) | (b[2] << 16) | (b[1] << 8) | b[0];
  final int sign = (bits >>> 16) & 0x8000;
  final int exponent = (bits >>> 23) & 0xFF;
  int mantissa = bits & 0x7FFFFF;
  if (exponent == 0xFF) {
    return sign | 0x7C00 | (mantissa == 0 ? 0 : 0x0200);
  }
  final int halfExponent = exponent - 127 + 15;
  if (halfExponent >= 0x1F) {
    return sign | 0x7C00; // 溢出到 inf
  }
  if (halfExponent <= 0) {
    if (halfExponent < -10) {
      return sign; // 下溢
    }
    mantissa |= 0x800000;
    final int shift = 14 - halfExponent;
    final int half = mantissa >> shift;
    // 舍入到偶数（与 numpy 一致）
    final int remainder = mantissa & ((1 << shift) - 1);
    final int halfway = 1 << (shift - 1);
    int result = half;
    if (remainder > halfway || (remainder == halfway && (half & 1) == 1)) {
      result++;
    }
    return sign | result;
  }
  final int half = (halfExponent << 10) | (mantissa >> 13);
  final int remainder = mantissa & 0x1FFF;
  int result = half;
  if (remainder > 0x1000 || (remainder == 0x1000 && (half & 1) == 1)) {
    result++;
  }
  return sign | result;
}

Uint8List _buildNpyBytes(List<double> values) {
  final List<int> dict =
      "{'descr': '<f4', 'fortran_order': False, 'shape': (${values.length},), }"
          .codeUnits;
  final int headerLength = _padTo64(dict.length);
  final ByteData out = ByteData(10 + headerLength + values.length * 4);
  const List<int> magic = <int>[0x93, 0x4E, 0x55, 0x4D, 0x50, 0x59];
  for (int i = 0; i < 6; i++) {
    out.setUint8(i, magic[i]);
  }
  out.setUint8(6, 1);
  out.setUint8(7, 0);
  out.setUint16(8, headerLength, Endian.little);
  for (int i = 0; i < headerLength; i++) {
    out.setUint8(10 + i, i < dict.length ? dict[i] : 0x20);
  }
  out.setUint8(10 + headerLength - 1, 0x0A);
  for (int i = 0; i < values.length; i++) {
    out.setFloat32(10 + headerLength + i * 4, values[i], Endian.little);
  }
  return out.buffer.asUint8List();
}

/// zlib 容器剥掉 2 字节头与 4 字节 adler32 尾，得到 zip 用的裸 deflate
List<int> _rawDeflate(List<int> data) {
  final Uint8List zlib = ZLibCodec().encode(data) as Uint8List;
  return Uint8List.sublistView(zlib, 2, zlib.length - 4);
}

List<int> _u16(int value) => <int>[value & 0xFF, (value >> 8) & 0xFF];

/// 按字节下标写小端 uint32（视图安全：不触碰 `.buffer`）
void _putU32(Uint8List bytes, int at, int value) {
  bytes[at] = value & 0xFF;
  bytes[at + 1] = (value >> 8) & 0xFF;
  bytes[at + 2] = (value >> 16) & 0xFF;
  bytes[at + 3] = (value >> 24) & 0xFF;
}

List<int> _u32(int value) => <int>[
      value & 0xFF,
      (value >> 8) & 0xFF,
      (value >> 16) & 0xFF,
      (value >> 24) & 0xFF,
    ];

int _crc32(List<int> data) {
  int crc = 0xFFFFFFFF;
  for (final int byte in data) {
    crc ^= byte;
    for (int bit = 0; bit < 8; bit++) {
      crc = (crc & 1) != 0 ? (crc >>> 1) ^ 0xEDB88320 : crc >>> 1;
    }
  }
  return (crc ^ 0xFFFFFFFF) & 0xFFFFFFFF;
}
