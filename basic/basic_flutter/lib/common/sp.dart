import 'package:shared_preferences/shared_preferences.dart';

/// SharedPreferences 工具类
/// 使用单例模式缓存实例，避免重复调用 getInstance()
class SpUtils {
  // 缓存的 SharedPreferences 实例
  static SharedPreferences? _prefs;

  /// 初始化 SharedPreferences（应在 app 启动时调用）
  static Future<void> init() async {
    _prefs ??= await SharedPreferences.getInstance();
  }

  /// 获取 SharedPreferences 实例
  static Future<SharedPreferences> get _instance async {
    _prefs ??= await SharedPreferences.getInstance();
    return _prefs!;
  }

  /// 同步获取实例（需确保已调用 init）
  static SharedPreferences get instance {
    assert(_prefs != null, 'SpUtils 未初始化，请先调用 SpUtils.init()');
    return _prefs!;
  }

  static Future<void> setValue(String key, Object? value) async {
    final sp = await _instance;
    if (value is int) {
      await sp.setInt(key, value);
    } else if (value is bool) {
      await sp.setBool(key, value);
    } else if (value is double) {
      await sp.setDouble(key, value);
    } else if (value is String) {
      await sp.setString(key, value);
    } else if (value is List<String>) {
      await sp.setStringList(key, value);
    }
  }

  static Future<T?> getValue<T>(String key, T defaultValue) async {
    final sp = await _instance;
    if (defaultValue is int) {
      return (sp.getInt(key) ?? defaultValue) as T;
    } else if (defaultValue is double) {
      return (sp.getDouble(key) ?? defaultValue) as T;
    } else if (defaultValue is bool) {
      return (sp.getBool(key) ?? defaultValue) as T;
    } else if (defaultValue is String) {
      return (sp.getString(key) ?? defaultValue) as T;
    } else if (defaultValue is List<String>) {
      return (sp.getStringList(key) ?? defaultValue) as T;
    }
    return null;
  }

  static Future<bool> setInt(String key, int? value,
      [int defaultValue = 0]) async {
    final sp = await _instance;
    return sp.setInt(key, value ?? defaultValue);
  }

  static Future<int> getInt(String key, [int defaultValue = 0]) async {
    final sp = await _instance;
    return sp.getInt(key) ?? defaultValue;
  }

  static Future<bool> setBool(String key, bool? value,
      [bool defaultValue = false]) async {
    final sp = await _instance;
    return sp.setBool(key, value ?? defaultValue);
  }

  static Future<bool> getBool(String key, [bool defaultValue = false]) async {
    final sp = await _instance;
    return sp.getBool(key) ?? defaultValue;
  }

  static Future<bool> setDouble(String key, double? value,
      [double defaultValue = 0.0]) async {
    final sp = await _instance;
    return sp.setDouble(key, value ?? defaultValue);
  }

  static Future<double> getDouble(String key,
      [double defaultValue = 0.0]) async {
    final sp = await _instance;
    return sp.getDouble(key) ?? defaultValue;
  }

  static Future<bool> setString(String key, String? value,
      [String defaultValue = '']) async {
    final sp = await _instance;
    return sp.setString(key, value ?? defaultValue);
  }

  static Future<String> getString(String key,
      [String defaultValue = '']) async {
    final sp = await _instance;
    return sp.getString(key) ?? defaultValue;
  }

  static Future<bool> setStringList(String key, List<String> value) async {
    final sp = await _instance;
    return sp.setStringList(key, value);
  }

  static Future<List<String>> getStringList(String key) async {
    final sp = await _instance;
    return sp.getStringList(key) ?? [];
  }

  static Future<bool> remove(String key) async {
    final sp = await _instance;
    return sp.remove(key);
  }

  static Future<bool> clearAll() async {
    final sp = await _instance;
    return sp.clear();
  }

  static Future<Set<String>> getKeys() async {
    final sp = await _instance;
    return sp.getKeys();
  }

  static Future<bool> containsKey(String key) async {
    final sp = await _instance;
    return sp.containsKey(key);
  }
}
