import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/basics/counter/counter_demo.dart';
import 'package:flutter_demo/demos/basics/getx_app/getx_app.dart';

/// Basics 模块
///
/// 包含：计数器、单例、GetX 实验应用等
class BasicsCatalog extends CatalogSection {
  const BasicsCatalog._();

  @override
  String get path => 'basics';

  @override
  String get title => 'Basics';

  @override
  String get subtitle => '计数器入门与 GetX 小应用';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'counter',
      title: 'Counter',
      subtitle: 'Flutter 默认计数器与 setState 刷新',
      pageBuilder: (BuildContext context) =>
          const CounterDemoPage(title: 'Counter'),
    ),
    CatalogEntry.page(
      path: 'getx-app',
      title: 'GetX',
      subtitle: '路由、状态、国际化与依赖注入小应用',
      pageBuilder: (BuildContext context) => const GetXDemoApp(title: 'GetX'),
    ),
  ];
}

/// 单例实例
const BasicsCatalog basicsCatalog = BasicsCatalog._();
