import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/packages/utils/event_bus_example.dart';
import 'package:basic_flutter/demos/packages/utils/smart_dialog_example.dart';
import 'package:basic_flutter/demos/packages/utils/toast_example.dart';
import 'package:basic_flutter/demos/packages/utils/uuid_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry packagesUtilsCatalog = CatalogEntry.catalog(
  path: 'utils',
  title: 'Utils',
  subtitle: '提示、事件广播与标识生成',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'toast',
      title: 'Toast',
      subtitle: '轻提示展示与统一 toast 封装',
      pageBuilder: (BuildContext context) =>
          const ToastDemoPage(title: 'Toast'),
    ),
    CatalogEntry.page(
      path: 'smart-dialog',
      title: 'FlutterSmartDialog',
      subtitle: '统一 toast、loading、dialog 的全局弹层方案',
      pageBuilder: (BuildContext context) =>
          const SmartDialogDemoPage(title: 'FlutterSmartDialog'),
    ),
    CatalogEntry.page(
      path: 'event-bus',
      title: 'EventBus',
      subtitle: '事件广播、监听结果与消息流转',
      pageBuilder: (BuildContext context) =>
          const EventBusDemoPage(title: 'EventBus'),
    ),
    CatalogEntry.page(
      path: 'uuid',
      title: 'Uuid',
      subtitle: '多版本 UUID 生成、校验与复制',
      pageBuilder: (BuildContext context) => const UuidDemoPage(title: 'Uuid'),
    ),
  ],
);
