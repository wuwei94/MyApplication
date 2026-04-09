import 'package:basic_flutter/features/layout/layout_containers/align_example.dart';
import 'package:basic_flutter/features/layout/layout_containers/center_example.dart';
import 'package:basic_flutter/features/layout/layout_containers/constrainedbox_example.dart';
import 'package:basic_flutter/features/layout/layout_containers/container_example.dart';
import 'package:basic_flutter/features/layout/layout_containers/padding_example.dart';
import 'package:basic_flutter/features/layout/layout_containers/sizedbox_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Layout Containers 路由
final List<RouteItem> layoutContainersRoutes = [
  RouteItem(
    path: '/layout-containers/container',
    title: 'Container',
    subtitle: '万能容器组件',
    pageBuilder: (BuildContext context) =>
        const ContainerExample(title: 'Container'),
  ),
  RouteItem(
    path: '/layout-containers/padding',
    title: 'Padding',
    subtitle: '内边距组件',
    pageBuilder: (BuildContext context) =>
        const PaddingExample(title: 'Padding'),
  ),
  RouteItem(
    path: '/layout-containers/center',
    title: 'Center',
    subtitle: '居中对齐组件',
    pageBuilder: (BuildContext context) => const CenterExample(title: 'Center'),
  ),
  RouteItem(
    path: '/layout-containers/align',
    title: 'Align',
    subtitle: '对齐组件',
    pageBuilder: (BuildContext context) => const AlignExample(title: 'Align'),
  ),
  RouteItem(
    path: '/layout-containers/sizedbox',
    title: 'SizedBox',
    subtitle: '固定尺寸组件',
    pageBuilder: (BuildContext context) =>
        const SizedBoxExample(title: 'SizedBox'),
  ),
  RouteItem(
    path: '/layout-containers/constrainedbox',
    title: 'ConstrainedBox',
    subtitle: '约束组件',
    pageBuilder: (BuildContext context) =>
        const ConstrainedBoxExample(title: 'ConstrainedBox'),
  ),
];
