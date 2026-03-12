import 'package:get/get.dart';

abstract class BaseRoute {
  /// 路由前缀，默认为空字符串
  String get prefix;

  /// 路由页面列表，默认为空列表
  List<GetPage<void>> getRoutePages();
}
