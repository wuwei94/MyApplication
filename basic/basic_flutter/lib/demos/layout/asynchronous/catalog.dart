import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/layout/asynchronous/completer_example.dart';
import 'package:basic_flutter/demos/layout/asynchronous/compute_example.dart';
import 'package:basic_flutter/demos/layout/asynchronous/future_example.dart';
import 'package:basic_flutter/demos/layout/asynchronous/isolate_example.dart';
import 'package:basic_flutter/demos/layout/asynchronous/stream_example.dart';
import 'package:flutter/widgets.dart';

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
      path: 'compute',
      title: 'Compute',
      subtitle: '计算隔离',
      pageBuilder: (BuildContext context) =>
          const ComputeDemoPage(title: 'Compute'),
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
  ],
);
