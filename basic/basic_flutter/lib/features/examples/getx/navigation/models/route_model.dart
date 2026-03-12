import 'package:flutter/widgets.dart';
import 'package:get/get.dart';

class GetRouteItem {
  final String name;
  final String title;
  final String subtitle;
  final GetPageBuilder page;

  GetRouteItem({
    this.name = "",
    this.title = "",
    this.subtitle = "",
    GetPageBuilder? page,
  }) : page = page ?? (() => const SizedBox.shrink());

  // 转换为 GetPage
  GetPage<dynamic> toGetPage() => GetPage<dynamic>(name: name, page: page);
}
