/// Laya 宿主契约用的 JSON 助手 —— 与 Android `LayaJson` 功能对应。
///

library;

import 'dart:convert';

/// Python `json.dumps` 默认口径的序列化。
///
/// 提示词文本会进分词器，分隔符差异会改变 token 序列，因此不能直接用
/// `jsonEncode`（紧凑无空格）：Python 默认分隔符是 `', '` 与 `': '`，
/// 且默认 `ensure_ascii=True`（非 ASCII 转 `\uXXXX`）。这里逐字复刻这两点。
///
/// 其余对齐点：键按插入序输出（Dart 的 `Map` 来自 `jsonDecode` 时保序）；
/// 浮点用最短往返表示（与 Python `repr` 一致）。
String layaStringify(Object? value, {bool ensureAscii = true}) {
  final StringBuffer buffer = StringBuffer();
  _writeValue(buffer, value, ensureAscii);
  return buffer.toString();
}

void _writeValue(StringBuffer buffer, Object? value, bool ensureAscii) {
  if (value == null) {
    buffer.write('null');
  } else if (value is String) {
    _writeString(buffer, value, ensureAscii);
  } else if (value is bool) {
    buffer.write(value ? 'true' : 'false');
  } else if (value is int) {
    buffer.write(value.toString());
  } else if (value is double) {
    buffer.write(_formatDouble(value));
  } else if (value is List) {
    buffer.write('[');
    for (int i = 0; i < value.length; i++) {
      if (i > 0) buffer.write(', ');
      _writeValue(buffer, value[i], ensureAscii);
    }
    buffer.write(']');
  } else if (value is Map) {
    buffer.write('{');
    bool first = true;
    value.forEach((Object? key, Object? item) {
      if (!first) buffer.write(', ');
      first = false;
      _writeString(buffer, key.toString(), ensureAscii);
      buffer.write(': ');
      _writeValue(buffer, item, ensureAscii);
    });
    buffer.write('}');
  } else {
    // json.dumps(default=str)
    _writeString(buffer, value.toString(), ensureAscii);
  }
}

void _writeString(StringBuffer buffer, String value, bool ensureAscii) {
  buffer.write('"');
  for (int i = 0; i < value.length; i++) {
    final int code = value.codeUnitAt(i);
    switch (code) {
      case 0x22:
        buffer.write(r'\"');
      case 0x5C:
        buffer.write(r'\\');
      case 0x0A:
        buffer.write(r'\n');
      case 0x0D:
        buffer.write(r'\r');
      case 0x09:
        buffer.write(r'\t');
      case 0x08:
        buffer.write(r'\b');
      case 0x0C:
        buffer.write(r'\f');
      default:
        if (code < 0x20 || (ensureAscii && code > 0x7E)) {
          buffer.write('\\u');
          buffer.write(code.toRadixString(16).padLeft(4, '0'));
        } else {
          buffer.writeCharCode(code);
        }
    }
  }
  buffer.write('"');
}

/// Python `repr(float)` 口径：整数值带 `.0` 后缀（与 Dart `toString()` 一致）。
String _formatDouble(double value) {
  if (value.isNaN) return 'NaN';
  if (value.isInfinite) return value > 0 ? 'Infinity' : '-Infinity';
  if (value == value.roundToDouble() && value.abs() < 1e21) {
    return '${value.toInt()}.0';
  }
  return value.toString();
}

/// 解析 JSON 文本；失败时抛出带上下文的异常。
Object? layaParse(String text) => jsonDecode(text);

/// 按 key 取 Map，类型不符即抛（与 Kotlin `LayaJson.asObject` 对齐）。
Map<String, Object?> layaAsObject(Object? value) {
  if (value is Map) {
    return value.map((Object? key, Object? item) => MapEntry<String, Object?>('$key', item));
  }
  throw ArgumentError('期望 JSON 对象，实际: ${value.runtimeType}');
}

/// 按序号取 List（与 Kotlin `LayaJson.asArray` 对齐）。
List<Object?> layaAsArray(Object? value) {
  if (value is List) {
    return value.cast<Object?>().toList();
  }
  throw ArgumentError('期望 JSON 数组，实际: ${value.runtimeType}');
}
