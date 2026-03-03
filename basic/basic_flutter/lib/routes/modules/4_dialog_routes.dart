import 'package:basic_flutter/features/4_dialog/2_simple_dialog_page.dart';
import 'package:basic_flutter/features/4_dialog/1_alert_dialog_page.dart';
import 'package:basic_flutter/features/4_dialog/3_custom_dialog_page.dart';
import 'package:basic_flutter/features/4_dialog/4_modal_bottom_sheet_page.dart';
import 'package:basic_flutter/features/4_dialog/5_date_picker_dialog_page.dart';
import 'package:basic_flutter/features/4_dialog/6_cupertino_alert_dialog_page.dart';
import 'package:basic_flutter/features/4_dialog/7_cupertino_action_sheet_page.dart';
import 'package:basic_flutter/features/4_dialog/8_cupertino_date_picker_dialog_page.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Dialog 对话框路由
final List<RouteItem> dialogRoutes = [
  RouteItem(
    name: 'SimpleDialog',
    path: '/dialog/simple',
    describe: '简单对话框',
    builder: (BuildContext context, _) => const SimpleDialogPage(),
  ),
  RouteItem(
    name: 'AlertDialog',
    path: '/dialog/alert',
    describe: '警告对话框',
    builder: (BuildContext context, _) => const AlertDialogPage(),
  ),
  RouteItem(
    name: 'CustomDialog',
    path: '/dialog/custom',
    describe: '自定义对话框',
    builder: (BuildContext context, _) => const CustomDialogPage(),
  ),
  RouteItem(
    name: 'ModalBottomSheet',
    path: '/dialog/modal-bottom-sheet',
    describe: '底部弹出模态框',
    builder: (BuildContext context, _) => const ModalBottomSheetPage(),
  ),
  RouteItem(
    name: 'DatePickerDialog',
    path: '/dialog/date-picker',
    describe: '日期选择对话框',
    builder: (BuildContext context, _) => const DatePickerDialogPage(),
  ),
  RouteItem(
    name: 'CupertinoAlertDialog',
    path: '/dialog/cupertino-alert',
    describe: 'iOS风格警告对话框',
    builder: (BuildContext context, _) => const CupertinoAlertDialogPage(),
  ),
  RouteItem(
    name: 'CupertinoActionSheet',
    path: '/dialog/cupertino-action-sheet',
    describe: 'iOS风格底部操作表',
    builder: (BuildContext context, _) => const CupertinoActionSheetPage(),
  ),
  RouteItem(
    name: 'CupertinoDatePickerDialog',
    path: '/dialog/cupertino-date-picker',
    describe: 'iOS风格日期选择对话框',
    builder: (BuildContext context, _) => const CupertinoDatePickerDialogPage(),
  ),
];
