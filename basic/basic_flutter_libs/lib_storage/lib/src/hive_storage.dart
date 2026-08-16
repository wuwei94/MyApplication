import 'package:flutter/widgets.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:lib_storage/src/i_storage.dart';

/// Hive 内核实现，默认内核。
/// 适合保存本地结构化数据和轻量离线缓存。
class HiveStorage implements IStorage {
  const HiveStorage();

  static const String _defaultBoxName = 'hive_default_box';
  static Future<void>? _initFuture;
  static Box<dynamic>? _box;

  static Future<void> _ensureInitialized() {
    return _initFuture ??= _init();
  }

  static Future<void> _init() async {
    WidgetsFlutterBinding.ensureInitialized();
    await Hive.initFlutter();
    _box = await Hive.openBox<dynamic>(_defaultBoxName);
  }

  static Future<Box<dynamic>> _getBox() async {
    await _ensureInitialized();
    return _box!;
  }

  /// 测试注入：指定 Hive 数据目录并直接打开默认 Box，跳过 initFlutter 的
  /// path_provider 平台通道依赖。
  @visibleForTesting
  static Future<void> initForTesting(String path) async {
    Hive.init(path);
    _box = await Hive.openBox<dynamic>(_defaultBoxName);
    _initFuture = Future<void>.value();
  }

  @override
  Future<bool> setValue(String key, Object value) async {
    final Box<dynamic> box = await _getBox();
    await box.put(key, value);
    return true;
  }

  @override
  Future<T> getValue<T>(String key, T defaultValue) async {
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

  @override
  Future<bool> remove(String key) async {
    final Box<dynamic> box = await _getBox();
    await box.delete(key);
    return true;
  }

  @override
  Future<bool> clearAll() async {
    final Box<dynamic> box = await _getBox();
    await box.clear();
    return true;
  }
}
