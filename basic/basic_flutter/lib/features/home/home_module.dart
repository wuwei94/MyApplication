import 'package:basic_flutter/app/catalog/app_catalog.dart';
import 'package:basic_flutter/features/feature_list_page.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Home 模块
/// 
/// 应用首页，展示所有功能模块入口
class HomeModule {
  const HomeModule._();

  /// 首页路由
  RouteItem get homeRoute => RouteItem.page(
        path: '/home',
        title: 'Home',
        pageBuilder: (BuildContext context) {
          return FeatureListPage(
            title: 'Flutter Demo',
            routes: appCatalog,
          );
        },
      );
}

/// 单例实例
const HomeModule homeModule = HomeModule._();
