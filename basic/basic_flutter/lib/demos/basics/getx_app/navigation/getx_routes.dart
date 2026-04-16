import 'package:basic_flutter/demos/basics/getx_app/navigation/base/getx_error_route.dart';
import 'package:basic_flutter/demos/basics/getx_app/navigation/modules/getx_features_route.dart';
import 'package:basic_flutter/demos/basics/getx_app/pages/getx_home_page.dart';
import 'package:get/get.dart';

class GetXRoutes {
  static final features = GetXFeaturesRoute();

  static List<GetPage<void>> getPages() {
    return [
      ...features.getRoutePages(),
      GetPage(
        name: '/home',
        page: () => const GetXHomePage(title: 'GetX示例'),
      ),
    ];
  }

  static GetPage<void> getUnknown() {
    return GetXErrorRoute().unknown;
  }
}
