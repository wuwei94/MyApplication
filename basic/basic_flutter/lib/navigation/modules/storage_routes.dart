import 'package:basic_flutter/features/storage/hive_example.dart';
import 'package:basic_flutter/features/storage/secure_storage_example.dart';
import 'package:basic_flutter/features/storage/shared_preferences_example.dart';
import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Storage 本地存储路由
final List<RouteItem> storageRoutes = [
  RouteItem(
    path: '/storage/hive',
    title: 'Hive',
    subtitle: 'Hive',
    routeBuilder: (BuildContext context, _) => const HiveExample(),
  ),
  RouteItem(
    path: '/storage/secure-storage',
    title: 'SecureStorage',
    subtitle: 'SecureStorage',
    routeBuilder: (BuildContext context, _) => const SecureStorageExample(),
  ),
  RouteItem(
    path: '/storage/shared-preferences',
    title: 'SharedPreferences',
    subtitle: 'SharedPreferences',
    routeBuilder: (BuildContext context, _) => const SharedPreferencesExample(),
  ),
];
