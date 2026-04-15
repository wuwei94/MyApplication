import 'package:basic_flutter/demos/network/dio_example.dart';
import 'package:basic_flutter/demos/network/http_example.dart';
import 'package:basic_flutter/demos/network/image_example.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/app/catalog/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// Network 模块
/// 
/// 包含：Dio、Http、图片加载等网络请求示例
class NetworkCatalog extends CatalogSection {
  const NetworkCatalog._();

  @override
  String get path => '/network';

  @override
  String get title => 'Network';

  @override
  String get subtitle => '网络请求';

  @override
  List<CatalogItem> get items => _items;

  static final List<CatalogItem> _items = [
    CatalogItem.page(
      path: '/network/dio',
      title: 'Dio',
      subtitle: 'Dio网络请求示例',
      pageBuilder: (BuildContext context) => const DioExample(title: 'Dio'),
    ),
    CatalogItem.page(
      path: '/network/http',
      title: 'Http',
      subtitle: 'Http网络请求示例',
      pageBuilder: (BuildContext context) => const HttpExample(title: 'Http'),
    ),
    CatalogItem.page(
      path: '/network/image-loader',
      title: 'ImageLoader',
      subtitle: 'ImageLoader图片加载示例',
      pageBuilder: (BuildContext context) =>
          const ImageExample(title: 'ImageLoader'),
    ),
  ];
}

/// 单例实例
const NetworkCatalog networkCatalog = NetworkCatalog._();
