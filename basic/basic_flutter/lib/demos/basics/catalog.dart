import 'package:basic_flutter/demos/basics/counter/counter_example.dart';
import 'package:basic_flutter/demos/basics/getx_app/getx_app.dart';
import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// Basics 模块
///
/// 包含：计数器、单例、GetX 实验应用等
class BasicsCatalog extends CatalogSection {
  const BasicsCatalog._();

  @override
  String get path => 'basics';

  @override
  String get title => 'Basics Example';

  @override
  String get subtitle => '基础示例';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'counter',
      title: 'Counter Example',
      subtitle: '计数器示例',
      pageBuilder: (BuildContext context) =>
          const CounterDemoPage(title: 'Counter Example'),
    ),
    CatalogEntry.page(
      path: 'getx-app',
      title: 'GetX Example',
      subtitle: 'GetX示例',
      pageBuilder: (BuildContext context) =>
          const GetXDemoApp(title: 'GetX Example'),
    ),
  ];
}

/// 单例实例
const BasicsCatalog basicsCatalog = BasicsCatalog._();
