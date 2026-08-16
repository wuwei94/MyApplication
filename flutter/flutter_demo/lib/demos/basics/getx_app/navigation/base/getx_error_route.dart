import 'package:flutter_demo/demos/basics/getx_app/error/getx_error_binding.dart';
import 'package:flutter_demo/demos/basics/getx_app/error/getx_error_page.dart';
import 'package:get/get.dart';

class GetXErrorRoute {
  /// 404 错误页面路径
  String get error404 => "/error/unknown404";

  /// 404 错误页面配置
  GetPage<void> get unknown => GetPage(
    name: error404,
    page: () => const GetXErrorPage(),
    binding: GetXErrorBinding(),
  );
}
