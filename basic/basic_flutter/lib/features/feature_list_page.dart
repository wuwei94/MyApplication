import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:basic_flutter/navigation/utils/app_navigator.dart';
import 'package:flutter/material.dart';

/// 通用路由列表页：首页展示分组，分组页展示具体示例
class FeatureListPage extends StatelessWidget {
  final String title;
  final List<RouteItem> routes;

  const FeatureListPage({super.key, required this.title, required this.routes});

  void _handleTap(BuildContext context, RouteItem item) {
    logInfo('点击了 ${item.title}, 路由详情: $item');
    AppNavigator.pushPath<void>(context, item.path);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: ListView.builder(
        itemCount: routes.length,
        itemBuilder: (context, index) {
          final RouteItem item = routes[index];
          return ListTile(
            title: Text(item.title),
            subtitle: item.subtitle.isNotEmpty ? Text(item.subtitle) : null,
            trailing: const Icon(Icons.chevron_right),
            onTap: () => _handleTap(context, item),
          );
        },
      ),
    );
  }
}
