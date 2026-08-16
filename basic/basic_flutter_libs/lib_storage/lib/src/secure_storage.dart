import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// 安全存储门面，固定 flutter_secure_storage 实现，不参与内核切换。
/// 适合保存小型敏感数据（Token、密钥、密码等），值统一按字符串序列化。
class SecureStorage {
  SecureStorage._();

  static const FlutterSecureStorage _storage = FlutterSecureStorage();

  /// 设置值
  static Future<bool> setValue(String key, Object value) async {
    await _storage.write(key: key, value: value.toString());
    return true;
  }

  /// 获取值（带默认值）
  static Future<T> getValue<T>(String key, T defaultValue) async {
    final String? value = await _storage.read(key: key);

    if (value == null) {
      return defaultValue;
    }

    if (defaultValue is int) {
      final int? parsedValue = int.tryParse(value);
      return (parsedValue ?? defaultValue) as T;
    }

    if (defaultValue is bool) {
      if (value == 'true') {
        return true as T;
      }
      if (value == 'false') {
        return false as T;
      }
      return defaultValue;
    }

    if (defaultValue is double) {
      final double? parsedValue = double.tryParse(value);
      return (parsedValue ?? defaultValue) as T;
    }

    if (defaultValue is String) {
      return value as T;
    }

    throw ArgumentError('Unsupported type: ${defaultValue.runtimeType}');
  }

  /// 移除指定 key
  static Future<bool> remove(String key) async {
    await _storage.delete(key: key);
    return true;
  }

  /// 清除全部数据
  static Future<bool> clearAll() async {
    await _storage.deleteAll();
    return true;
  }
}
