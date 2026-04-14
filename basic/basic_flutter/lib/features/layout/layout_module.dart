import 'package:basic_flutter/features/layout/animations/fade_transition_example.dart';
import 'package:basic_flutter/features/layout/animations/rotation_transition_example.dart';
import 'package:basic_flutter/features/layout/animations/scale_transition_example.dart';
import 'package:basic_flutter/features/layout/animations/size_transition_example.dart';
import 'package:basic_flutter/features/layout/animations/slide_transition_example.dart';
import 'package:basic_flutter/features/layout/async_programming/completer_example.dart';
import 'package:basic_flutter/features/layout/async_programming/compute_example.dart';
import 'package:basic_flutter/features/layout/async_programming/future_example.dart';
import 'package:basic_flutter/features/layout/async_programming/isolate_example.dart';
import 'package:basic_flutter/features/layout/async_programming/stream_example.dart';
import 'package:basic_flutter/features/layout/decoration_effects/backdrop_filter_example.dart';
import 'package:basic_flutter/features/layout/decoration_effects/clip_example.dart';
import 'package:basic_flutter/features/layout/decoration_effects/decoratedbox_example.dart';
import 'package:basic_flutter/features/layout/decoration_effects/opacity_example.dart';
import 'package:basic_flutter/features/layout/decoration_effects/shader_mask_example.dart';
import 'package:basic_flutter/features/layout/dialogs_sheets/alert_dialog_example.dart';
import 'package:basic_flutter/features/layout/dialogs_sheets/cupertino_dialogs_example.dart';
import 'package:basic_flutter/features/layout/dialogs_sheets/custom_dialog_example.dart';
import 'package:basic_flutter/features/layout/dialogs_sheets/date_picker_example.dart';
import 'package:basic_flutter/features/layout/dialogs_sheets/modal_bottom_sheet_example.dart';
import 'package:basic_flutter/features/layout/flow_layout/flow_example.dart';
import 'package:basic_flutter/features/layout/flow_layout/wrap_example.dart';
import 'package:basic_flutter/features/layout/gesture_interaction/gesturedetector_example.dart';
import 'package:basic_flutter/features/layout/gesture_interaction/pop_scope_example.dart';
import 'package:basic_flutter/features/layout/layout_builder/layout_builder_example.dart';
import 'package:basic_flutter/features/layout/layout_containers/align_example.dart';
import 'package:basic_flutter/features/layout/layout_containers/center_example.dart';
import 'package:basic_flutter/features/layout/layout_containers/constrainedbox_example.dart';
import 'package:basic_flutter/features/layout/layout_containers/container_example.dart';
import 'package:basic_flutter/features/layout/layout_containers/padding_example.dart';
import 'package:basic_flutter/features/layout/layout_containers/sizedbox_example.dart';
import 'package:basic_flutter/features/layout/linear_layout/column_example.dart';
import 'package:basic_flutter/features/layout/linear_layout/flexible_expanded_example.dart';
import 'package:basic_flutter/features/layout/linear_layout/row_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/animatedlist_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/custom_scrollview_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/gridview_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/listview_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/nested_scrollview_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/pageview_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/single_child_scrollview_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/tabbarview_example.dart';
import 'package:basic_flutter/features/layout/sliver_widgets/sliver_appbar_example.dart';
import 'package:basic_flutter/features/layout/sliver_widgets/sliver_grid_example.dart';
import 'package:basic_flutter/features/layout/sliver_widgets/sliver_list_example.dart';
import 'package:basic_flutter/features/layout/stack_positioning/positioned_example.dart';
import 'package:basic_flutter/features/layout/stack_positioning/stack_example.dart';
import 'package:basic_flutter/features/layout/state_driven/futurebuilder_example.dart';
import 'package:basic_flutter/features/layout/state_driven/streambuilder_example.dart';
import 'package:basic_flutter/features/layout/state_management/inherited_widget_example.dart';
import 'package:basic_flutter/features/layout/state_management/listenable_builder_example.dart';
import 'package:basic_flutter/features/layout/state_management/value_listenable_builder_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Layout 模块
///
/// 包含：容器布局、线性布局、堆叠定位、流式布局、滚动组件、Sliver组件、
/// 手势交互、动画效果、弹窗面板、装饰效果、布局构建器、状态驱动、状态管理、异步编程
class LayoutModule {
  const LayoutModule._();

