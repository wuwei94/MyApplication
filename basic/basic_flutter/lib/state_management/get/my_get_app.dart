import 'package:basic_flutter/state_management/get/page/my_get_page.dart';
import 'package:flutter/material.dart';

/// GetX
/// https://pub.dev/packages/get
class MyGet extends StatelessWidget {
  const MyGet({super.key});

  @override
  Widget build(BuildContext context) {
    // 子页面不应该使用 GetMaterialApp，直接使用页面组件
    return MyGetXPage(title: 'Flutter GetX demo');
  }
}
