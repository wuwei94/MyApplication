import 'package:basic_flutter/features/examples/getx/pages/counter_page.dart';
import 'package:basic_flutter/features/examples/getx/pages/utils_page.dart';
import 'package:basic_flutter/features/examples/getx/navigation/base/base_route.dart';
import 'package:basic_flutter/features/examples/getx/navigation/models/route_model.dart';
import 'package:basic_flutter/features/examples/getx/navigation/utils/route_converter.dart';
import 'package:get/get.dart';

class FeaturesRoute extends BaseRoute {
  @override
  String get prefix => "/prefix";

  @override
  List<GetPage<void>> getRoutePages() {
    return RouteConverter.toGetPage(getRouteItems());
  }

  List<GetRouteItem> getRouteItems() {
    return [
      // 计数器示例
      GetRouteItem(
        name: "$prefix/counter",
        title: "Counter",
        subtitle: "计数器示例",
        page: () => const CounterPage(),
      ),
      // 工具类示例
      GetRouteItem(
        name: "$prefix/utils",
        title: "Utils",
        subtitle: "工具类示例",
        page: () => const UtilsPage(),
      ),
    ];
  }
}
