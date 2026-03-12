/// 基础仓库接口
/// 定义了数据仓库的基本操作
abstract class BaseRepository<T> {
  /// 获取所有数据
  Future<List<T>> getAll();

  /// 根据ID获取数据
  Future<T?> getById(String id);

  /// 创建数据
  Future<T> create(T item);

  /// 更新数据
  Future<T> update(String id, T item);

  /// 删除数据
  Future<void> delete(String id);
}
