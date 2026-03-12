import 'package:basic_flutter/features/examples/getx/navigation/base/error_route.dart';
import 'package:basic_flutter/features/examples/getx/navigation/modules/features_route.dart';
import 'package:basic_flutter/features/examples/getx/navigation/modules/main_route.dart';
import 'package:get/get.dart';

class AppRoutes {
  static final main = MainRoute();

  static final features = FeaturesRoute();

  static List<GetPage<void>> getPages() {
    return [...main.getRoutePages(), ...features.getRoutePages()];
  }

  static GetPage<void> getUnknown() {
    return ErrorRoute().unknown;
  }
}
