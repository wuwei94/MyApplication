import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/utils/ui/toast.dart';

/// Cupertino Dialogs
/// Demonstrates iOS-style dialogs
class CupertinoDialogsDemoPage extends BasicLayoutPage {
  const CupertinoDialogsDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<CupertinoDialogsDemoPage> createState() =>
      _CupertinoDialogsDemoPageState();
}

class _CupertinoDialogsDemoPageState
    extends BasicLayoutPageState<CupertinoDialogsDemoPage> {
  @override
  List<String> buildList() => const <String>[
        '1. 显示 Cupertino AlertDialog',
        '2. 显示 Cupertino ActionSheet',
        '3. 显示 Cupertino DatePicker',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _showAlertDialog();
      case 1:
        _showActionSheet();
      case 2:
        _showDatePicker();
    }
  }

  void _showAlertDialog() {
    showCupertinoDialog<void>(
      context: context,
      builder: (BuildContext dialogContext) => CupertinoAlertDialog(
        title: const Text('Cupertino Alert'),
        content: const Text('This is an iOS-style alert dialog.'),
        actions: <Widget>[
          CupertinoDialogAction(
            onPressed: () {
              Navigator.pop(dialogContext);
              showToast('Cupertino Alert · Cancel');
            },
            child: const Text('Cancel'),
          ),
          CupertinoDialogAction(
            isDefaultAction: true,
            onPressed: () {
              Navigator.pop(dialogContext);
              showToast('Cupertino Alert · OK');
            },
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  void _showActionSheet() {
    showCupertinoModalPopup<void>(
      context: context,
      builder: (BuildContext sheetContext) => CupertinoActionSheet(
        title: const Text('Action Sheet'),
        message: const Text('Choose an option'),
        actions: <Widget>[
          CupertinoActionSheetAction(
            onPressed: () {
              Navigator.pop(sheetContext);
              showToast('ActionSheet · Option 1');
            },
            child: const Text('Option 1'),
          ),
          CupertinoActionSheetAction(
            onPressed: () {
              Navigator.pop(sheetContext);
              showToast('ActionSheet · Option 2');
            },
            child: const Text('Option 2'),
          ),
          CupertinoActionSheetAction(
            onPressed: () {
              Navigator.pop(sheetContext);
              showToast('ActionSheet · Option 3');
            },
            child: const Text('Option 3'),
          ),
        ],
        cancelButton: CupertinoActionSheetAction(
          isDefaultAction: true,
          onPressed: () {
            Navigator.pop(sheetContext);
            showToast('ActionSheet · Cancel');
          },
          child: const Text('Cancel'),
        ),
      ),
    );
  }

  void _showDatePicker() {
    showCupertinoModalPopup<void>(
      context: context,
      builder: (BuildContext pickerContext) => Container(
        height: 250,
        color: Colors.white,
        child: CupertinoDatePicker(
          mode: CupertinoDatePickerMode.date,
          initialDateTime: DateTime.now(),
          onDateTimeChanged: (DateTime date) {
            showToast('Cupertino date ${date.toString().split(' ')[0]}');
          },
        ),
      ),
    );
  }

  @override
  Widget buildPreview() {
    return const Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Icon(Icons.phone_iphone, size: 48),
          SizedBox(height: 16),
          Text('CupertinoAlertDialog / ActionSheet / DatePicker'),
        ],
      ),
    );
  }
}
