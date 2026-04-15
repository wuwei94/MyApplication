import 'package:basic_flutter/features/storage/hive_example.dart';
import 'package:basic_flutter/features/storage/secure_storage_example.dart';
import 'package:basic_flutter/features/storage/shared_preferences_example.dart';
import 'package:basic_flutter/navigation/models/route_module.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Storage 本地存储路由
final RouteModule storageModule = RouteModule(
  entry: RouteItem.section(
    path: '/storage',
    title: 'Storage',
    subtitle: '本地存储',
    routeItems: storageRoutes,
  ),
  routes: storageRoutes,
);

final List<RouteItem> storageRoutes = [
  RouteItem.page(
    path: 'hive',
    title: 'Hive',
    subtitle: 'Hive示例',
    pageBuilder: (BuildContext context) => const HiveExample(title: 'Hive'),
  ),
  RouteItem.page(
    path: 'secure-storage',
    title: 'SecureStorage',
    subtitle: 'SecureStorage示例',
    pageBuilder: (BuildContext context) =>
        const SecureStorageExample(title: 'SecureStorage'),
  ),
  RouteItem.page(
    path: 'shared-preferences',
    title: 'SharedPreferences',
    subtitle: 'SharedPreferences示例',
    pageBuilder: (BuildContext context) =>
        const SharedPreferencesExample(title: 'SharedPreferences'),
  ),
];
