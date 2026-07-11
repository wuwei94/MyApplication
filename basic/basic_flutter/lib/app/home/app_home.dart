import 'package:basic_flutter/catalog/pages/catalog_page.dart';
import 'package:basic_flutter/catalog/registry/catalog_registry.dart';
import 'package:basic_flutter/catalog/services/catalog_tree_resolver.dart';
import 'package:flutter/widgets.dart';

/// 应用首页，展示所有 Demo 分组入口。
class AppHome extends StatelessWidget {
  const AppHome({super.key});

  @override
  Widget build(BuildContext context) {
    return CatalogPage(
      title: 'Flutter Example',
      routes: CatalogTreeResolver.resolve(catalogRegistry),
    );
  }
}
