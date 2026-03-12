import 'package:basic_flutter/features/examples/getx/navigation/base/base_route.dart';
import 'package:basic_flutter/features/examples/getx/pages/home_page.dart';
import 'package:get/get.dart';

class MainRoute extends BaseRoute {
  @override
  String get prefix => "";

  String get home => "/home";

  @override
  List<GetPage<void>> getRoutePages() {
    return [GetPage(name: home, page: () => const HomePage())];
  }
}
