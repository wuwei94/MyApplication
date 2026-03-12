import 'package:shared_preferences/shared_preferences.dart';

/// SharedPreferences 工具类
/// 基于 SharedPreferencesAsync，避免缓存导致的脏读问题。
class SharedPreferencesUtils {
  SharedPreferencesUtils._();

  static final SharedPreferencesAsync _prefs = SharedPreferencesAsync();

  /// 设置值（自动根据类型判断）
  static Future<bool> setValue(String key, Object? value) async {
    if (value == null) {
      await _prefs.remove(key);
      return true;
    }

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
  static Future<T?> getValue<T>(String key, [T? defaultValue]) async {
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

  /// 设置 int 值
  static Future<bool> setInt(String key, int value) async {
    await _prefs.setInt(key, value);
    return true;
  }

  /// 获取 int 值
  static Future<int> getInt(String key, [int defaultValue = 0]) async {
    return await _prefs.getInt(key) ?? defaultValue;
  }

  /// 设置 bool 值
  static Future<bool> setBool(String key, bool value) async {
    await _prefs.setBool(key, value);
    return true;
  }

  /// 获取 bool 值
  static Future<bool> getBool(String key, [bool defaultValue = false]) async {
    return await _prefs.getBool(key) ?? defaultValue;
  }

  /// 设置 double 值
  static Future<bool> setDouble(String key, double value) async {
    await _prefs.setDouble(key, value);
    return true;
  }

  /// 获取 double 值
  static Future<double> getDouble(
    String key, [
    double defaultValue = 0.0,
  ]) async {
    return await _prefs.getDouble(key) ?? defaultValue;
  }

  /// 设置 String 值
  static Future<bool> setString(String key, String value) async {
    await _prefs.setString(key, value);
    return true;
  }

  /// 获取 String 值
  static Future<String> getString(
    String key, [
    String defaultValue = '',
  ]) async {
    return await _prefs.getString(key) ?? defaultValue;
  }

  /// 设置 StringList 值
  static Future<bool> setStringList(String key, List<String> value) async {
    await _prefs.setStringList(key, value);
    return true;
  }

  /// 获取 StringList 值
  static Future<List<String>> getStringList(
    String key, [
    List<String>? defaultValue,
  ]) async {
    return await _prefs.getStringList(key) ?? defaultValue ?? <String>[];
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

  /// 获取所有 key
  static Future<Set<String>> getKeys() async {
    return _prefs.getKeys();
  }

  /// 检查是否包含指定 key
  static Future<bool> containsKey(String key) async {
    return _prefs.containsKey(key);
  }

  /// 批量设置值
  static Future<void> setValues(Map<String, dynamic> values) async {
    for (final entry in values.entries) {
      await setValue(entry.key, entry.value);
    }
  }

  /// 批量获取值
  static Future<Map<String, dynamic>> getValues(List<String> keys) async {
    final values = await _prefs.getAll(allowList: keys.toSet());
    return Map<String, dynamic>.from(values);
  }
}
