import 'package:flutter/material.dart';
import 'package:flutter_demo/demos/basics/getx_app/navigation/modules/getx_features_route.dart';
import 'package:get/get.dart';

/// GetX 示例列表页面
/// 用于展示所有 GetX 示例的入口
class GetXHomePage extends StatelessWidget {
  const GetXHomePage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    final routes = GetXFeaturesRoute().getRouteItems();
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: ListView.builder(
        itemCount: routes.length,
        itemBuilder: (context, index) {
          final item = routes[index];
          return ListTile(
            title: Text(item.title),
            subtitle: item.subtitle.isNotEmpty ? Text(item.subtitle) : null,
            onTap: () => Get.toNamed<void>(item.name),
          );
        },
      ),
    );
  }
}
