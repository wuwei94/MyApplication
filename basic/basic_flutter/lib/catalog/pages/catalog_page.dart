import 'package:basic_flutter/app/navigation/app_navigator.dart';
import 'package:basic_flutter/catalog/models/resolved_catalog_entry.dart';
import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:flutter/material.dart';

/// 通用 Demo 目录页：首页展示分组，分组页展示具体示例
class CatalogPage extends StatelessWidget {
  final String title;
  final List<ResolvedCatalogEntry> routes;

  const CatalogPage({super.key, required this.title, required this.routes});

  void _handleTap(BuildContext context, ResolvedCatalogEntry item) {
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
          final ResolvedCatalogEntry item = routes[index];
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
