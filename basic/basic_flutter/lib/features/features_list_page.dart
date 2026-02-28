import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

/// 分组列表页面
/// 用于展示某一分类下的所有路由
class FeaturesListPage extends StatelessWidget {
  final String title;
  final List<RouteItem> routes;

  const FeaturesListPage({
    super.key,
    required this.title,
    required this.routes,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: ListView.builder(
        itemCount: routes.length,
        itemBuilder: (context, index) {
          final item = routes[index];
          return ListTile(
            title: Text(item.name),
            subtitle: item.describe.isNotEmpty ? Text(item.describe) : null,
            onTap: () => context.push(item.path),
          );
        },
      ),
    );
  }
}
