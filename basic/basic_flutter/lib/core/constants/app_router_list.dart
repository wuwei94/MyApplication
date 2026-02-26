import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:basic_flutter/routes/route_item.dart';

/// 路由列表数据
class AppRouteList {
  static List<RouteItem> getRouteList() {
    return [
      RouteItem(routeName: '—— Example ——', routePath: '', routeDescribe: ''),
      RouteItem(
        routeName: '计数器',
        routePath: ExampleRoutes.counter,
        routeDescribe: '基础计数器示例',
      ),
      RouteItem(
        routeName: '—— Layout 布局 ——',
        routePath: '',
        routeDescribe: '会有一个children属性',
      ),
      RouteItem(
        routeName: 'Row',
        routePath: LayoutRoutes.row,
        routeDescribe: '水平线性布局',
      ),
      RouteItem(
        routeName: 'Column',
        routePath: LayoutRoutes.column,
        routeDescribe: '垂直线性布局',
      ),
      RouteItem(
        routeName: 'Flex',
        routePath: LayoutRoutes.flex,
        routeDescribe: '弹性布局，按照一定比例来分配父容器空间',
      ),
      RouteItem(
        routeName: 'Wrap',
        routePath: LayoutRoutes.wrap,
        routeDescribe: '流式布局，根据子组件大小自动换行的布局',
      ),
      RouteItem(
        routeName: 'Flow',
        routePath: LayoutRoutes.flow,
        routeDescribe: '流式布局，根据子组件大小自动换行的布局',
      ),
      RouteItem(
        routeName: 'Stack',
        routePath: LayoutRoutes.stack,
        routeDescribe: '堆叠布局，根据距父容器四个角的位置来确定自身的位置',
      ),
      RouteItem(
        routeName: '—— Container 容器 ——',
        routePath: '',
        routeDescribe: '会有一个child属性',
      ),
      RouteItem(
        routeName: 'Container',
        routePath: ContainerRoutes.container,
        routeDescribe: '容器',
      ),
      RouteItem(
        routeName: 'Padding',
        routePath: ContainerRoutes.padding,
        routeDescribe: '填充容器',
      ),
      RouteItem(
        routeName: 'Align',
        routePath: ContainerRoutes.align,
        routeDescribe: '对齐容器',
      ),
      RouteItem(
        routeName: 'Center',
        routePath: ContainerRoutes.center,
        routeDescribe: '居中容器',
      ),
      RouteItem(
        routeName: 'ConstrainedBox',
        routePath: ContainerRoutes.constrainedBox,
        routeDescribe: '约束容器',
      ),
      RouteItem(
        routeName: 'DecoratedBox',
        routePath: ContainerRoutes.decoratedBox,
        routeDescribe: '装饰容器',
      ),
      RouteItem(
        routeName: 'SizedBox',
        routePath: ContainerRoutes.sizedBox,
        routeDescribe: '尺寸容器',
      ),
      RouteItem(routeName: '—— 可滚动组件 ——', routePath: ''),
      RouteItem(
        routeName: 'ListView',
        routePath: ScrollingRoutes.listView,
        routeDescribe: 'ListView',
      ),
      RouteItem(
        routeName: 'GridView',
        routePath: ScrollingRoutes.gridView,
        routeDescribe: 'GridView',
      ),
      RouteItem(
        routeName: 'ScrollView',
        routePath: ScrollingRoutes.scrollView,
        routeDescribe: 'ScrollView',
      ),
      RouteItem(
        routeName: 'PageView',
        routePath: ScrollingRoutes.pageView,
        routeDescribe: 'PageView',
      ),
      RouteItem(
        routeName: 'TabBarView',
        routePath: ScrollingRoutes.tabBarView,
        routeDescribe: 'TabBarView',
      ),
      RouteItem(
        routeName: 'AnimatedList',
        routePath: ScrollingRoutes.animatedList,
        routeDescribe: 'AnimatedList',
      ),
      RouteItem(
        routeName: 'CustomScrollView',
        routePath: ScrollingRoutes.customScrollView,
        routeDescribe: 'CustomScrollView',
      ),
      RouteItem(
        routeName: 'NestedScrollView',
        routePath: ScrollingRoutes.nestedScrollView,
        routeDescribe: 'NestedScrollView',
      ),
      RouteItem(routeName: '—— 功能型组件 ——', routePath: ''),
      RouteItem(
        routeName: 'LayoutBuilder',
        routePath: FunctionalRoutes.layoutBuilder,
        routeDescribe: '获取父组件大小并布局容器',
      ),
      RouteItem(
        routeName: 'GestureDetector',
        routePath: FunctionalRoutes.gestureDetector,
        routeDescribe: '手势检测',
      ),
      RouteItem(
        routeName: 'PopScope',
        routePath: FunctionalRoutes.popScope,
        routeDescribe: '返回拦截',
      ),
      RouteItem(
        routeName: 'InheritedWidget',
        routePath: FunctionalRoutes.inheritedWidget,
        routeDescribe: '数据共享',
      ),
      RouteItem(
        routeName: 'ValueListenableBuilder',
        routePath: FunctionalRoutes.valueListenableBuilder,
        routeDescribe: '数据源监听',
      ),
      RouteItem(
        routeName: 'FutureBuilder',
        routePath: FunctionalRoutes.futureBuilder,
        routeDescribe: '异步UI更新',
      ),
      RouteItem(
        routeName: 'StreamBuilder',
        routePath: FunctionalRoutes.streamBuilder,
        routeDescribe: '异步UI更新',
      ),
      RouteItem(routeName: '—— 其他组件 ——', routePath: ''),
      RouteItem(
        routeName: 'Animation',
        routePath: AnimationRoutes.animation,
        routeDescribe: 'Animation',
      ),
      RouteItem(
        routeName: 'Dialog',
        routePath: DialogRoutes.dialog,
        routeDescribe: 'Dialog',
      ),
      RouteItem(
        routeName: 'Isolate',
        routePath: ConcurrencyRoutes.isolate,
        routeDescribe: 'Isolate',
      ),
      RouteItem(routeName: '—— 网络请求 ——', routePath: ''),
      RouteItem(
        routeName: 'Dio',
        routePath: NetworkRoutes.dio,
        routeDescribe: 'Dio',
      ),
      RouteItem(routeName: '—— 状态管理 ——', routePath: ''),
      RouteItem(
        routeName: 'Provider',
        routePath: StateManagementRoutes.provider,
        routeDescribe: 'Provider',
      ),
      RouteItem(
        routeName: 'GetX',
        routePath: StateManagementRoutes.getX,
        routeDescribe: 'GetX',
      ),
      RouteItem(
        routeName: 'GetX2',
        routePath: StateManagementRoutes.getX2,
        routeDescribe: 'GetX',
      ),
      RouteItem(
        routeName: 'BloC',
        routePath: StateManagementRoutes.bloC,
        routeDescribe: 'BloC',
      ),
      RouteItem(routeName: '—— 三方框架 ——', routePath: ''),
      RouteItem(
        routeName: 'Toast',
        routePath: PackageRoutes.toast,
        routeDescribe: 'Toast',
      ),
      RouteItem(
        routeName: 'Notification',
        routePath: PackageRoutes.notification,
        routeDescribe: 'Notification',
      ),
      RouteItem(
        routeName: 'SharedPreferences',
        routePath: PackageRoutes.sharedPreferences,
        routeDescribe: 'SharedPreferences',
      ),
      RouteItem(
        routeName: 'ScreenUtil',
        routePath: PackageRoutes.screenUtil,
        routeDescribe: 'ScreenUtil',
      ),
    ];
  }
}