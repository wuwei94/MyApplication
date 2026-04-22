import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/packages/utils/event_bus_example.dart';
import 'package:basic_flutter/demos/packages/utils/toast_example.dart';
import 'package:basic_flutter/demos/packages/utils/uuid_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry packagesUtilsCatalog = CatalogEntry.catalog(
  path: 'utils',
  title: 'Utils',
  subtitle: '提示、事件通信与通用标识',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'toast',
      title: 'Toast',
      subtitle: '轻提示展示',
      pageBuilder: (BuildContext context) =>
          const ToastDemoPage(title: 'Toast'),
    ),
    CatalogEntry.page(
      path: 'event-bus',
      title: 'EventBus',
      subtitle: '应用内事件总线通信',
      pageBuilder: (BuildContext context) =>
          const EventBusDemoPage(title: 'EventBus'),
    ),
    CatalogEntry.page(
      path: 'uuid',
      title: 'Uuid',
      subtitle: 'UUID 生成与校验',
      pageBuilder: (BuildContext context) => const UuidDemoPage(title: 'Uuid'),
    ),
  ],
);
