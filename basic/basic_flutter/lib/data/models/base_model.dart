/// 基础模型接口
/// 所有数据模型都应该实现这个接口
abstract class BaseModel {
  /// 模型ID
  String get id;

  /// 转换为JSON
  Map<String, dynamic> toJson();
}

/// 模型工厂接口
/// 用于从JSON创建模型实例
abstract class ModelFactory<T extends BaseModel> {
  /// 从JSON创建模型实例
  T fromJson(Map<String, dynamic> json);
}
