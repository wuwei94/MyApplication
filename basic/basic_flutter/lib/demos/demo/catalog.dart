import 'package:basic_flutter/demos/demo/custom_local_font_example.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/app/catalog/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// Demo 模块
/// 
/// 包含：自定义字体等演示示例
class ShowcaseCatalog extends CatalogSection {
  const ShowcaseCatalog._();

  @override
  String get path => '/demo';

  @override
  String get title => 'Demo';

  @override
  String get subtitle => '演示组件';

  @override
  List<CatalogItem> get items => _items;

  static final List<CatalogItem> _items = [
    CatalogItem.page(
      path: '/demo/custom-local-font',
      title: 'Custom Local Font',
      subtitle: '本地自定义字体示例',
      pageBuilder: (BuildContext context) =>
          const CustomLocalFontExample(title: 'Custom Local Font'),
    ),
  ];
}

/// 单例实例
const ShowcaseCatalog showcaseCatalog = ShowcaseCatalog._();
