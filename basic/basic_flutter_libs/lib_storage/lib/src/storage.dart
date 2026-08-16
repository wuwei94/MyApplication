import 'package:lib_storage/src/hive_storage.dart';
import 'package:lib_storage/src/i_storage.dart';

/// 键值存储统一门面，与 Android 基础层存储约定对齐。
/// 业务侧只依赖 [Storage]，切换内核仅需替换 [kernel]，调用方零改动。
/// 敏感数据统一走 [SecureStorage]，不经过本门面。
class Storage {
  Storage._();

  /// 当前内核实现，默认 Hive。
  static IStorage kernel = const HiveStorage();

  /// 设置值
  static Future<bool> setValue(String key, Object value) {
    return kernel.setValue(key, value);
  }

  /// 获取值（带默认值）
  static Future<T> getValue<T>(String key, T defaultValue) {
    return kernel.getValue<T>(key, defaultValue);
  }

  /// 移除指定 key
  static Future<bool> remove(String key) {
    return kernel.remove(key);
  }

  /// 清除全部数据
  static Future<bool> clearAll() {
    return kernel.clearAll();
  }
}
