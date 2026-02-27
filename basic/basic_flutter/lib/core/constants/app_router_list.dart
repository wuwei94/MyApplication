import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:basic_flutter/routes/data/app_route_info.dart';

/// 路由列表数据
class AppRouteList {
  static List<AppRouteInfo> getRouteList() {
    return [
      AppRouteInfo(name: '—— Example ——', path: '', describe: ''),
      AppRouteInfo(
        name: '计数器',
        path: ExampleRoutes.counter,
        describe: '基础计数器示例',
      ),
      AppRouteInfo(
        name: '—— Layout 布局 ——',
        path: '',
        describe: '会有一个children属性',
      ),
      AppRouteInfo(
        name: 'Row',
        path: LayoutRoutes.row,
        describe: '水平线性布局',
      ),
      AppRouteInfo(
        name: 'Column',
        path: LayoutRoutes.column,
        describe: '垂直线性布局',
      ),
      AppRouteInfo(
        name: 'Flex',
        path: LayoutRoutes.flex,
        describe: '弹性布局，按照一定比例来分配父容器空间',
      ),
      AppRouteInfo(
        name: 'Wrap',
        path: LayoutRoutes.wrap,
        describe: '流式布局，根据子组件大小自动换行的布局',
      ),
      AppRouteInfo(
        name: 'Flow',
        path: LayoutRoutes.flow,
        describe: '流式布局，根据子组件大小自动换行的布局',
      ),
      AppRouteInfo(
        name: 'Stack',
        path: LayoutRoutes.stack,
        describe: '堆叠布局，根据距父容器四个角的位置来确定自身的位置',
      ),
      AppRouteInfo(
        name: '—— Container 容器 ——',
        path: '',
        describe: '会有一个child属性',
      ),
      AppRouteInfo(
        name: 'Container',
        path: ContainerRoutes.container,
        describe: '容器',
      ),
      AppRouteInfo(
        name: 'Padding',
        path: ContainerRoutes.padding,
        describe: '填充容器',
      ),
      AppRouteInfo(
        name: 'Align',
        path: ContainerRoutes.align,
        describe: '对齐容器',
      ),
      AppRouteInfo(
        name: 'Center',
        path: ContainerRoutes.center,
        describe: '居中容器',
      ),
      AppRouteInfo(
        name: 'ConstrainedBox',
        path: ContainerRoutes.constrainedBox,
        describe: '约束容器',
      ),
      AppRouteInfo(
        name: 'DecoratedBox',
        path: ContainerRoutes.decoratedBox,
        describe: '装饰容器',
      ),
      AppRouteInfo(
        name: 'SizedBox',
        path: ContainerRoutes.sizedBox,
        describe: '尺寸容器',
      ),
      AppRouteInfo(name: '—— 可滚动组件 ——', path: ''),
      AppRouteInfo(
        name: 'ListView',
        path: ScrollingRoutes.listView,
        describe: 'ListView',
      ),
      AppRouteInfo(
        name: 'GridView',
        path: ScrollingRoutes.gridView,
        describe: 'GridView',
      ),
      AppRouteInfo(
        name: 'ScrollView',
        path: ScrollingRoutes.scrollView,
        describe: 'ScrollView',
      ),
      AppRouteInfo(
        name: 'PageView',
        path: ScrollingRoutes.pageView,
        describe: 'PageView',
      ),
      AppRouteInfo(
        name: 'TabBarView',
        path: ScrollingRoutes.tabBarView,
        describe: 'TabBarView',
      ),
      AppRouteInfo(
        name: 'AnimatedList',
        path: ScrollingRoutes.animatedList,
        describe: 'AnimatedList',
      ),
      AppRouteInfo(
        name: 'CustomScrollView',
        path: ScrollingRoutes.customScrollView,
        describe: 'CustomScrollView',
      ),
      AppRouteInfo(
        name: 'NestedScrollView',
        path: ScrollingRoutes.nestedScrollView,
        describe: 'NestedScrollView',
      ),
      AppRouteInfo(name: '—— 功能型组件 ——', path: ''),
      AppRouteInfo(
        name: 'LayoutBuilder',
        path: FunctionalRoutes.layoutBuilder,
        describe: '获取父组件大小并布局容器',
      ),
      AppRouteInfo(
        name: 'GestureDetector',
        path: FunctionalRoutes.gestureDetector,
        describe: '手势检测',
      ),
      AppRouteInfo(
        name: 'PopScope',
        path: FunctionalRoutes.popScope,
        describe: '返回拦截',
      ),
      AppRouteInfo(
        name: 'InheritedWidget',
        path: FunctionalRoutes.inheritedWidget,
        describe: '数据共享',
      ),
      AppRouteInfo(
        name: 'ValueListenableBuilder',
        path: FunctionalRoutes.valueListenableBuilder,
        describe: '数据源监听',
      ),
      AppRouteInfo(
        name: 'FutureBuilder',
        path: FunctionalRoutes.futureBuilder,
        describe: '异步UI更新',
      ),
      AppRouteInfo(
        name: 'StreamBuilder',
        path: FunctionalRoutes.streamBuilder,
        describe: '异步UI更新',
      ),
      AppRouteInfo(name: '—— 其他组件 ——', path: ''),
      AppRouteInfo(
        name: 'Animation',
        path: AnimationRoutes.animation,
        describe: 'Animation',
      ),
      AppRouteInfo(
        name: 'Dialog',
        path: DialogRoutes.dialog,
        describe: 'Dialog',
      ),
      AppRouteInfo(
        name: 'Isolate',
        path: ConcurrencyRoutes.isolate,
        describe: 'Isolate',
      ),
      AppRouteInfo(name: '—— 网络请求 ——', path: ''),
      AppRouteInfo(
        name: 'Dio',
        path: NetworkRoutes.dio,
        describe: 'Dio',
      ),
      AppRouteInfo(name: '—— 状态管理 ——', path: ''),
      AppRouteInfo(
        name: 'Provider',
        path: StateManagementRoutes.provider,
        describe: 'Provider',
      ),
      AppRouteInfo(
        name: 'GetX',
        path: StateManagementRoutes.getX,
        describe: 'GetX',
      ),
      AppRouteInfo(
        name: 'GetX2',
        path: StateManagementRoutes.getX2,
        describe: 'GetX',
      ),
      AppRouteInfo(
        name: 'BloC',
        path: StateManagementRoutes.bloC,
        describe: 'BloC',
      ),
      AppRouteInfo(name: '—— 三方框架 ——', path: ''),
      AppRouteInfo(
        name: 'Toast',
        path: PackageRoutes.toast,
        describe: 'Toast',
      ),
      AppRouteInfo(
        name: 'Notification',
        path: PackageRoutes.notification,
        describe: 'Notification',
      ),
      AppRouteInfo(
        name: 'SharedPreferences',
        path: PackageRoutes.sharedPreferences,
        describe: 'SharedPreferences',
      ),
      AppRouteInfo(
        name: 'ScreenUtil',
        path: PackageRoutes.screenUtil,
        describe: 'ScreenUtil',
      ),
    ];
  }
}