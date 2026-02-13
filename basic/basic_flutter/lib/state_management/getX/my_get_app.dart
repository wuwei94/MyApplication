import 'package:basic_flutter/state_management/getX/my_get_view.dart';
import 'package:flutter/material.dart';

/// GetX
/// https://pub.dev/packages/get
class MyGetX2 extends StatelessWidget {
  const MyGetX2({super.key});

  @override
  Widget build(BuildContext context) {
    // 子页面不应该使用 GetMaterialApp，使用普通页面包装
    return FirstPage();
  }
}
