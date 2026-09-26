import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/utils/ui/toast.dart';

/// AlertDialog
/// Demonstrates various dialog types
class AlertDialogDemoPage extends BasicLayoutPage {
  const AlertDialogDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<AlertDialogDemoPage> createState() =>
      _AlertDialogDemoPageState();
}

class _AlertDialogDemoPageState
    extends BasicLayoutPageState<AlertDialogDemoPage> {
  String _lastResult = '尚未触发对话框';

  @override
  List<String> buildList() => const <String>[
        '1. 显示 AlertDialog',
        '2. 显示 SimpleDialog',
        '3. 显示 BottomSheet',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _showAlertDialog();
      case 1:
        _showSimpleDialog();
      case 2:
        _showBottomSheet();
    }
  }

  void _setResult(String value) {
    setState(() {
      _lastResult = value;
    });
    showToast(value);
  }

  void _showAlertDialog() {
    showDialog<void>(
      context: context,
      builder: (BuildContext dialogContext) => AlertDialog(
        title: const Text('Alert'),
        content: const Text('This is an alert dialog.'),
        actions: <Widget>[
          TextButton(
            onPressed: () {
              Navigator.pop(dialogContext);
              _setResult('AlertDialog · Cancel');
            },
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(dialogContext);
              _setResult('AlertDialog · OK');
            },
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  void _showSimpleDialog() {
    showDialog<void>(
      context: context,
      builder: (BuildContext dialogContext) => SimpleDialog(
        title: const Text('Select Option'),
        children: <Widget>[
          SimpleDialogOption(
            onPressed: () {
              Navigator.pop(dialogContext);
              _setResult('SimpleDialog · Option 1');
            },
            child: const Text('Option 1'),
          ),
          SimpleDialogOption(
            onPressed: () {
              Navigator.pop(dialogContext);
              _setResult('SimpleDialog · Option 2');
            },
            child: const Text('Option 2'),
          ),
          SimpleDialogOption(
            onPressed: () {
              Navigator.pop(dialogContext);
              _setResult('SimpleDialog · Option 3');
            },
            child: const Text('Option 3'),
          ),
        ],
      ),
    );
  }

  void _showBottomSheet() {
    showModalBottomSheet<void>(
      context: context,
      builder: (BuildContext sheetContext) => Container(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            const Text(
              'Bottom Sheet',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            ListTile(
              leading: const Icon(Icons.share),
              title: const Text('Share'),
              onTap: () {
                Navigator.pop(sheetContext);
                _setResult('BottomSheet · Share');
              },
            ),
            ListTile(
              leading: const Icon(Icons.link),
              title: const Text('Copy Link'),
              onTap: () {
                Navigator.pop(sheetContext);
                _setResult('BottomSheet · Copy Link');
              },
            ),
            ListTile(
              leading: const Icon(Icons.delete),
              title: const Text('Delete'),
              onTap: () {
                Navigator.pop(sheetContext);
                _setResult('BottomSheet · Delete');
              },
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget buildPreview() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          const Icon(Icons.chat_bubble_outline, size: 48),
          const SizedBox(height: 16),
          const Text('AlertDialog / SimpleDialog / BottomSheet'),
          const SizedBox(height: 16),
          Text(_lastResult),
        ],
      ),
    );
  }
}
