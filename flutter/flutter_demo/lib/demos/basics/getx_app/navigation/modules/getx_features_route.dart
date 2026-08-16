import 'package:flutter_demo/demos/basics/getx_app/bindings/getx_worker_binding.dart';
import 'package:flutter_demo/demos/basics/getx_app/navigation/base/getx_base_route.dart';
import 'package:flutter_demo/demos/basics/getx_app/navigation/models/getx_route_entry.dart';
import 'package:flutter_demo/demos/basics/getx_app/navigation/utils/getx_route_converter.dart';
import 'package:flutter_demo/demos/basics/getx_app/pages/getx_counter_page.dart';
import 'package:flutter_demo/demos/basics/getx_app/pages/getx_locale_page.dart';
import 'package:flutter_demo/demos/basics/getx_app/pages/getx_storage_page.dart';
import 'package:flutter_demo/demos/basics/getx_app/pages/getx_update_page.dart';
import 'package:flutter_demo/demos/basics/getx_app/pages/getx_utils_page.dart';
import 'package:flutter_demo/demos/basics/getx_app/pages/getx_worker_page.dart';
import 'package:get/get.dart';

class GetXFeaturesRoute extends GetXBaseRoute {
  @override
  String get prefix => "/prefix";

  @override
  List<GetPage<void>> getRoutePages() {
    return GetXRouteConverter.toGetPage(getRouteItems());
  }

  List<GetXRouteEntry> getRouteItems() {
    return [
      // 计数器示例
      GetXRouteEntry(
        name: "$prefix/counter",
        title: "Counter",
        subtitle: "计数器示例",
        page: () => const GetXCounterPage(title: 'Counter'),
      ),
      GetXRouteEntry(
        name: "$prefix/update",
        title: "Update",
        subtitle: "计数器示例",
        page: () => const GetXUpdatePage(title: 'Update'),
      ),
      GetXRouteEntry(
        name: "$prefix/worker",
        title: "Worker",
        subtitle: "计数器示例",
        page: () => const GetXWorkerPage(title: 'Worker'),
        binding: GetXWorkerBinding(),
      ),
      // 工具类示例
      GetXRouteEntry(
        name: "$prefix/utils",
        title: "Utils",
        subtitle: "工具类示例",
        page: () => const GetXUtilsPage(title: 'Utils'),
      ),
      // 工具类示例
      GetXRouteEntry(
        name: "$prefix/storage",
        title: "Storage",
        subtitle: "存储类示例",
        page: () => const GetXStoragePage(title: 'Storage'),
      ),
      // 语言切换示例
      GetXRouteEntry(
        name: "$prefix/locale",
        title: "Locale",
        subtitle: "语言切换示例",
        page: () => const GetXLocalePage(title: 'Locale'),
      ),
    ];
  }
}
