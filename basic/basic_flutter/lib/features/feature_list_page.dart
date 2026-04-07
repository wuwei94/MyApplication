import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:basic_flutter/navigation/constants/navigation_constants.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

/// 通用路由列表页：首页展示分组，分组页展示具体示例
class FeatureListPage extends StatelessWidget {
  final String title;
  final List<RouteItem> routes;

  const FeatureListPage({super.key, required this.title, required this.routes});

  void _handleTap(BuildContext context, RouteItem item) {
    logInfo('点击了 ${item.title}, 路由详情: $item');

    if (item.routeItems.isNotEmpty) {
      context.push(groupRoutePath, extra: item);
      return;
    }

    if (item.path.isNotEmpty) {
      item.pushByGo(context);
    }
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
