import 'package:flutter/widgets.dart';
import 'package:hive_flutter/hive_flutter.dart';

/// Hive 工具类
/// 适合保存本地结构化数据和轻量离线缓存。
class HiveStorageUtils {
  HiveStorageUtils._();

  static const String _defaultBoxName = 'hive_default_box';
  static Future<void>? _initFuture;
  static Box<dynamic>? _box;

  static Future<void> _ensureInitialized() {
    return _initFuture ??= _initHive();
  }

  static Future<void> _initHive() async {
    WidgetsFlutterBinding.ensureInitialized();
    await Hive.initFlutter();
    _box = await Hive.openBox<dynamic>(_defaultBoxName);
  }

  static Future<Box<dynamic>> _getBox() async {
    await _ensureInitialized();
    return _box!;
  }

  /// 设置值
  static Future<bool> setValue(String key, Object value) async {
    final Box<dynamic> box = await _getBox();
    await box.put(key, value);
    return true;
  }

  /// 获取值（带默认值）
  static Future<T> getValue<T>(String key, T defaultValue) async {
    final Box<dynamic> box = await _getBox();
    final dynamic value = box.get(key);

    if (value == null) {
      return defaultValue;
    }

    if (value is T) {
      return value;
    }

    if (defaultValue is List<String> && value is List<dynamic>) {
      return value.cast<String>() as T;
    }

    return defaultValue;
  }

  /// 移除指定 key
  static Future<bool> remove(String key) async {
    final Box<dynamic> box = await _getBox();
    await box.delete(key);
    return true;
  }

  /// 清除所有数据
  static Future<bool> clearAll() async {
    final Box<dynamic> box = await _getBox();
    await box.clear();
    return true;
  }
}
