import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/animations_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/async_programming_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/decoration_effects_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/dialogs_sheets_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/flow_layout_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/gesture_interaction_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/layout_builder_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/layout_containers_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/linear_layout_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/scroll_widgets_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/sliver_widgets_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/stack_positioning_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/state_driven_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes/state_management_routes.dart';

final List<RouteItem> layoutRoutes = [
  // ========== 容器布局 ==========
  RouteItem(
    path: '/layout-containers',
    title: '容器布局',
    subtitle: 'Container、Padding、Center、Align、SizedBox、ConstrainedBox',
    routeItems: layoutContainersRoutes,
  ),

  // ========== 线性布局 ==========
  RouteItem(
    path: '/linear-layout',
    title: '线性布局',
    subtitle: 'Row、Column、Flexible、Expanded',
    routeItems: linearLayoutRoutes,
  ),

  // ========== 堆叠定位 ==========
  RouteItem(
    path: '/stack-positioning',
    title: '堆叠定位',
    subtitle: 'Stack、Positioned',
    routeItems: stackPositioningRoutes,
  ),

  // ========== 流式布局 ==========
  RouteItem(
    path: '/flow-layout',
    title: '流式布局',
    subtitle: 'Wrap、Flow',
    routeItems: flowLayoutRoutes,
  ),

  // ========== 滚动组件 ==========
  RouteItem(
    path: '/scroll-widgets',
    title: '滚动组件',
    subtitle: 'ListView、GridView、PageView、TabBarView、NestedScrollView...',
    routeItems: scrollWidgetsRoutes,
  ),

  // ========== Sliver 组件 ==========
  RouteItem(
    path: '/sliver-widgets',
    title: 'Sliver 组件',
    subtitle: 'SliverList、SliverGrid、SliverAppBar',
    routeItems: sliverWidgetsRoutes,
  ),

  // ========== 手势交互 ==========
  RouteItem(
    path: '/gesture-interaction',
    title: '手势交互',
    subtitle: 'GestureDetector、PopScope',
    routeItems: gestureInteractionRoutes,
  ),

  // ========== 动画效果 ==========
  RouteItem(
    path: '/animations',
    title: '动画效果',
    subtitle: 'FadeTransition、ScaleTransition、RotationTransition...',
    routeItems: animationsRoutes,
  ),

  // ========== 弹窗与底部面板 ==========
  RouteItem(
    path: '/dialogs-sheets',
    title: '弹窗与底部面板',
    subtitle: 'Dialog、BottomSheet、DatePicker、Cupertino Dialogs',
    routeItems: dialogsSheetsRoutes,
  ),

  // ========== 装饰效果 ==========
  RouteItem(
    path: '/decoration-effects',
    title: '装饰效果',
    subtitle: 'DecoratedBox、Opacity、Clip、BackdropFilter、ShaderMask',
    routeItems: decorationEffectsRoutes,
  ),

  // ========== 布局构建器 ==========
  RouteItem(
    path: '/layout-builder',
    title: '布局构建器',
    subtitle: 'LayoutBuilder',
    routeItems: layoutBuilderRoutes,
  ),

  // ========== 状态驱动组件 ==========
  RouteItem(
    path: '/state-driven',
    title: '状态驱动组件',
    subtitle: 'FutureBuilder、StreamBuilder',
    routeItems: stateDrivenRoutes,
  ),

  // ========== 状态管理 ==========
  RouteItem(
    path: '/state-management',
    title: '状态管理',
    subtitle: 'InheritedWidget、ValueListenableBuilder、ListenableBuilder',
    routeItems: stateManagementRoutes,
  ),

  // ========== 异步编程 ==========
  RouteItem(
    path: '/async-programming',
    title: '异步编程',
    subtitle: 'Future、Stream、Compute、Completer、Isolate',
    routeItems: asyncProgrammingRoutes,
  ),
];
