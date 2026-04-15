import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

/// Cupertino Dialogs Example
/// Demonstrates iOS-style dialogs
class CupertinoDialogsExample extends StatelessWidget {
  const CupertinoDialogsExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return CupertinoDialogsRoute(title: title);
  }
}

class CupertinoDialogsRoute extends StatelessWidget {
  const CupertinoDialogsRoute({super.key, required this.title});

  final String title;

  void _showAlertDialog(BuildContext context) {
    showCupertinoDialog<void>(
      context: context,
      builder: (context) => CupertinoAlertDialog(
        title: const Text('Cupertino Alert'),
        content: const Text('This is an iOS-style alert dialog.'),
        actions: [
          CupertinoDialogAction(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          CupertinoDialogAction(
            isDefaultAction: true,
            onPressed: () => Navigator.pop(context),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  void _showActionSheet(BuildContext context) {
    showCupertinoModalPopup<void>(
      context: context,
      builder: (context) => CupertinoActionSheet(
        title: const Text('Action Sheet'),
        message: const Text('Choose an option'),
        actions: [
          CupertinoActionSheetAction(
            onPressed: () => Navigator.pop(context),
            child: const Text('Option 1'),
          ),
          CupertinoActionSheetAction(
            onPressed: () => Navigator.pop(context),
            child: const Text('Option 2'),
          ),
          CupertinoActionSheetAction(
            onPressed: () => Navigator.pop(context),
            child: const Text('Option 3'),
          ),
        ],
        cancelButton: CupertinoActionSheetAction(
          isDefaultAction: true,
          onPressed: () => Navigator.pop(context),
          child: const Text('Cancel'),
        ),
      ),
    );
  }

  void _showDatePicker(BuildContext context) {
    showCupertinoModalPopup<void>(
      context: context,
      builder: (context) => Container(
        height: 250,
        color: Colors.white,
        child: CupertinoDatePicker(
          mode: CupertinoDatePickerMode.date,
          initialDateTime: DateTime.now(),
          onDateTimeChanged: (date) {
            // Handle date change
          },
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: () => _showAlertDialog(context),
              child: const Text('Cupertino AlertDialog'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => _showActionSheet(context),
              child: const Text('Cupertino ActionSheet'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => _showDatePicker(context),
              child: const Text('Cupertino DatePicker'),
            ),
          ],
        ),
      ),
    );
  }
}
