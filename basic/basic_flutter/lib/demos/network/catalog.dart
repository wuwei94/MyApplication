import 'package:basic_flutter/demos/network/dio_example.dart';
import 'package:basic_flutter/demos/network/http_example.dart';
import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// Network 模块
///
/// 包含：Dio、Http 等网络请求示例
class NetworkCatalog extends CatalogSection {
  const NetworkCatalog._();

  @override
  String get path => 'network';

  @override
  String get title => 'Network Example';

  @override
  String get subtitle => '请求发送、响应展示与异常处理';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'dio',
      title: 'Dio',
      subtitle: '封装 POST 请求、响应解析与异常处理',
      pageBuilder: (BuildContext context) => const DioDemoPage(title: 'Dio'),
    ),
    CatalogEntry.page(
      path: 'http',
      title: 'Http',
      subtitle: '基础 POST 请求与统一响应结果展示',
      pageBuilder: (BuildContext context) => const HttpDemoPage(title: 'Http'),
    ),
  ];
}

/// 单例实例
const NetworkCatalog networkCatalog = NetworkCatalog._();
