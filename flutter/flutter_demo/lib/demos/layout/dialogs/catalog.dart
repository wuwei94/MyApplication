import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/demos/layout/dialogs/alert_dialog_demo.dart';
import 'package:flutter_demo/demos/layout/dialogs/cupertino_dialogs_demo.dart';
import 'package:flutter_demo/demos/layout/dialogs/custom_dialog_demo.dart';
import 'package:flutter_demo/demos/layout/dialogs/date_picker_demo.dart';
import 'package:flutter_demo/demos/layout/dialogs/modal_bottom_sheet_demo.dart';

final CatalogEntry dialogsCatalog = CatalogEntry.catalog(
  path: 'dialogs',
  title: '弹窗与模态组件',
  subtitle: 'Dialog、BottomSheet、DatePicker、Cupertino Dialogs',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'alert-dialog',
      title: 'AlertDialog',
      subtitle: 'Material弹窗',
      pageBuilder: (BuildContext context) =>
          const AlertDialogDemoPage(title: 'AlertDialog'),
    ),
    CatalogEntry.page(
      path: 'cupertino-dialogs',
      title: 'Cupertino Dialogs',
      subtitle: 'iOS风格弹窗',
      pageBuilder: (BuildContext context) =>
          const CupertinoDialogsDemoPage(title: 'Cupertino Dialogs'),
    ),
    CatalogEntry.page(
      path: 'custom-dialog',
      title: 'Custom Dialog',
      subtitle: '自定义弹窗',
      pageBuilder: (BuildContext context) =>
          const CustomDialogDemoPage(title: 'Custom Dialog'),
    ),
    CatalogEntry.page(
      path: 'date-picker',
      title: 'DatePicker',
      subtitle: '日期选择器',
      pageBuilder: (BuildContext context) =>
          const DatePickerDemoPage(title: 'DatePicker'),
    ),
    CatalogEntry.page(
      path: 'modal-bottom-sheet',
      title: 'ModalBottomSheet',
      subtitle: '底部面板',
      pageBuilder: (BuildContext context) =>
          const ModalBottomSheetDemoPage(title: 'ModalBottomSheet'),
    ),
  ],
);
