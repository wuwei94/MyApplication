/// 基础数据源接口
/// 定义了数据源的基本操作
abstract class BaseDataSource<T> {
  /// 从本地获取数据
  Future<List<T>> getLocalData();

  /// 从远程获取数据
  Future<List<T>> getRemoteData();

  /// 保存数据到本地
  Future<void> saveToLocal(List<T> data);

  /// 清除本地数据
  Future<void> clearLocalData();
}
