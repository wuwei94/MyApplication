import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/demos/layout/asynchronous/completer_demo.dart';
import 'package:flutter_demo/demos/layout/asynchronous/compute_demo.dart';
import 'package:flutter_demo/demos/layout/asynchronous/future_demo.dart';
import 'package:flutter_demo/demos/layout/asynchronous/isolate_demo.dart';
import 'package:flutter_demo/demos/layout/asynchronous/stream_demo.dart';

final CatalogEntry asynchronousCatalog = CatalogEntry.catalog(
  path: 'asynchronous',
  title: '异步编程',
  subtitle: 'Future、Stream、Compute、Completer、Isolate',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'future',
      title: 'Future',
      subtitle: '异步任务',
      pageBuilder: (BuildContext context) => const FutureDemoPage(title: 'Future'),
    ),
    CatalogEntry.page(
      path: 'stream',
      title: 'Stream',
      subtitle: '异步流',
      pageBuilder: (BuildContext context) => const StreamDemoPage(title: 'Stream'),
    ),
    CatalogEntry.page(
      path: 'completer',
      title: 'Completer',
      subtitle: '异步完成器',
      pageBuilder: (BuildContext context) =>
          const CompleterDemoPage(title: 'Completer'),
    ),
    CatalogEntry.page(
      path: 'isolate',
      title: 'Isolate',
      subtitle: '多线程',
      pageBuilder: (BuildContext context) =>
          const IsolateDemoPage(title: 'Isolate'),
    ),
    CatalogEntry.page(
      path: 'compute',
      title: 'Compute',
      subtitle: '计算隔离',
      pageBuilder: (BuildContext context) =>
          const ComputeDemoPage(title: 'Compute'),
    ),
  ],
);
