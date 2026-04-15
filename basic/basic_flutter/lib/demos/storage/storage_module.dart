import 'package:basic_flutter/demos/storage/hive_example.dart';
import 'package:basic_flutter/demos/storage/secure_storage_example.dart';
import 'package:basic_flutter/demos/storage/shared_preferences_example.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:flutter/widgets.dart';

/// Storage 模块
/// 
/// 包含：Hive、SecureStorage、SharedPreferences 等本地存储示例
class StorageModule {
  const StorageModule._();

  /// 首页目录入口
  CatalogItem get catalog => CatalogItem.catalog(
        path: '/storage',
        title: 'Storage',
        subtitle: '本地存储',
        children: routes,
      );

  /// 所有路由列表
  List<CatalogItem> get routes => _routes;

  static final List<CatalogItem> _routes = [
    CatalogItem.page(
      path: 'hive',
      title: 'Hive',
      subtitle: 'Hive示例',
      pageBuilder: (BuildContext context) => const HiveExample(title: 'Hive'),
    ),
    CatalogItem.page(
      path: 'secure-storage',
      title: 'SecureStorage',
      subtitle: 'SecureStorage示例',
      pageBuilder: (BuildContext context) =>
          const SecureStorageExample(title: 'SecureStorage'),
    ),
    CatalogItem.page(
      path: 'shared-preferences',
      title: 'SharedPreferences',
      subtitle: 'SharedPreferences示例',
      pageBuilder: (BuildContext context) =>
          const SharedPreferencesExample(title: 'SharedPreferences'),
    ),
  ];
}

/// 单例实例
const StorageModule storageModule = StorageModule._();
