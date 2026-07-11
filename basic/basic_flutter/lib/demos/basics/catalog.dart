import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:basic_flutter/demos/basics/counter/counter_example.dart';
import 'package:basic_flutter/demos/basics/getx_app/getx_app.dart';
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
  String get subtitle => '计数器入门与 GetX 小应用';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'counter',
      title: 'Counter Example',
      subtitle: 'Flutter 默认计数器与 setState 刷新',
      pageBuilder: (BuildContext context) =>
          const CounterDemoPage(title: 'Counter Example'),
    ),
    CatalogEntry.page(
      path: 'getx-app',
      title: 'GetX Example',
      subtitle: '路由、状态、国际化与依赖注入小应用',
      pageBuilder: (BuildContext context) =>
          const GetXDemoApp(title: 'GetX Example'),
    ),
  ];
}

/// 单例实例
const BasicsCatalog basicsCatalog = BasicsCatalog._();
