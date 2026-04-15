import 'package:basic_flutter/app/catalog/demo_catalog.dart';
import 'package:basic_flutter/app/catalog/demo_catalog_page.dart';
import 'package:flutter/widgets.dart';

/// 应用首页，展示所有 Demo 分组入口。
class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return DemoCatalogPage(title: 'Flutter Demo', routes: demoCatalog);
  }
}
