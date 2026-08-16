import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/pages/catalog_page.dart';
import 'package:flutter_demo/catalog/registry/catalog_registry.dart';
import 'package:flutter_demo/catalog/services/catalog_tree_resolver.dart';

/// 应用首页，展示所有 Demo 分组入口。
class AppHome extends StatelessWidget {
  const AppHome({super.key});

  @override
  Widget build(BuildContext context) {
    return CatalogPage(
      title: 'Flutter Demo',
      routes: CatalogTreeResolver.resolve(catalogRegistry),
    );
  }
}
