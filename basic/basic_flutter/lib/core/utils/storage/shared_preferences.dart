import 'package:shared_preferences/shared_preferences.dart';

/// SharedPreferences 未初始化异常
class SharedPreferencesNotInitializedException implements Exception {
  final String message;
  SharedPreferencesNotInitializedException([
    this.message = 'SharedPreferencesUtils 未初始化，请先调用 SharedPreferencesUtils.init()',
  ]);

  @override
  String toString() => 'SharedPreferencesNotInitializedException: $message';
}

/// SharedPreferences 工具类
/// 统一使用异步 API，避免同步/异步混合使用导致的混淆
class SharedPreferencesUtils {
  // 缓存的 SharedPreferences 实例
  static SharedPreferences? _prefs;

  // 是否已初始化
  static bool _isInitialized = false;

  /// 初始化 SharedPreferences（应在 app 启动时调用）
  static Future<void> init() async {
    if (_isInitialized) return;
    _prefs = await SharedPreferences.getInstance();
    _isInitialized = true;
  }

  /// 检查是否已初始化
  static bool get isInitialized => _isInitialized;

  /// 获取 SharedPreferences 实例（异步）
  static Future<SharedPreferences> get _instance async {
    if (!_isInitialized) {
      await init();
    }
    return _prefs!;
  }

  /// 确保已初始化，否则抛出异常
  static void _ensureInitialized() {
    if (!_isInitialized) {
      throw SharedPreferencesNotInitializedException();
    }
  }

  /// 设置值（自动根据类型判断）
  static Future<bool> setValue(String key, Object? value) async {
    final SharedPreferences sp = await _instance;
    if (value == null) {
      return sp.remove(key);
    } else if (value is int) {
      return sp.setInt(key, value);
    } else if (value is bool) {
      return sp.setBool(key, value);
    } else if (value is double) {
      return sp.setDouble(key, value);
    } else if (value is String) {
      return sp.setString(key, value);
    } else if (value is List<String>) {
      return sp.setStringList(key, value);
    }
    throw ArgumentError('Unsupported type: ${value.runtimeType}');
  }

  /// 获取值（带默认值）
  static Future<T?> getValue<T>(String key, [T? defaultValue]) async {
    final SharedPreferences sp = await _instance;
    final value = sp.get(key);
    if (value == null) return defaultValue;
    if (value is T) return value as T;
    return defaultValue;
  }

  /// 设置 int 值
  static Future<bool> setInt(
    String key,
    int value,
  ) async {
    final SharedPreferences sp = await _instance;
    return sp.setInt(key, value);
  }

  /// 获取 int 值
  static Future<int> getInt(String key, [int defaultValue = 0]) async {
    final SharedPreferences sp = await _instance;
    return sp.getInt(key) ?? defaultValue;
  }

  /// 设置 bool 值
  static Future<bool> setBool(
    String key,
    bool value,
  ) async {
    final SharedPreferences sp = await _instance;
    return sp.setBool(key, value);
  }

  /// 获取 bool 值
  static Future<bool> getBool(String key, [bool defaultValue = false]) async {
    final SharedPreferences sp = await _instance;
    return sp.getBool(key) ?? defaultValue;
  }

  /// 设置 double 值
  static Future<bool> setDouble(
    String key,
    double value,
  ) async {
    final SharedPreferences sp = await _instance;
    return sp.setDouble(key, value);
  }

  /// 获取 double 值
  static Future<double> getDouble(
    String key, [
    double defaultValue = 0.0,
  ]) async {
    final SharedPreferences sp = await _instance;
    return sp.getDouble(key) ?? defaultValue;
  }

  /// 设置 String 值
  static Future<bool> setString(
    String key,
    String value,
  ) async {
    final SharedPreferences sp = await _instance;
    return sp.setString(key, value);
  }

  /// 获取 String 值
  static Future<String> getString(
    String key, [
    String defaultValue = '',
  ]) async {
    final SharedPreferences sp = await _instance;
    return sp.getString(key) ?? defaultValue;
  }

  /// 设置 StringList 值
  static Future<bool> setStringList(String key, List<String> value) async {
    final SharedPreferences sp = await _instance;
    return sp.setStringList(key, value);
  }

  /// 获取 StringList 值
  static Future<List<String>> getStringList(
    String key, [
    List<String>? defaultValue,
  ]) async {
    final SharedPreferences sp = await _instance;
    return sp.getStringList(key) ?? defaultValue ?? [];
  }

  /// 移除指定 key
  static Future<bool> remove(String key) async {
    final SharedPreferences sp = await _instance;
    return sp.remove(key);
  }

  /// 清除所有数据
  static Future<bool> clearAll() async {
    final SharedPreferences sp = await _instance;
    return sp.clear();
  }

  /// 获取所有 key
  static Future<Set<String>> getKeys() async {
    final SharedPreferences sp = await _instance;
    return sp.getKeys();
  }

  /// 检查是否包含指定 key
  static Future<bool> containsKey(String key) async {
    final SharedPreferences sp = await _instance;
    return sp.containsKey(key);
  }

  /// 批量设置值
  static Future<void> setValues(Map<String, dynamic> values) async {
    final SharedPreferences sp = await _instance;
    for (final entry in values.entries) {
      await setValue(entry.key, entry.value);
    }
  }

  /// 批量获取值
  static Future<Map<String, dynamic>> getValues(List<String> keys) async {
    final SharedPreferences sp = await _instance;
    final Map<String, dynamic> result = {};
    for (final key in keys) {
      result[key] = sp.get(key);
    }
    return result;
  }
}
