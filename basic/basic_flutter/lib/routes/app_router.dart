import 'package:basic_flutter/animation/my_animation.dart';
import 'package:basic_flutter/concurrency/my_isolate.dart';
import 'package:basic_flutter/container/my_align.dart';
import 'package:basic_flutter/container/my_center.dart';
import 'package:basic_flutter/container/my_constrained_box.dart';
import 'package:basic_flutter/container/my_container.dart';
import 'package:basic_flutter/container/my_decorated_box.dart';
import 'package:basic_flutter/container/my_padding.dart';
import 'package:basic_flutter/container/my_sized_box.dart';
import 'package:basic_flutter/dialog/my_dialog.dart';
import 'package:basic_flutter/examples/my_counter.dart';
import 'package:basic_flutter/main.dart';
import 'package:basic_flutter/functional/my_future_builder.dart';
import 'package:basic_flutter/functional/my_gesture_detector.dart';
import 'package:basic_flutter/functional/my_inherited_widget.dart';
import 'package:basic_flutter/functional/my_layout_builder.dart';
import 'package:basic_flutter/functional/my_pop_scope.dart';
import 'package:basic_flutter/functional/my_stream_builder.dart';
import 'package:basic_flutter/functional/my_value_listenable_builder.dart';
import 'package:basic_flutter/layout/my_column.dart';
import 'package:basic_flutter/layout/my_flex.dart';
import 'package:basic_flutter/layout/my_flow.dart';
import 'package:basic_flutter/layout/my_row.dart';
import 'package:basic_flutter/layout/my_stack.dart';
import 'package:basic_flutter/layout/my_wrap.dart';
import 'package:basic_flutter/packages/my_dio.dart';
import 'package:basic_flutter/packages/my_notification.dart';
import 'package:basic_flutter/packages/my_screen_util.dart';
import 'package:basic_flutter/packages/my_shared_preferences.dart';
import 'package:basic_flutter/packages/my_toast.dart';
import 'package:basic_flutter/routes/route_item.dart';
import 'package:basic_flutter/scroll/my_animated_list.dart';
import 'package:basic_flutter/scroll/my_custom_scroll_view.dart';
import 'package:basic_flutter/scroll/my_grid_view.dart';
import 'package:basic_flutter/scroll/my_list_view.dart';
import 'package:basic_flutter/scroll/my_nested_scroll_view.dart';
import 'package:basic_flutter/scroll/my_page_view.dart';
import 'package:basic_flutter/scroll/my_scroll_view.dart';
import 'package:basic_flutter/scroll/my_tab_bar_view.dart';
import 'package:basic_flutter/state_management/bloc/my_bloc.dart';
import 'package:basic_flutter/state_management/get/my_get_app.dart';
import 'package:basic_flutter/state_management/getX/my_get_app.dart';
import 'package:basic_flutter/state_management/provider/my_provider.dart';
import 'package:go_router/go_router.dart' hide RouteData;

/// 路由路径常量
class AppRoutes {
  static const String home = '/';
  static const String counter = '/counter';

  // Layout 布局
  static const String row = '/row';
  static const String column = '/column';
  static const String flex = '/flex';
  static const String wrap = '/wrap';
  static const String flow = '/flow';
  static const String stack = '/stack';

  // Container 容器
  static const String container = '/container';
  static const String padding = '/padding';
  static const String align = '/align';
  static const String center = '/center';
  static const String constrainedBox = '/constrained-box';
  static const String decoratedBox = '/decorated-box';
  static const String sizedBox = '/sized-box';

  // 可滚动组件
  static const String listView = '/list-view';
  static const String gridView = '/grid-view';
  static const String scrollView = '/scroll-view';
  static const String pageView = '/page-view';
  static const String tabBarView = '/tab-bar-view';
  static const String animatedList = '/animated-list';
  static const String customScrollView = '/custom-scroll-view';
  static const String nestedScrollView = '/nested-scroll-view';

  // 功能型组件
  static const String layoutBuilder = '/layout-builder';
  static const String gestureDetector = '/gesture-detector';
  static const String popScope = '/pop-scope';
  static const String inheritedWidget = '/inherited-widget';
  static const String valueListenableBuilder = '/value-listenable-builder';
  static const String futureBuilder = '/future-builder';
  static const String streamBuilder = '/stream-builder';

