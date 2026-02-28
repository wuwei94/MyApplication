import 'package:basic_flutter/features/2_container/my_align.dart';
import 'package:basic_flutter/features/2_container/my_center.dart';
import 'package:basic_flutter/features/2_container/my_constrained_box.dart';
import 'package:basic_flutter/features/2_container/my_container.dart';
import 'package:basic_flutter/features/2_container/my_decorated_box.dart';
import 'package:basic_flutter/features/2_container/my_padding.dart';
import 'package:basic_flutter/features/2_container/my_sized_box.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Container 容器路由
final List<RouteItem> containerRoutes = [
  RouteItem(
    name: 'Container',
    path: '/container',
    describe: '容器',
    builder: (BuildContext context, _) => const MyContainer(),
  ),
  RouteItem(
    name: 'Padding',
    path: '/padding',
    describe: '填充容器',
    builder: (BuildContext context, _) => const MyPadding(),
  ),
  RouteItem(
    name: 'Align',
    path: '/align',
    describe: '对齐容器',
    builder: (BuildContext context, _) => const MyAlign(),
  ),
  RouteItem(
    name: 'Center',
    path: '/center',
    describe: '居中容器',
    builder: (BuildContext context, _) => const MyCenter(),
  ),
  RouteItem(
    name: 'ConstrainedBox',
    path: '/constrained-box',
    describe: '约束容器',
    builder: (BuildContext context, _) => const MyConstrainedBox(),
  ),
  RouteItem(
    name: 'DecoratedBox',
    path: '/decorated-box',
    describe: '装饰容器',
    builder: (BuildContext context, _) => const MyDecoratedBox(),
  ),
  RouteItem(
    name: 'SizedBox',
    path: '/sized-box',
    describe: '尺寸容器',
    builder: (BuildContext context, _) => const MySizedBox(),
  ),
];
