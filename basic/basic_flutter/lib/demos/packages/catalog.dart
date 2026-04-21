import 'package:basic_flutter/demos/packages/auto_size_text_example.dart';
import 'package:basic_flutter/demos/packages/custom_google_font_example.dart';
import 'package:basic_flutter/demos/packages/constraint_layout_example.dart';
import 'package:basic_flutter/demos/packages/easy_refresh_example.dart';
import 'package:basic_flutter/demos/packages/extended_text_field_example.dart';
import 'package:basic_flutter/demos/packages/event_bus_example.dart';
import 'package:basic_flutter/demos/packages/flutter_linkify_example.dart';
import 'package:basic_flutter/demos/packages/intl_example.dart';
import 'package:basic_flutter/demos/packages/keyboard_visibility_example.dart';
import 'package:basic_flutter/demos/packages/notification_example.dart';
import 'package:basic_flutter/demos/packages/permission_example.dart';
import 'package:basic_flutter/demos/packages/screen_util_example.dart';
import 'package:basic_flutter/demos/packages/scroll_to_index_example.dart';
import 'package:basic_flutter/demos/packages/slidable_example.dart';
import 'package:basic_flutter/demos/packages/timeago_example.dart';
import 'package:basic_flutter/demos/packages/time_machine_example.dart';
import 'package:basic_flutter/demos/packages/toast_example.dart';
import 'package:basic_flutter/demos/packages/url_launcher_example.dart';
import 'package:basic_flutter/demos/packages/uuid_example.dart';
import 'package:basic_flutter/demos/packages/webview_example.dart';
import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// Packages 模块
///
/// 包含：Toast、Notification、Permission、WebView、ScreenUtil、
/// Google Fonts 等第三方包示例
class PackagesCatalog extends CatalogSection {
  const PackagesCatalog._();

  @override
  String get path => 'packages';

  @override
  String get title => 'Packages Example';

  @override
  String get subtitle => '三方组件';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'toast',
      title: 'Toast',
      subtitle: 'Toast示例',
      pageBuilder: (BuildContext context) =>
          const ToastDemoPage(title: 'Toast'),
    ),
    CatalogEntry.page(
      path: 'notification',
      title: 'Notification',
      subtitle: 'Notification示例',
      pageBuilder: (BuildContext context) =>
          const NotificationDemoPage(title: 'Notification'),
    ),
    CatalogEntry.page(
      path: 'event-bus',
      title: 'EventBus',
      subtitle: 'EventBus事件总线示例',
      pageBuilder: (BuildContext context) =>
          const EventBusDemoPage(title: 'EventBus'),
    ),
    CatalogEntry.page(
      path: 'uuid',
      title: 'Uuid',
      subtitle: 'UUID 生成与校验示例',
      pageBuilder: (BuildContext context) => const UuidDemoPage(title: 'Uuid'),
    ),
    CatalogEntry.page(
      path: 'permission',
      title: 'Permission',
      subtitle: 'Permission示例',
      pageBuilder: (BuildContext context) =>
          const PermissionDemoPage(title: 'Permission'),
    ),
    CatalogEntry.page(
      path: 'web-view',
      title: 'WebView',
      subtitle: 'WebView示例',
      pageBuilder: (BuildContext context) =>
          const WebViewDemoPage(title: 'WebView'),
    ),
    CatalogEntry.page(
      path: 'url-launcher',
      title: 'UrlLauncher',
      subtitle: '链接跳转与系统能力唤起示例',
      pageBuilder: (BuildContext context) =>
          const UrlLauncherDemoPage(title: 'UrlLauncher'),
    ),
    CatalogEntry.page(
      path: 'flutter-linkify',
      title: 'FlutterLinkify',
      subtitle: '正文链接自动识别示例',
      pageBuilder: (BuildContext context) =>
          const FlutterLinkifyDemoPage(title: 'FlutterLinkify'),
    ),
    CatalogEntry.page(
      path: 'intl',
      title: 'Intl',
      subtitle: '中英文日期时间格式化示例',
      pageBuilder: (BuildContext context) => const IntlDemoPage(title: 'Intl'),
    ),
    CatalogEntry.page(
      path: 'screen-util',
      title: 'ScreenUtil',
      subtitle: 'ScreenUtil示例',
      pageBuilder: (BuildContext context) =>
          const ScreenUtilDemoPage(title: 'ScreenUtil'),
    ),
    CatalogEntry.page(
      path: 'constraint-layout',
      title: 'ConstraintLayout',
      subtitle: 'flutter_constraintlayout 约束布局示例',
      pageBuilder: (BuildContext context) =>
          const ConstraintLayoutDemoPage(title: 'ConstraintLayout'),
    ),
    CatalogEntry.page(
      path: 'auto-size-text',
      title: 'AutoSizeText',
      subtitle: '自适应文本缩放示例',
      pageBuilder: (BuildContext context) =>
          const AutoSizeTextDemoPage(title: 'AutoSizeText'),
    ),
    CatalogEntry.page(
      path: 'easy-refresh',
      title: 'EasyRefresh',
      subtitle: '下拉刷新与上拉加载示例',
      pageBuilder: (BuildContext context) =>
          const EasyRefreshDemoPage(title: 'EasyRefresh'),
    ),
    CatalogEntry.page(
      path: 'extended-text-field',
      title: 'ExtendedTextField',
      subtitle: '富文本输入与特殊文本 span 示例',
      pageBuilder: (BuildContext context) =>
          const ExtendedTextFieldDemoPage(title: 'ExtendedTextField'),
    ),
    CatalogEntry.page(
      path: 'keyboard-visibility',
      title: 'KeyboardVisibility',
      subtitle: '键盘显隐监听与点击空白收起示例',
      pageBuilder: (BuildContext context) =>
          const KeyboardVisibilityDemoPage(title: 'KeyboardVisibility'),
    ),
    CatalogEntry.page(
      path: 'scroll-to-index',
      title: 'ScrollToIndex',
      subtitle: '按索引定位滚动列表项示例',
      pageBuilder: (BuildContext context) =>
          const ScrollToIndexDemoPage(title: 'ScrollToIndex'),
    ),
    CatalogEntry.page(
      path: 'slidable',
      title: 'Slidable',
      subtitle: '滑动操作列表项示例',
      pageBuilder: (BuildContext context) =>
          const SlidableDemoPage(title: 'Slidable'),
    ),
    CatalogEntry.page(
      path: 'time-machine',
      title: 'TimeMachine',
      subtitle: '日期时间、时区与文化格式化示例',
      pageBuilder: (BuildContext context) =>
          const TimeMachineDemoPage(title: 'TimeMachine'),
    ),
    CatalogEntry.page(
      path: 'timeago',
      title: 'Timeago',
      subtitle: '相对时间文案格式化示例',
      pageBuilder: (BuildContext context) =>
          const TimeagoDemoPage(title: 'Timeago'),
    ),
    CatalogEntry.page(
      path: 'custom-google-font',
      title: 'Custom Google Font',
      subtitle: 'Google Fonts 第三方字体示例',
      pageBuilder: (BuildContext context) =>
          const CustomGoogleFontDemoPage(title: 'Custom Google Font'),
    ),
  ];
}

/// 单例实例
const PackagesCatalog packagesCatalog = PackagesCatalog._();
