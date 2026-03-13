import 'package:basic_flutter/features/examples/getx/navigation/base/error_route.dart';
import 'package:basic_flutter/features/examples/getx/navigation/modules/features_route.dart';
import 'package:basic_flutter/features/examples/getx/pages/home_page.dart';
import 'package:get/get.dart';

class AppRoutes {
  static final features = FeaturesRoute();

  static List<GetPage<void>> getPages() {
    return [
      ...features.getRoutePages(),
      GetPage(name: "/home", page: () => const HomePage())
    ];
  }

  static GetPage<void> getUnknown() {
    return ErrorRoute().unknown;
  }
}
