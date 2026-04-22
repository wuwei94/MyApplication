import 'package:basic_flutter/demos/storage/drift_example.dart';
import 'package:basic_flutter/demos/storage/hive_example.dart';
import 'package:basic_flutter/demos/storage/isar_example.dart';
import 'package:basic_flutter/demos/storage/objectbox_example.dart';
import 'package:basic_flutter/demos/storage/path_provider_example.dart';
import 'package:basic_flutter/demos/storage/secure_storage_example.dart';
import 'package:basic_flutter/demos/storage/shared_preferences_example.dart';
import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// Storage 模块
///
/// 包含：Hive、Drift、Isar、ObjectBox、SecureStorage、SharedPreferences、PathProvider 等本地存储示例
class StorageCatalog extends CatalogSection {
  const StorageCatalog._();

  @override
  String get path => 'storage';

  @override
  String get title => 'Storage Example';

  @override
  String get subtitle => '本地存储';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'hive',
      title: 'Hive',
      subtitle: 'Hive示例',
      pageBuilder: (BuildContext context) => const HiveDemoPage(title: 'Hive'),
    ),
    CatalogEntry.page(
      path: 'secure-storage',
      title: 'SecureStorage',
      subtitle: 'SecureStorage示例',
      pageBuilder: (BuildContext context) =>
          const SecureStorageDemoPage(title: 'SecureStorage'),
    ),
    CatalogEntry.page(
      path: 'shared-preferences',
      title: 'SharedPreferences',
      subtitle: 'SharedPreferences示例',
      pageBuilder: (BuildContext context) =>
          const SharedPreferencesDemoPage(title: 'SharedPreferences'),
    ),
    CatalogEntry.page(
      path: 'objectbox',
      title: 'ObjectBox',
      subtitle: 'ObjectBox 对象数据库示例',
      pageBuilder: (BuildContext context) =>
          const ObjectBoxDemoPage(title: 'ObjectBox'),
    ),
    CatalogEntry.page(
      path: 'drift',
      title: 'Drift',
      subtitle: 'Drift 关系型数据库示例',
      pageBuilder: (BuildContext context) =>
          const DriftDemoPage(title: 'Drift'),
    ),
    CatalogEntry.page(
      path: 'isar',
      title: 'Isar',
      subtitle: 'Isar 对象数据库示例',
      pageBuilder: (BuildContext context) => const IsarDemoPage(title: 'Isar'),
    ),
    CatalogEntry.page(
      path: 'path-provider',
      title: 'PathProvider',
      subtitle: '系统目录与本地文件路径示例',
      pageBuilder: (BuildContext context) =>
          const PathProviderDemoPage(title: 'PathProvider'),
    ),
  ];
}

/// 单例实例
const StorageCatalog storageCatalog = StorageCatalog._();
