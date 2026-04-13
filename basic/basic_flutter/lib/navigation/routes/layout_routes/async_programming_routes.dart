import 'package:basic_flutter/features/layout/async_programming/completer_example.dart';
import 'package:basic_flutter/features/layout/async_programming/compute_example.dart';
import 'package:basic_flutter/features/layout/async_programming/future_example.dart';
import 'package:basic_flutter/features/layout/async_programming/isolate_example.dart';
import 'package:basic_flutter/features/layout/async_programming/stream_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Async Programming 路由
final List<RouteItem> asyncProgrammingRoutes = [
  RouteItem.page(
    path: 'future',
    title: 'Future',
    subtitle: 'Future异步',
    pageBuilder: (BuildContext context) => const FutureExample(title: 'Future'),
  ),
  RouteItem.page(
    path: 'stream',
    title: 'Stream',
    subtitle: 'Stream异步',
    pageBuilder: (BuildContext context) => const StreamExample(title: 'Stream'),
  ),
  RouteItem.page(
    path: 'compute',
    title: 'Compute',
    subtitle: '后台计算',
    pageBuilder: (BuildContext context) =>
        const ComputeExample(title: 'Compute'),
  ),
  RouteItem.page(
    path: 'completer',
    title: 'Completer',
    subtitle: '手动完成Future',
    pageBuilder: (BuildContext context) =>
        const CompleterExample(title: 'Completer'),
  ),
  RouteItem.page(
    path: 'isolate',
    title: 'Isolate',
    subtitle: '多线程Isolate',
    pageBuilder: (BuildContext context) =>
        const IsolateExample(title: 'Isolate'),
  ),
];
