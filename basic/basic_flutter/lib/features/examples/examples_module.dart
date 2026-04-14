import 'package:basic_flutter/features/examples/counter/counter_page.dart';
import 'package:basic_flutter/features/examples/getx/getx_example_app.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Examples 模块
/// 
/// 包含：计数器示例、GetX 完整示例等
class ExamplesModule {
  const ExamplesModule._();

  /// 首页目录入口
  RouteItem get catalog => RouteItem.section(
        path: '/examples',
        title: 'Examples',
        subtitle: '基础示例',
        routeItems: routes,
      );

  /// 所有路由列表
  List<RouteItem> get routes => _routes;

  static final List<RouteItem> _routes = [
    RouteItem.page(
      path: '/example/counter',
      title: 'Counter Example',
      subtitle: '计数器示例',
      pageBuilder: (BuildContext context) =>
          const CounterExample(title: 'Counter Example'),
    ),
    RouteItem.page(
      path: '/example/getx',
      title: 'GetX Example',
      subtitle: 'GetX示例',
      pageBuilder: (BuildContext context) => const GetXApp(title: 'GetX Example'),
    ),
  ];
}

/// 单例实例
const ExamplesModule examplesModule = ExamplesModule._();
