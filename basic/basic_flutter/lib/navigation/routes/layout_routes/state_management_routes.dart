import 'package:basic_flutter/features/layout/state_management/inherited_widget_example.dart';
import 'package:basic_flutter/features/layout/state_management/listenable_builder_example.dart';
import 'package:basic_flutter/features/layout/state_management/value_listenable_builder_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// State Management 路由
final List<RouteItem> stateManagementRoutes = [
  RouteItem(
    path: 'value-listenable-builder',
    title: 'ValueListenableBuilder',
    subtitle: '值监听',
    pageBuilder: (BuildContext context) =>
        const ValueListenableBuilderExample(title: 'ValueListenableBuilder'),
  ),
  RouteItem(
    path: 'listenable-builder',
    title: 'ListenableBuilder',
    subtitle: 'Listenable监听',
    pageBuilder: (BuildContext context) =>
        const ListenableBuilderExample(title: 'ListenableBuilder'),
  ),
  RouteItem(
    path: 'inherited-widget',
    title: 'InheritedWidget',
    subtitle: '状态共享',
    pageBuilder: (BuildContext context) =>
        const InheritedWidgetExample(title: 'InheritedWidget'),
  ),
];
