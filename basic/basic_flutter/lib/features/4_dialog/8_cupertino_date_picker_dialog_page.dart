import 'package:basic_flutter/core/utils/logger/log.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class CupertinoDatePickerDialogPage extends StatelessWidget {
  const CupertinoDatePickerDialogPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('CupertinoDatePickerDialog')),
      body: Center(
        child: ElevatedButton(
          onPressed: () => _showCupertinoDatePickerDialog(context),
          child: const Text('显示 CupertinoDatePickerDialog'),
        ),
      ),
    );
  }

  Future<void> _showCupertinoDatePickerDialog(BuildContext context) async {
    final DateTime date = DateTime.now();
    await showCupertinoModalPopup<void>(
      context: context,
      builder: (BuildContext ctx) {
        return Container(
          height: 200,
          color: Colors.white,
          child: CupertinoDatePicker(
            mode: CupertinoDatePickerMode.dateAndTime,
            minimumDate: date,
            maximumDate: date.add(const Duration(days: 30)),
            maximumYear: date.year + 1,
            onDateTimeChanged: (DateTime value) {
              logDebug(value);
            },
          ),
        );
      },
    );
  }
}
