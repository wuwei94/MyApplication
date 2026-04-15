import 'package:basic_flutter/demos/demo/custom_local_font_example.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:flutter/widgets.dart';

/// Demo 模块
/// 
/// 包含：自定义字体等演示示例
class DemoModule {
  const DemoModule._();

  /// 首页目录入口
  CatalogItem get catalog => CatalogItem.catalog(
        path: '/demo',
        title: 'Demo',
        subtitle: '演示组件',
        children: routes,
      );

  /// 所有路由列表
  List<CatalogItem> get routes => _routes;

  static final List<CatalogItem> _routes = [
    CatalogItem.page(
      path: 'custom-local-font',
      title: 'Custom Local Font',
      subtitle: '本地自定义字体示例',
      pageBuilder: (BuildContext context) =>
          const CustomLocalFontExample(title: 'Custom Local Font'),
    ),
  ];
}

/// 单例实例
const DemoModule demoModule = DemoModule._();
