import 'package:basic_flutter/features/demo/custom_local_font_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Demo 模块
/// 
/// 包含：自定义字体等演示示例
class DemoModule {
  const DemoModule._();

  /// 首页目录入口
  RouteItem get catalog => RouteItem.section(
        path: '/demo',
        title: 'Demo',
        subtitle: '演示组件',
        routeItems: routes,
      );

  /// 所有路由列表
  List<RouteItem> get routes => _routes;

  static final List<RouteItem> _routes = [
    RouteItem.page(
      path: '/demo/custom-local-font',
      title: 'Custom Local Font',
      subtitle: '本地自定义字体示例',
      pageBuilder: (BuildContext context) =>
          const CustomLocalFontExample(title: 'Custom Local Font'),
    ),
  ];
}

/// 单例实例
const DemoModule demoModule = DemoModule._();
