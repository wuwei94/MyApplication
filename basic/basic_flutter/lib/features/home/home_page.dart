import 'package:basic_flutter/navigation/registry/route_registry.dart';
import 'package:flutter/material.dart';

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
          final item = routeRegistry[index];
          return ListTile(
            title: Text(item.title),
            subtitle: Text(item.subtitle),
            trailing: const Icon(Icons.chevron_right),
            onTap: () => item.pushByAuto(context),
          );
        },
      ),
    );
  }
}
