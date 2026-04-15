import 'package:basic_flutter/demos/examples/counter/counter_page.dart';
import 'package:basic_flutter/demos/examples/getx/getx_example_app.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/app/catalog/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// Examples 模块
/// 
/// 包含：计数器示例、GetX 完整示例等
class ExamplesCatalog extends CatalogSection {
  const ExamplesCatalog._();

  @override
  String get path => '/examples';

  @override
  String get title => 'Examples';

  @override
  String get subtitle => '基础示例';

  @override
  List<CatalogItem> get items => _items;

  static final List<CatalogItem> _items = [
    CatalogItem.page(
      path: '/examples/counter',
      title: 'Counter Example',
      subtitle: '计数器示例',
      pageBuilder: (BuildContext context) =>
          const CounterExample(title: 'Counter Example'),
    ),
    CatalogItem.page(
      path: '/examples/getx',
      title: 'GetX Example',
      subtitle: 'GetX示例',
      pageBuilder: (BuildContext context) => const GetXApp(title: 'GetX Example'),
    ),
  ];
}

/// 单例实例
const ExamplesCatalog examplesCatalog = ExamplesCatalog._();
