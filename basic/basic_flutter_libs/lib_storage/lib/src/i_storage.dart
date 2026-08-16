/// 键值存储统一接口。
/// 业务侧只依赖 [Storage] 门面，切换内核仅需替换 [Storage.kernel]，调用方零改动。
/// 敏感数据不经过本接口，统一走 [SecureStorage]。
abstract class IStorage {
  /// 设置值
  Future<bool> setValue(String key, Object value);

  /// 获取值（带默认值）
  Future<T> getValue<T>(String key, T defaultValue);

  /// 移除指定 key
  Future<bool> remove(String key);

  /// 清除全部数据
  Future<bool> clearAll();
}
