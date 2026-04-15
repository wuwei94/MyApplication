import 'package:basic_flutter/app/catalog/demo_catalog.dart';
import 'package:basic_flutter/demos/demo_catalog_page.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:flutter/widgets.dart';

/// Home 模块
/// 
/// 应用首页，展示所有 Demo 分组入口
class HomeModule {
  const HomeModule._();

  /// 首页路由
  CatalogItem get homeRoute => CatalogItem.page(
        path: '/home',
        title: 'Home',
        pageBuilder: (BuildContext context) {
          return DemoCatalogPage(
            title: 'Flutter Demo',
            routes: demoCatalog,
          );
        },
      );
}

/// 单例实例
const HomeModule homeModule = HomeModule._();
