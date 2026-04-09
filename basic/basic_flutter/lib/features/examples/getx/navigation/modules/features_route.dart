import 'package:basic_flutter/features/examples/getx/bindings/worker_binding.dart';
import 'package:basic_flutter/features/examples/getx/navigation/base/base_route.dart';
import 'package:basic_flutter/features/examples/getx/navigation/models/route_model.dart';
import 'package:basic_flutter/features/examples/getx/navigation/utils/route_converter.dart';
import 'package:basic_flutter/features/examples/getx/pages/counter_page.dart';
import 'package:basic_flutter/features/examples/getx/pages/locale_page.dart';
import 'package:basic_flutter/features/examples/getx/pages/storage_page.dart';
import 'package:basic_flutter/features/examples/getx/pages/update_page.dart';
import 'package:basic_flutter/features/examples/getx/pages/utils_page.dart';
import 'package:basic_flutter/features/examples/getx/pages/worker_page.dart';
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
        page: () => const CounterPage(title: 'Counter'),
      ),
      GetRouteItem(
        name: "$prefix/update",
        title: "Update",
        subtitle: "计数器示例",
        page: () => const UpdatePage(title: 'Update'),
      ),
      GetRouteItem(
        name: "$prefix/worker",
        title: "Worker",
        subtitle: "计数器示例",
        page: () => const WorkerPage(title: 'Worker'),
        binding: WorkerBinding(),
      ),
      // 工具类示例
      GetRouteItem(
        name: "$prefix/utils",
        title: "Utils",
        subtitle: "工具类示例",
        page: () => const UtilsPage(title: 'Utils'),
      ),
      // 工具类示例
      GetRouteItem(
        name: "$prefix/storage",
        title: "Storage",
        subtitle: "存储类示例",
        page: () => const StoragePage(title: 'Storage'),
      ),
      // 语言切换示例
      GetRouteItem(
        name: "$prefix/locale",
        title: "Locale",
        subtitle: "语言切换示例",
        page: () => const LocalePage(title: 'Locale'),
      ),
    ];
  }
}
