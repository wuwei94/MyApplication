import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/dialogs_sheets/alert_dialog_example.dart';
import 'package:basic_flutter/demos/layout/dialogs_sheets/cupertino_dialogs_example.dart';
import 'package:basic_flutter/demos/layout/dialogs_sheets/custom_dialog_example.dart';
import 'package:basic_flutter/demos/layout/dialogs_sheets/date_picker_example.dart';
import 'package:basic_flutter/demos/layout/dialogs_sheets/modal_bottom_sheet_example.dart';
import 'package:flutter/widgets.dart';

final CatalogItem dialogsSheetsCatalog = CatalogItem.catalog(
  path: '/layout/dialogs',
  title: '弹窗与底部面板',
  subtitle: 'Dialog、BottomSheet、DatePicker、Cupertino Dialogs',
  children: <CatalogItem>[
    CatalogItem.page(
      path: '/layout/dialogs/alert-dialog',
      title: 'AlertDialog',
      subtitle: 'Material弹窗',
      pageBuilder: (BuildContext context) =>
          const AlertDialogExample(title: 'AlertDialog'),
    ),
    CatalogItem.page(
      path: '/layout/dialogs/cupertino-dialogs',
      title: 'Cupertino Dialogs',
      subtitle: 'iOS风格弹窗',
      pageBuilder: (BuildContext context) =>
          const CupertinoDialogsExample(title: 'Cupertino Dialogs'),
    ),
    CatalogItem.page(
      path: '/layout/dialogs/custom-dialog',
      title: 'Custom Dialog',
      subtitle: '自定义弹窗',
      pageBuilder: (BuildContext context) =>
          const CustomDialogExample(title: 'Custom Dialog'),
    ),
    CatalogItem.page(
      path: '/layout/dialogs/date-picker',
      title: 'DatePicker',
      subtitle: '日期选择器',
      pageBuilder: (BuildContext context) =>
          const DatePickerExample(title: 'DatePicker'),
    ),
    CatalogItem.page(
      path: '/layout/dialogs/modal-bottom-sheet',
      title: 'ModalBottomSheet',
      subtitle: '底部面板',
      pageBuilder: (BuildContext context) =>
          const ModalBottomSheetExample(title: 'ModalBottomSheet'),
    ),
  ],
);
