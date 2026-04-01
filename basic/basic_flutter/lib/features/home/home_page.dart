import 'package:basic_flutter/navigation/registry/route_registry.dart';
import 'package:basic_flutter/navigation/utils/route_converter.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

/// 首页 - 分组入口
class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Flutter Example')),
      body: ListView.builder(
        itemCount: routeRegistry.length,
        itemBuilder: (context, index) {
          final group = routeRegistry[index];
          return ListTile(
            title: Text(group.title),
            subtitle: Text(group.subtitle),
            trailing: const Icon(Icons.chevron_right),
            onTap: () => context.push(RouteConverter.groupPath, extra: group),
          );
        },
      ),
    );
  }
}
