import 'package:basic_flutter/demos/storage/hive_example.dart';
import 'package:basic_flutter/demos/storage/secure_storage_example.dart';
import 'package:basic_flutter/demos/storage/shared_preferences_example.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/app/catalog/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// Storage 模块
/// 
/// 包含：Hive、SecureStorage、SharedPreferences 等本地存储示例
class StorageCatalog extends CatalogSection {
  const StorageCatalog._();

  @override
  String get path => '/storage';

  @override
  String get title => 'Storage';

  @override
  String get subtitle => '本地存储';

  @override
  List<CatalogItem> get items => _items;

  static final List<CatalogItem> _items = [
    CatalogItem.page(
      path: '/storage/hive',
      title: 'Hive',
      subtitle: 'Hive示例',
      pageBuilder: (BuildContext context) => const HiveExample(title: 'Hive'),
    ),
    CatalogItem.page(
      path: '/storage/secure-storage',
      title: 'SecureStorage',
      subtitle: 'SecureStorage示例',
      pageBuilder: (BuildContext context) =>
          const SecureStorageExample(title: 'SecureStorage'),
    ),
    CatalogItem.page(
      path: '/storage/shared-preferences',
      title: 'SharedPreferences',
      subtitle: 'SharedPreferences示例',
      pageBuilder: (BuildContext context) =>
          const SharedPreferencesExample(title: 'SharedPreferences'),
    ),
  ];
}

/// 单例实例
const StorageCatalog storageCatalog = StorageCatalog._();
