import 'package:flutter/material.dart';

class DatePickerDialogPage extends StatelessWidget {
  const DatePickerDialogPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('DatePickerDialog')),
      body: Center(
        child: ElevatedButton(
          onPressed: () => _showDatePickerDialog(context),
          child: const Text('显示 DatePickerDialog'),
        ),
      ),
    );
  }

  Future<void> _showDatePickerDialog(BuildContext context) async {
    final DateTime date = DateTime.now();
    await showDatePicker(
      context: context,
      initialDate: date,
      firstDate: date,
      lastDate: date.add(const Duration(days: 30)),
    );
  }
}
