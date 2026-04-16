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
  String get subtitle => '网络请求';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'dio',
      title: 'Dio',
      subtitle: 'Dio网络请求示例',
      pageBuilder: (BuildContext context) => const DioDemoPage(title: 'Dio'),
    ),
    CatalogEntry.page(
      path: 'http',
      title: 'Http',
      subtitle: 'Http网络请求示例',
      pageBuilder: (BuildContext context) => const HttpDemoPage(title: 'Http'),
    ),
  ];
}

/// 单例实例
const NetworkCatalog networkCatalog = NetworkCatalog._();
