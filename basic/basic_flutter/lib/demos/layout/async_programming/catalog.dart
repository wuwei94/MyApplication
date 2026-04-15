import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/async_programming/completer_example.dart';
import 'package:basic_flutter/demos/layout/async_programming/compute_example.dart';
import 'package:basic_flutter/demos/layout/async_programming/future_example.dart';
import 'package:basic_flutter/demos/layout/async_programming/isolate_example.dart';
import 'package:basic_flutter/demos/layout/async_programming/stream_example.dart';
import 'package:flutter/widgets.dart';

final CatalogItem asyncProgrammingCatalog = CatalogItem.catalog(
  path: '/layout/async',
  title: '异步编程',
  subtitle: 'Future、Stream、Compute、Completer、Isolate',
  children: <CatalogItem>[
    CatalogItem.page(
      path: '/layout/async/future',
      title: 'Future',
      subtitle: '异步任务',
      pageBuilder: (BuildContext context) => const FutureExample(title: 'Future'),
    ),
    CatalogItem.page(
      path: '/layout/async/stream',
      title: 'Stream',
      subtitle: '异步流',
      pageBuilder: (BuildContext context) => const StreamExample(title: 'Stream'),
    ),
    CatalogItem.page(
      path: '/layout/async/compute',
      title: 'Compute',
      subtitle: '计算隔离',
      pageBuilder: (BuildContext context) =>
          const ComputeExample(title: 'Compute'),
    ),
    CatalogItem.page(
      path: '/layout/async/completer',
      title: 'Completer',
      subtitle: '异步完成器',
      pageBuilder: (BuildContext context) =>
          const CompleterExample(title: 'Completer'),
    ),
    CatalogItem.page(
      path: '/layout/async/isolate',
      title: 'Isolate',
      subtitle: '多线程',
      pageBuilder: (BuildContext context) =>
          const IsolateExample(title: 'Isolate'),
    ),
  ],
);