  /// 首页目录入口
  RouteItem get catalog => RouteItem.section(
        path: '/layout',
        title: 'Layout',
        subtitle: '布局组件',
        routeItems: routes,
      );

  /// 所有路由列表
  List<RouteItem> get routes => _routes;

  static final List<RouteItem> _routes = [
    // ========== 容器布局 ==========
    RouteItem.section(
      path: '/layout/containers',
      title: '容器布局',
      subtitle: 'Container、Padding、Center、Align、SizedBox、ConstrainedBox',
      routeItems: [
        RouteItem.page(
          path: 'container',
          title: 'Container',
          subtitle: '容器组件',
          pageBuilder: (BuildContext context) =>
              const ContainerExample(title: 'Container'),
        ),
        RouteItem.page(
          path: 'padding',
          title: 'Padding',
          subtitle: '内边距组件',
          pageBuilder: (BuildContext context) =>
              const PaddingExample(title: 'Padding'),
        ),
        RouteItem.page(
          path: 'center',
          title: 'Center',
          subtitle: '居中组件',
          pageBuilder: (BuildContext context) =>
              const CenterExample(title: 'Center'),
        ),
        RouteItem.page(
          path: 'align',
          title: 'Align',
          subtitle: '对齐组件',
          pageBuilder: (BuildContext context) =>
              const AlignExample(title: 'Align'),
        ),
        RouteItem.page(
          path: 'sized-box',
          title: 'SizedBox',
          subtitle: '尺寸组件',
          pageBuilder: (BuildContext context) =>
              const SizedBoxExample(title: 'SizedBox'),
        ),
        RouteItem.page(
          path: 'constrained-box',
          title: 'ConstrainedBox',
          subtitle: '约束组件',
          pageBuilder: (BuildContext context) =>
              const ConstrainedBoxExample(title: 'ConstrainedBox'),
        ),
      ],
    ),

    // ========== 线性布局 ==========
    RouteItem.section(
      path: '/layout/linear',
      title: '线性布局',
      subtitle: 'Row、Column、Flexible、Expanded',
      routeItems: [
        RouteItem.page(
          path: 'row',
          title: 'Row',
          subtitle: '水平布局',
          pageBuilder: (BuildContext context) => const RowExample(title: 'Row'),
        ),
        RouteItem.page(
          path: 'column',
          title: 'Column',
          subtitle: '垂直布局',
          pageBuilder: (BuildContext context) =>
              const ColumnExample(title: 'Column'),
        ),
        RouteItem.page(
          path: 'flexible-expanded',
          title: 'Flexible & Expanded',
          subtitle: '弹性布局',
          pageBuilder: (BuildContext context) =>
              const FlexibleExpandedExample(title: 'Flexible & Expanded'),
        ),
      ],
    ),

    // ========== 堆叠定位 ==========
    RouteItem.section(
      path: '/layout/stacking',
      title: '堆叠定位',
      subtitle: 'Stack、Positioned',
      routeItems: [
        RouteItem.page(
          path: 'stack',
          title: 'Stack',
          subtitle: '堆叠布局',
          pageBuilder: (BuildContext context) =>
              const StackExample(title: 'Stack'),
        ),
        RouteItem.page(
          path: 'positioned',
          title: 'Positioned',
          subtitle: '定位组件',
          pageBuilder: (BuildContext context) =>
              const PositionedExample(title: 'Positioned'),
        ),
      ],
    ),

    // ========== 流式布局 ==========
    RouteItem.section(
      path: '/layout/flow',
      title: '流式布局',
      subtitle: 'Wrap、Flow',
      routeItems: [
        RouteItem.page(
          path: 'wrap',
          title: 'Wrap',
          subtitle: '自动换行',
          pageBuilder: (BuildContext context) => const WrapExample(title: 'Wrap'),
        ),
        RouteItem.page(
          path: 'flow',
          title: 'Flow',
          subtitle: '流式布局',
          pageBuilder: (BuildContext context) => const FlowExample(title: 'Flow'),
        ),
      ],
    ),

    // ========== 滚动组件 ==========
    RouteItem.section(
      path: '/layout/scroll',
      title: '滚动组件',
      subtitle: 'ListView、GridView、PageView、TabBarView、NestedScrollView...',
      routeItems: [
        RouteItem.page(
          path: 'list-view',
          title: 'ListView',
          subtitle: '列表滚动组件',
          pageBuilder: (BuildContext context) =>
              const ListViewExample(title: 'ListView'),
        ),
        RouteItem.page(
          path: 'grid-view',
          title: 'GridView',
          subtitle: '网格滚动组件',
          pageBuilder: (BuildContext context) =>
              const GridViewExample(title: 'GridView'),
        ),
        RouteItem.page(
          path: 'tab-bar-view',
          title: 'TabBarView',
          subtitle: '标签页组件',
          pageBuilder: (BuildContext context) =>
              const TabBarViewExample(title: 'TabBarView'),
        ),
        RouteItem.page(
          path: 'nested-scroll-view',
          title: 'NestedScrollView',
          subtitle: '嵌套滚动组件',
          pageBuilder: (BuildContext context) =>
              const NestedScrollViewExample(title: 'NestedScrollView'),
        ),
        RouteItem.page(
          path: 'animated-list',
          title: 'AnimatedList',
          subtitle: '动画列表',
          pageBuilder: (BuildContext context) =>
              const AnimatedListExample(title: 'AnimatedList'),
        ),
        RouteItem.page(
          path: 'page-view',
          title: 'PageView',
          subtitle: '页面滑动',
          pageBuilder: (BuildContext context) =>
              const PageViewExample(title: 'PageView'),
        ),
        RouteItem.page(
          path: 'single-child-scroll-view',
          title: 'SingleChildScrollView',
          subtitle: '单孩子滚动',
          pageBuilder: (BuildContext context) =>
              const SingleChildScrollViewExample(title: 'SingleChildScrollView'),
        ),
        RouteItem.page(
          path: 'custom-scroll-view',
          title: 'CustomScrollView',
          subtitle: '自定义滚动',
          pageBuilder: (BuildContext context) =>
              const CustomScrollViewExample(title: 'CustomScrollView'),
        ),
      ],
    ),

    // ========== Sliver 组件 ==========
    RouteItem.section(
      path: '/layout/slivers',
      title: 'Sliver 组件',
      subtitle: 'SliverList、SliverGrid、SliverAppBar',
      routeItems: [
        RouteItem.page(
          path: 'sliver-list',
          title: 'SliverList',
          subtitle: 'Sliver列表',
          pageBuilder: (BuildContext context) =>
              const SliverListExample(title: 'SliverList'),
        ),
        RouteItem.page(
          path: 'sliver-grid',
          title: 'SliverGrid',
          subtitle: 'Sliver网格',
          pageBuilder: (BuildContext context) =>
              const SliverGridExample(title: 'SliverGrid'),
        ),
        RouteItem.page(
          path: 'sliver-app-bar',
          title: 'SliverAppBar',
          subtitle: 'Sliver应用栏',
          pageBuilder: (BuildContext context) =>
              const SliverAppBarExample(title: 'SliverAppBar'),
        ),
      ],
    ),

    // ========== 手势交互 ==========
    RouteItem.section(
      path: '/layout/gestures',
      title: '手势交互',
      subtitle: 'GestureDetector、PopScope',
      routeItems: [
        RouteItem.page(
          path: 'gesture-detector',
          title: 'GestureDetector',
          subtitle: '手势检测',
          pageBuilder: (BuildContext context) =>
              const GestureDetectorExample(title: 'GestureDetector'),
        ),
        RouteItem.page(
          path: 'pop-scope',
          title: 'PopScope',
          subtitle: '返回拦截',
          pageBuilder: (BuildContext context) =>
              const PopScopeExample(title: 'PopScope'),
        ),
      ],
    ),

    // ========== 动画效果 ==========
    RouteItem.section(
      path: '/layout/animations',
      title: '动画效果',
      subtitle: 'FadeTransition、ScaleTransition、RotationTransition...',
      routeItems: [
        RouteItem.page(
          path: 'fade-transition',
          title: 'FadeTransition',
          subtitle: '淡入淡出动画',
          pageBuilder: (BuildContext context) =>
              const FadeTransitionExample(title: 'FadeTransition'),
        ),
        RouteItem.page(
          path: 'scale-transition',
          title: 'ScaleTransition',
          subtitle: '缩放动画',
          pageBuilder: (BuildContext context) =>
              const ScaleTransitionExample(title: 'ScaleTransition'),
        ),
        RouteItem.page(
          path: 'rotation-transition',
          title: 'RotationTransition',
          subtitle: '旋转动画',
          pageBuilder: (BuildContext context) =>
              const RotationTransitionExample(title: 'RotationTransition'),
        ),
        RouteItem.page(
          path: 'size-transition',
          title: 'SizeTransition',
          subtitle: '尺寸动画',
          pageBuilder: (BuildContext context) =>
              const SizeTransitionExample(title: 'SizeTransition'),
        ),
        RouteItem.page(
          path: 'slide-transition',
          title: 'SlideTransition',
          subtitle: '滑动动画',
          pageBuilder: (BuildContext context) =>
              const SlideTransitionExample(title: 'SlideTransition'),
        ),
      ],
    ),

    // ========== 弹窗与底部面板 ==========
    RouteItem.section(
      path: '/layout/dialogs',
      title: '弹窗与底部面板',
      subtitle: 'Dialog、BottomSheet、DatePicker、Cupertino Dialogs',
      routeItems: [
        RouteItem.page(
          path: 'alert-dialog',
          title: 'AlertDialog',
          subtitle: 'Material弹窗',
          pageBuilder: (BuildContext context) =>
              const AlertDialogExample(title: 'AlertDialog'),
        ),
        RouteItem.page(
          path: 'cupertino-dialogs',
          title: 'Cupertino Dialogs',
          subtitle: 'iOS风格弹窗',
          pageBuilder: (BuildContext context) =>
              const CupertinoDialogsExample(title: 'Cupertino Dialogs'),
        ),
        RouteItem.page(
          path: 'custom-dialog',
          title: 'Custom Dialog',
          subtitle: '自定义弹窗',
          pageBuilder: (BuildContext context) =>
              const CustomDialogExample(title: 'Custom Dialog'),
        ),
        RouteItem.page(
          path: 'date-picker',
          title: 'DatePicker',
          subtitle: '日期选择器',
          pageBuilder: (BuildContext context) =>
              const DatePickerExample(title: 'DatePicker'),
        ),
        RouteItem.page(
          path: 'modal-bottom-sheet',
          title: 'ModalBottomSheet',
          subtitle: '底部面板',
          pageBuilder: (BuildContext context) =>
              const ModalBottomSheetExample(title: 'ModalBottomSheet'),
        ),
      ],
    ),

    // ========== 装饰效果 ==========
    RouteItem.section(
      path: '/layout/decorations',
      title: '装饰效果',
      subtitle: 'DecoratedBox、Opacity、Clip、BackdropFilter、ShaderMask',
      routeItems: [
        RouteItem.page(
          path: 'decorated-box',
          title: 'DecoratedBox',
          subtitle: '装饰盒子',
          pageBuilder: (BuildContext context) =>
              const DecoratedBoxExample(title: 'DecoratedBox'),
        ),
        RouteItem.page(
          path: 'opacity',
          title: 'Opacity',
          subtitle: '透明度',
          pageBuilder: (BuildContext context) =>
              const OpacityExample(title: 'Opacity'),
        ),
        RouteItem.page(
          path: 'clip',
          title: 'Clip',
          subtitle: '裁剪',
          pageBuilder: (BuildContext context) => const ClipExample(title: 'Clip'),
        ),
        RouteItem.page(
          path: 'backdrop-filter',
          title: 'BackdropFilter',
          subtitle: '背景滤镜',
          pageBuilder: (BuildContext context) =>
              const BackdropFilterExample(title: 'BackdropFilter'),
        ),
        RouteItem.page(
          path: 'shader-mask',
          title: 'ShaderMask',
          subtitle: '着色器遮罩',
          pageBuilder: (BuildContext context) =>
              const ShaderMaskExample(title: 'ShaderMask'),
        ),
      ],
    ),

    // ========== 布局构建器 ==========
    RouteItem.section(
      path: '/layout/builders',
      title: '布局构建器',
      subtitle: 'LayoutBuilder',
      routeItems: [
        RouteItem.page(
          path: 'layout-builder',
          title: 'LayoutBuilder',
          subtitle: '响应式布局',
          pageBuilder: (BuildContext context) =>
              const LayoutBuilderExample(title: 'LayoutBuilder'),
        ),
      ],
    ),

    // ========== 状态驱动组件 ==========
    RouteItem.section(
      path: '/layout/state-driven',
      title: '状态驱动组件',
      subtitle: 'FutureBuilder、StreamBuilder',
      routeItems: [
        RouteItem.page(
          path: 'future-builder',
          title: 'FutureBuilder',
          subtitle: 'Future构建器',
          pageBuilder: (BuildContext context) =>
              const FutureBuilderExample(title: 'FutureBuilder'),
        ),
        RouteItem.page(
          path: 'stream-builder',
          title: 'StreamBuilder',
          subtitle: 'Stream构建器',
          pageBuilder: (BuildContext context) =>
              const StreamBuilderExample(title: 'StreamBuilder'),
        ),
      ],
    ),

    // ========== 状态管理 ==========
    RouteItem.section(
      path: '/layout/state-management',
      title: '状态管理',
      subtitle: 'InheritedWidget、ValueListenableBuilder、ListenableBuilder',
      routeItems: [
        RouteItem.page(
          path: 'inherited-widget',
          title: 'InheritedWidget',
          subtitle: '数据共享',
          pageBuilder: (BuildContext context) =>
              const InheritedWidgetExample(title: 'InheritedWidget'),
        ),
        RouteItem.page(
          path: 'value-listenable-builder',
          title: 'ValueListenableBuilder',
          subtitle: '值监听构建器',
          pageBuilder: (BuildContext context) =>
              const ValueListenableBuilderExample(title: 'ValueListenableBuilder'),
        ),
        RouteItem.page(
          path: 'listenable-builder',
          title: 'ListenableBuilder',
          subtitle: '可监听构建器',
          pageBuilder: (BuildContext context) =>
              const ListenableBuilderExample(title: 'ListenableBuilder'),
        ),
      ],
    ),

    // ========== 异步编程 ==========
    RouteItem.section(
      path: '/layout/async',
      title: '异步编程',
      subtitle: 'Future、Stream、Compute、Completer、Isolate',
      routeItems: [
        RouteItem.page(
          path: 'future',
          title: 'Future',
          subtitle: '异步任务',
          pageBuilder: (BuildContext context) =>
              const FutureExample(title: 'Future'),
        ),
        RouteItem.page(
          path: 'stream',
          title: 'Stream',
          subtitle: '异步流',
          pageBuilder: (BuildContext context) =>
              const StreamExample(title: 'Stream'),
        ),
        RouteItem.page(
          path: 'compute',
          title: 'Compute',
          subtitle: '计算隔离',
          pageBuilder: (BuildContext context) =>
              const ComputeExample(title: 'Compute'),
        ),
        RouteItem.page(
          path: 'completer',
          title: 'Completer',
          subtitle: '异步完成器',
          pageBuilder: (BuildContext context) =>
              const CompleterExample(title: 'Completer'),
        ),
        RouteItem.page(
          path: 'isolate',
          title: 'Isolate',
          subtitle: '多线程',
          pageBuilder: (BuildContext context) =>
              const IsolateExample(title: 'Isolate'),
        ),
      ],
    ),
  ];
}

/// 单例实例
const LayoutModule layoutModule = LayoutModule._();
