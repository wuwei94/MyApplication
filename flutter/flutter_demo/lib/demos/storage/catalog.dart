import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/storage/drift_demo.dart';
import 'package:flutter_demo/demos/storage/hive_demo.dart';
import 'package:flutter_demo/demos/storage/isar_demo.dart';
import 'package:flutter_demo/demos/storage/lib_storage_demo.dart';
import 'package:flutter_demo/demos/storage/objectbox_demo.dart';
import 'package:flutter_demo/demos/storage/path_provider_demo.dart';
import 'package:flutter_demo/demos/storage/secure_storage_demo.dart';
import 'package:flutter_demo/demos/storage/shared_preferences_demo.dart';
import 'package:flutter_demo/demos/storage/sqflite_demo.dart';

/// Storage 模块
///
/// 包含：Hive、Drift、Isar、ObjectBox、SecureStorage、SharedPreferences、
/// PathProvider 等本地存储示例，以及统一封装 lib_storage 的用法示例
///
/// 列表顺序与副标题按类别排列，便于横向对比同类方案：
/// 键值存储 → 关系型数据库 → 对象数据库 → 门面封装 → 路径工具
class StorageCatalog extends CatalogSection {
  const StorageCatalog._();

  @override
  String get path => 'storage';

  @override
  String get title => 'Storage';

  @override
  String get subtitle => '键值存储、数据库与文件目录';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'shared-preferences',
      title: 'SharedPreferences',
      subtitle: '键值存储 · 轻量键值存储与计数器持久化',
      pageBuilder: (BuildContext context) =>
          const SharedPreferencesDemoPage(title: 'SharedPreferences'),
    ),
    CatalogEntry.page(
      path: 'secure-storage',
      title: 'SecureStorage',
      subtitle: '键值存储（加密） · 敏感数据持久化与读取',
      pageBuilder: (BuildContext context) =>
          const SecureStorageDemoPage(title: 'SecureStorage'),
    ),
    CatalogEntry.page(
      path: 'hive',
      title: 'Hive',
      subtitle: '键值存储 · 本地键值存储与计数器持久化',
      pageBuilder: (BuildContext context) => const HiveDemoPage(title: 'Hive'),
    ),
    CatalogEntry.page(
      path: 'drift',
      title: 'Drift',
      subtitle: '关系型数据库 · SQL 任务列表示例与查询监听',
      pageBuilder: (BuildContext context) =>
          const DriftDemoPage(title: 'Drift'),
    ),
    CatalogEntry.page(
      path: 'sqflite',
      title: 'Sqflite',
      subtitle: '关系型数据库 · 手写 SQL 的任务 CRUD 与事务',
      pageBuilder: (BuildContext context) =>
          const SqfliteDemoPage(title: 'Sqflite'),
    ),
    CatalogEntry.page(
      path: 'isar',
      title: 'Isar',
      subtitle: '对象数据库 · 任务列表示例与变更监听',
      pageBuilder: (BuildContext context) => const IsarDemoPage(title: 'Isar'),
    ),
    CatalogEntry.page(
      path: 'objectbox',
      title: 'ObjectBox',
      subtitle: '对象数据库 · 任务列表示例与自动刷新',
      pageBuilder: (BuildContext context) =>
          const ObjectBoxDemoPage(title: 'ObjectBox'),
    ),
    CatalogEntry.page(
      path: 'lib-storage',
      title: 'lib_storage',
      subtitle: '门面封装 · Hive / SharedPreferences 双内核可切换',
      pageBuilder: (BuildContext context) =>
          const LibStorageDemoPage(title: 'lib_storage'),
    ),
    CatalogEntry.page(
      path: 'path-provider',
      title: 'PathProvider',
      subtitle: '路径工具 · 系统目录探测与示例文件读写',
      pageBuilder: (BuildContext context) =>
          const PathProviderDemoPage(title: 'PathProvider'),
    ),
  ];
}

/// 单例实例
const StorageCatalog storageCatalog = StorageCatalog._();
