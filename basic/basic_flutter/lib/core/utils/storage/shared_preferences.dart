import 'package:shared_preferences/shared_preferences.dart';

/// SharedPreferences 工具类
/// 基于 SharedPreferencesAsync，适合保存普通本地配置和非敏感数据。
class SharedPreferencesUtils {
  SharedPreferencesUtils._();

  static final SharedPreferencesAsync _prefs = SharedPreferencesAsync();

  /// 设置值（自动根据类型判断）
  static Future<bool> setValue(String key, Object value) async {
    if (value is int) {
      await _prefs.setInt(key, value);
      return true;
    }

    if (value is bool) {
      await _prefs.setBool(key, value);
      return true;
    }

    if (value is double) {
      await _prefs.setDouble(key, value);
      return true;
    }

    if (value is String) {
      await _prefs.setString(key, value);
      return true;
    }

    if (value is List<String>) {
      await _prefs.setStringList(key, value);
      return true;
    }

    throw ArgumentError('Unsupported type: ${value.runtimeType}');
  }

  /// 获取值（带默认值）
  static Future<T> getValue<T>(String key, T defaultValue) async {
    final values = await _prefs.getAll(allowList: <String>{key});
    final value = values[key];

    if (value == null) {
      return defaultValue;
    }

    if (value is T) {
      return value as T;
    }

    return defaultValue;
  }

  /// 移除指定 key
  static Future<bool> remove(String key) async {
    await _prefs.remove(key);
    return true;
  }

  /// 清除所有数据
  static Future<bool> clearAll([Set<String>? allowList]) async {
    await _prefs.clear(allowList: allowList);
    return true;
  }
}
