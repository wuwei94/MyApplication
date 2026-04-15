import 'package:basic_flutter/demos/examples/counter/counter_page.dart';
import 'package:basic_flutter/demos/examples/getx/getx_example_app.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:flutter/widgets.dart';

/// Examples 模块
/// 
/// 包含：计数器示例、GetX 完整示例等
class ExamplesModule {
  const ExamplesModule._();

  /// 首页目录入口
  CatalogItem get catalog => CatalogItem.catalog(
        path: '/examples',
        title: 'Examples',
        subtitle: '基础示例',
        children: routes,
      );

  /// 所有路由列表
  List<CatalogItem> get routes => _routes;

  static final List<CatalogItem> _routes = [
    CatalogItem.page(
      path: 'counter',
      title: 'Counter Example',
      subtitle: '计数器示例',
      pageBuilder: (BuildContext context) =>
          const CounterExample(title: 'Counter Example'),
    ),
    CatalogItem.page(
      path: 'getx',
      title: 'GetX Example',
      subtitle: 'GetX示例',
      pageBuilder: (BuildContext context) => const GetXApp(title: 'GetX Example'),
    ),
  ];
}

/// 单例实例
const ExamplesModule examplesModule = ExamplesModule._();
