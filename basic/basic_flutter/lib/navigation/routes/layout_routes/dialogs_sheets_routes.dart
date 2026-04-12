import 'package:basic_flutter/features/layout/dialogs_sheets/alert_dialog_example.dart';
import 'package:basic_flutter/features/layout/dialogs_sheets/cupertino_dialogs_example.dart';
import 'package:basic_flutter/features/layout/dialogs_sheets/custom_dialog_example.dart';
import 'package:basic_flutter/features/layout/dialogs_sheets/date_picker_example.dart';
import 'package:basic_flutter/features/layout/dialogs_sheets/modal_bottom_sheet_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Dialogs & Sheets 路由
final List<RouteItem> dialogsSheetsRoutes = [
  RouteItem(
    path: 'alert-dialog',
    title: 'Dialogs',
    subtitle: '对话框示例',
    pageBuilder: (BuildContext context) =>
        const AlertDialogExample(title: 'Dialogs'),
  ),
  RouteItem(
    path: 'custom-dialog',
    title: 'CustomDialog',
    subtitle: '自定义对话框',
    pageBuilder: (BuildContext context) =>
        const CustomDialogExample(title: 'CustomDialog'),
  ),
  RouteItem(
    path: 'date-picker',
    title: 'DatePicker',
    subtitle: '日期选择器',
    pageBuilder: (BuildContext context) =>
        const DatePickerExample(title: 'DatePicker'),
  ),
  RouteItem(
    path: 'bottom-sheet',
    title: 'ModalBottomSheet',
    subtitle: '底部弹窗',
    pageBuilder: (BuildContext context) =>
        const ModalBottomSheetExample(title: 'ModalBottomSheet'),
  ),
  RouteItem(
    path: 'cupertino-dialogs',
    title: 'Cupertino Dialogs',
    subtitle: 'iOS风格对话框',
    pageBuilder: (BuildContext context) =>
        const CupertinoDialogsExample(title: 'Cupertino Dialogs'),
  ),
];
