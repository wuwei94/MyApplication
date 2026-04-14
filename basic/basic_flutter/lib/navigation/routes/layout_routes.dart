import 'package:basic_flutter/navigation/models/route_module.dart';
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

final RouteModule layoutModule = RouteModule(
  entry: RouteItem.section(
    path: '/layout',
    title: 'Layout',
    subtitle: '布局组件',
    routeItems: layoutRoutes,
  ),
  routes: layoutRoutes,
);

final List<RouteItem> layoutRoutes = [
  // ========== 容器布局 ==========
  RouteItem.section(
    path: '/layout/containers',
    title: '容器布局',
    subtitle: 'Container、Padding、Center、Align、SizedBox、ConstrainedBox',
    routeItems: layoutContainersRoutes,
  ),

  // ========== 线性布局 ==========
  RouteItem.section(
    path: '/layout/linear',
    title: '线性布局',
    subtitle: 'Row、Column、Flexible、Expanded',
    routeItems: linearLayoutRoutes,
  ),

  // ========== 堆叠定位 ==========
  RouteItem.section(
    path: '/layout/stacking',
    title: '堆叠定位',
    subtitle: 'Stack、Positioned',
    routeItems: stackPositioningRoutes,
  ),

  // ========== 流式布局 ==========
  RouteItem.section(
    path: '/layout/flow',
    title: '流式布局',
    subtitle: 'Wrap、Flow',
    routeItems: flowLayoutRoutes,
  ),

  // ========== 滚动组件 ==========
  RouteItem.section(
    path: '/layout/scroll',
    title: '滚动组件',
    subtitle: 'ListView、GridView、PageView、TabBarView、NestedScrollView...',
    routeItems: scrollWidgetsRoutes,
  ),

  // ========== Sliver 组件 ==========
  RouteItem.section(
    path: '/layout/slivers',
    title: 'Sliver 组件',
    subtitle: 'SliverList、SliverGrid、SliverAppBar',
    routeItems: sliverWidgetsRoutes,
  ),

  // ========== 手势交互 ==========
  RouteItem.section(
    path: '/layout/gestures',
    title: '手势交互',
    subtitle: 'GestureDetector、PopScope',
    routeItems: gestureInteractionRoutes,
  ),

  // ========== 动画效果 ==========
  RouteItem.section(
    path: '/layout/animations',
    title: '动画效果',
    subtitle: 'FadeTransition、ScaleTransition、RotationTransition...',
    routeItems: animationsRoutes,
  ),

  // ========== 弹窗与底部面板 ==========
  RouteItem.section(
    path: '/layout/dialogs',
    title: '弹窗与底部面板',
    subtitle: 'Dialog、BottomSheet、DatePicker、Cupertino Dialogs',
    routeItems: dialogsSheetsRoutes,
  ),

  // ========== 装饰效果 ==========
  RouteItem.section(
    path: '/layout/decorations',
    title: '装饰效果',
    subtitle: 'DecoratedBox、Opacity、Clip、BackdropFilter、ShaderMask',
    routeItems: decorationEffectsRoutes,
  ),

  // ========== 布局构建器 ==========
  RouteItem.section(
    path: '/layout/builders',
    title: '布局构建器',
    subtitle: 'LayoutBuilder',
    routeItems: layoutBuilderRoutes,
  ),

  // ========== 状态驱动组件 ==========
  RouteItem.section(
    path: '/layout/state-driven',
    title: '状态驱动组件',
    subtitle: 'FutureBuilder、StreamBuilder',
    routeItems: stateDrivenRoutes,
  ),

  // ========== 状态管理 ==========
  RouteItem.section(
    path: '/layout/state-management',
    title: '状态管理',
    subtitle: 'InheritedWidget、ValueListenableBuilder、ListenableBuilder',
    routeItems: stateManagementRoutes,
  ),

  // ========== 异步编程 ==========
  RouteItem.section(
    path: '/layout/async',
    title: '异步编程',
    subtitle: 'Future、Stream、Compute、Completer、Isolate',
    routeItems: asyncProgrammingRoutes,
  ),
];
