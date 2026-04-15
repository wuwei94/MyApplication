import 'package:get_storage/get_storage.dart';

/// GetX 示例中的存储工具类
/// 基于 GetStorage 实现轻量级本地存储。
class StorageUtils {
  StorageUtils._();

  static const String _container = 'getx_storage';
  static final GetStorage _storage = GetStorage(_container);
  static Future<void>? _initFuture;

  static Future<void> _ensureInitialized() {
    return _initFuture ??= GetStorage.init(_container);
  }

  /// 设置值（自动根据类型判断）
  static Future<bool> setValue(String key, Object value) async {
    await _ensureInitialized();

    if (value is int ||
        value is bool ||
        value is double ||
        value is String ||
        value is List<String>) {
      await _storage.write(key, value);
      return true;
    }

    throw ArgumentError('Unsupported type: ${value.runtimeType}');
  }

  /// 获取值（带默认值）
  static Future<T> getValue<T>(String key, T defaultValue) async {
    await _ensureInitialized();

    final value = _storage.read<dynamic>(key);

    if (value == null) {
      return defaultValue;
    }

    if (value is T) {
      return value;
    }

    return defaultValue;
  }

  /// 移除指定 key
  static Future<bool> remove(String key) async {
    await _ensureInitialized();
    await _storage.remove(key);
    return true;
  }

  /// 清除所有数据
  static Future<bool> clearAll([Set<String>? allowList]) async {
    await _ensureInitialized();

    if (allowList == null || allowList.isEmpty) {
      await _storage.erase();
      return true;
    }

    for (final key in allowList) {
      await _storage.remove(key);
    }

    return true;
  }
}
