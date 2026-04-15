import 'package:basic_flutter/demos/examples/getx/error/error_binding.dart';
import 'package:basic_flutter/demos/examples/getx/error/error_view.dart';
import 'package:get/get.dart';

class ErrorRoute {
  /// 404 错误页面路径
  String get error404 => "/error/unknown404";

  /// 404 错误页面配置
  GetPage<void> get unknown => GetPage(
    name: error404,
    page: () => const ErrorPage(),
    binding: ErrorBinding(),
  );
}