  // Other
  static const String animation = '/animation';
  static const String dialog = '/dialog';
  static const String isolate = '/isolate';

  // 网络请求
  static const String dio = '/dio';

  // 状态管理
  static const String provider = '/provider';
  static const String getX = '/getx';
  static const String getX2 = '/getx2';
  static const String bloC = '/bloc';

  // 三方框架
  static const String toast = '/toast';
  static const String notification = '/notification';
  static const String sharedPreferences = '/shared-preferences';
  static const String screenUtil = '/screen-util';
}

/// 路由列表数据
class AppRouteList {
  static List<RouteItem> getRouteList() {
    return [
      RouteItem(
        routeName: '—— Example ——',
        routePath: '',
        routeDescribe: '',
      ),
      RouteItem(
        routeName: '计数器',
        routePath: AppRoutes.counter,
        routeDescribe: '基础计数器示例',
      ),
      RouteItem(
        routeName: '—— Layout 布局 ——',
        routePath: '',
        routeDescribe: '会有一个children属性',
      ),
      RouteItem(
        routeName: 'Row',
        routePath: AppRoutes.row,
        routeDescribe: '水平线性布局',
      ),
      RouteItem(
        routeName: 'Column',
        routePath: AppRoutes.column,
        routeDescribe: '垂直线性布局',
      ),
      RouteItem(
        routeName: 'Flex',
        routePath: AppRoutes.flex,
        routeDescribe: '弹性布局，按照一定比例来分配父容器空间',
      ),
      RouteItem(
        routeName: 'Wrap',
        routePath: AppRoutes.wrap,
        routeDescribe: '流式布局，根据子组件大小自动换行的布局',
      ),
      RouteItem(
        routeName: 'Flow',
        routePath: AppRoutes.flow,
        routeDescribe: '流式布局，根据子组件大小自动换行的布局',
      ),
      RouteItem(
        routeName: 'Stack',
        routePath: AppRoutes.stack,
        routeDescribe: '堆叠布局，根据距父容器四个角的位置来确定自身的位置',
      ),
      RouteItem(
        routeName: '—— Container 容器 ——',
        routePath: '',
        routeDescribe: '会有一个child属性',
      ),
      RouteItem(
        routeName: 'Container',
        routePath: AppRoutes.container,
        routeDescribe: '容器',
      ),
      RouteItem(
        routeName: 'Padding',
        routePath: AppRoutes.padding,
        routeDescribe: '填充容器',
      ),
      RouteItem(
        routeName: 'Align',
        routePath: AppRoutes.align,
        routeDescribe: '对齐容器',
      ),
      RouteItem(
        routeName: 'Center',
        routePath: AppRoutes.center,
        routeDescribe: '居中容器',
      ),
      RouteItem(
        routeName: 'ConstrainedBox',
        routePath: AppRoutes.constrainedBox,
        routeDescribe: '约束容器',
      ),
      RouteItem(
        routeName: 'DecoratedBox',
        routePath: AppRoutes.decoratedBox,
        routeDescribe: '装饰容器',
      ),
      RouteItem(
        routeName: 'SizedBox',
        routePath: AppRoutes.sizedBox,
        routeDescribe: '尺寸容器',
      ),
      RouteItem(routeName: '—— 可滚动组件 ——', routePath: ''),
      RouteItem(
        routeName: 'ListView',
        routePath: AppRoutes.listView,
        routeDescribe: 'ListView',
      ),
      RouteItem(
        routeName: 'GridView',
        routePath: AppRoutes.gridView,
        routeDescribe: 'GridView',
      ),
      RouteItem(
        routeName: 'ScrollView',
        routePath: AppRoutes.scrollView,
        routeDescribe: 'ScrollView',
      ),
      RouteItem(
        routeName: 'PageView',
        routePath: AppRoutes.pageView,
        routeDescribe: 'PageView',
      ),
      RouteItem(
        routeName: 'TabBarView',
        routePath: AppRoutes.tabBarView,
        routeDescribe: 'TabBarView',
      ),
      RouteItem(
        routeName: 'AnimatedList',
        routePath: AppRoutes.animatedList,
        routeDescribe: 'AnimatedList',
      ),
      RouteItem(
        routeName: 'CustomScrollView',
        routePath: AppRoutes.customScrollView,
        routeDescribe: 'CustomScrollView',
      ),
      RouteItem(
        routeName: 'NestedScrollView',
        routePath: AppRoutes.nestedScrollView,
        routeDescribe: 'NestedScrollView',
      ),
      RouteItem(routeName: '—— 功能型组件 ——', routePath: ''),
      RouteItem(
        routeName: 'LayoutBuilder',
        routePath: AppRoutes.layoutBuilder,
        routeDescribe: '获取父组件大小并布局容器',
      ),
      RouteItem(
        routeName: 'GestureDetector',
        routePath: AppRoutes.gestureDetector,
        routeDescribe: '手势检测',
      ),
      RouteItem(
        routeName: 'PopScope',
        routePath: AppRoutes.popScope,
        routeDescribe: '返回拦截',
      ),
      RouteItem(
        routeName: 'InheritedWidget',
        routePath: AppRoutes.inheritedWidget,
        routeDescribe: '数据共享',
      ),
      RouteItem(
        routeName: 'ValueListenableBuilder',
        routePath: AppRoutes.valueListenableBuilder,
        routeDescribe: '数据源监听',
      ),
      RouteItem(
        routeName: 'FutureBuilder',
        routePath: AppRoutes.futureBuilder,
        routeDescribe: '异步UI更新',
      ),
      RouteItem(
        routeName: 'StreamBuilder',
        routePath: AppRoutes.streamBuilder,
        routeDescribe: '异步UI更新',
      ),
      RouteItem(routeName: '—— 其他组件 ——', routePath: ''),
      RouteItem(
        routeName: 'Animation',
        routePath: AppRoutes.animation,
        routeDescribe: 'Animation',
      ),
      RouteItem(
        routeName: 'Dialog',
        routePath: AppRoutes.dialog,
        routeDescribe: 'Dialog',
      ),
      RouteItem(
        routeName: 'Isolate',
        routePath: AppRoutes.isolate,
        routeDescribe: 'Isolate',
      ),
      RouteItem(routeName: '—— 网络请求 ——', routePath: ''),
      RouteItem(
        routeName: 'Dio',
        routePath: AppRoutes.dio,
        routeDescribe: 'Dio',
      ),
      RouteItem(routeName: '—— 状态管理 ——', routePath: ''),
      RouteItem(
        routeName: 'Provider',
        routePath: AppRoutes.provider,
        routeDescribe: 'Provider',
      ),
      RouteItem(
        routeName: 'GetX',
        routePath: AppRoutes.getX,
        routeDescribe: 'GetX',
      ),
      RouteItem(
        routeName: 'GetX2',
        routePath: AppRoutes.getX2,
        routeDescribe: 'GetX',
      ),
      RouteItem(
        routeName: 'BloC',
        routePath: AppRoutes.bloC,
        routeDescribe: 'BloC',
      ),
      RouteItem(routeName: '—— 三方框架 ——', routePath: ''),
      RouteItem(
        routeName: 'Toast',
        routePath: AppRoutes.toast,
        routeDescribe: 'Toast',
      ),
      RouteItem(
        routeName: 'Notification',
        routePath: AppRoutes.notification,
        routeDescribe: 'Notification',
      ),
      RouteItem(
        routeName: 'SharedPreferences',
        routePath: AppRoutes.sharedPreferences,
        routeDescribe: 'SharedPreferences',
      ),
      RouteItem(
        routeName: 'ScreenUtil',
        routePath: AppRoutes.screenUtil,
        routeDescribe: 'ScreenUtil',
      ),
    ];
  }
}

