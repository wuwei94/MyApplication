import 'package:basic_flutter/features/state_manager/getx/pages/counter_getx_page.dart';
import 'package:flutter/material.dart';

/// GetX
/// https://pub.dev/packages/get
class CounterGetxExample extends StatelessWidget {
  const CounterGetxExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    // 子页面不应该使用 GetMaterialApp，直接使用页面组件
    return CounterGetxPage(title: title);
  }
}