/// GoRouter 配置
final GoRouter appRouter = GoRouter(
  initialLocation: AppRoutes.home,
  routes: [
    // 首页
    GoRoute(
      path: AppRoutes.home,
      builder: (context, state) => const HomePage(),
    ),
    // 计数器
    GoRoute(
      path: AppRoutes.counter,
      builder: (context, state) => const MyCounter(),
    ),

    // Layout 布局
    GoRoute(path: AppRoutes.row, builder: (context, state) => const MyRow()),
    GoRoute(
      path: AppRoutes.column,
      builder: (context, state) => const MyColumn(),
    ),
    GoRoute(path: AppRoutes.flex, builder: (context, state) => const MyFlex()),
    GoRoute(path: AppRoutes.wrap, builder: (context, state) => const MyWrap()),
    GoRoute(path: AppRoutes.flow, builder: (context, state) => const MyFlow()),
    GoRoute(
      path: AppRoutes.stack,
      builder: (context, state) => const MyStack(),
    ),

    // Container 容器
    GoRoute(
      path: AppRoutes.container,
      builder: (context, state) => const MyContainer(),
    ),
    GoRoute(
      path: AppRoutes.padding,
      builder: (context, state) => const MyPadding(),
    ),
    GoRoute(
      path: AppRoutes.align,
      builder: (context, state) => const MyAlign(),
    ),
    GoRoute(
      path: AppRoutes.center,
      builder: (context, state) => const MyCenter(),
    ),
    GoRoute(
      path: AppRoutes.constrainedBox,
      builder: (context, state) => const MyConstrainedBox(),
    ),
    GoRoute(
      path: AppRoutes.decoratedBox,
      builder: (context, state) => const MyDecoratedBox(),
    ),
    GoRoute(
      path: AppRoutes.sizedBox,
      builder: (context, state) => const MySizedBox(),
    ),

    // 可滚动组件
    GoRoute(
      path: AppRoutes.listView,
      builder: (context, state) => const MyListView(),
    ),
    GoRoute(
      path: AppRoutes.gridView,
      builder: (context, state) => const MyGridView(),
    ),
    GoRoute(
      path: AppRoutes.scrollView,
      builder: (context, state) => const MyScrollView(),
    ),
    GoRoute(
      path: AppRoutes.pageView,
      builder: (context, state) => const MyPageView(),
    ),
    GoRoute(
      path: AppRoutes.tabBarView,
      builder: (context, state) => const MyTabBarView(),
    ),
    GoRoute(
      path: AppRoutes.animatedList,
      builder: (context, state) => const MyAnimatedList(),
    ),
    GoRoute(
      path: AppRoutes.customScrollView,
      builder: (context, state) => const MyCustomScrollView(),
    ),
    GoRoute(
      path: AppRoutes.nestedScrollView,
      builder: (context, state) => const MyNestedScrollView(),
    ),

    // 功能型组件
    GoRoute(
      path: AppRoutes.layoutBuilder,
      builder: (context, state) => const MyLayoutBuilder(),
    ),
    GoRoute(
      path: AppRoutes.gestureDetector,
      builder: (context, state) => const MyGestureDetector(),
    ),
    GoRoute(
      path: AppRoutes.popScope,
      builder: (context, state) => const MyPopScope(),
    ),
    GoRoute(
      path: AppRoutes.inheritedWidget,
      builder: (context, state) => const MyInheritedWidget(),
    ),
    GoRoute(
      path: AppRoutes.valueListenableBuilder,
      builder: (context, state) => const MyValueListenableBuilder(),
    ),
    GoRoute(
      path: AppRoutes.futureBuilder,
      builder: (context, state) => const MyFutureBuilder(),
    ),
    GoRoute(
      path: AppRoutes.streamBuilder,
      builder: (context, state) => const MyStreamBuilder(),
    ),

    // Other
    GoRoute(
      path: AppRoutes.animation,
      builder: (context, state) => const MyAnimation(),
    ),
    GoRoute(
      path: AppRoutes.dialog,
      builder: (context, state) => const MyDialog(),
    ),
    GoRoute(
      path: AppRoutes.isolate,
      builder: (context, state) => const MyIsolate(),
    ),

    // 网络请求
    GoRoute(path: AppRoutes.dio, builder: (context, state) => const MyDio()),

    // 状态管理
    GoRoute(
      path: AppRoutes.provider,
      builder: (context, state) => const MyProvider(),
    ),
    GoRoute(path: AppRoutes.getX, builder: (context, state) => const MyGet()),
    GoRoute(
      path: AppRoutes.getX2,
      builder: (context, state) => const MyGetX2(),
    ),
    GoRoute(path: AppRoutes.bloC, builder: (context, state) => const MyBloC()),

    // 三方框架
    GoRoute(
      path: AppRoutes.toast,
      builder: (context, state) => const MyToast(),
    ),
    GoRoute(
      path: AppRoutes.notification,
      builder: (context, state) => const MyNotification(),
    ),
    GoRoute(
      path: AppRoutes.sharedPreferences,
      builder: (context, state) => const MySharedPreferences(),
    ),
    GoRoute(
      path: AppRoutes.screenUtil,
      builder: (context, state) => const MyScreenUtil(),
    ),
  ],
);
