import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/utils/ui/toast.dart';

/// DatePicker
/// Demonstrates date and time pickers
class DatePickerDemoPage extends BasicLayoutPage {
  const DatePickerDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<DatePickerDemoPage> createState() =>
      _DatePickerDemoPageState();
}

class _DatePickerDemoPageState extends BasicLayoutPageState<DatePickerDemoPage> {
  DateTime? _selectedDate;
  TimeOfDay? _selectedTime;

  @override
  List<String> buildList() => const <String>[
        '1. 选择日期',
        '2. 选择时间',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _pickDate();
      case 1:
        _pickTime();
    }
  }

  Future<void> _pickDate() async {
    final DateTime? date = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime(2020),
      lastDate: DateTime(2030),
    );
    if (date != null) {
      setState(() {
        _selectedDate = date;
      });
      showToast('已选择日期 ${date.toString().split(' ')[0]}');
    }
  }

  Future<void> _pickTime() async {
    final TimeOfDay? time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.now(),
    );
    if (time != null) {
      if (!mounted) {
        return;
      }
      setState(() {
        _selectedTime = time;
      });
      showToast('已选择时间 ${time.format(context)}');
    }
  }

  @override
  Widget buildPreview() {
    final DateTime? selectedDate = _selectedDate;
    final TimeOfDay? selectedTime = _selectedTime;
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          const Text('Selected Date'),
          const SizedBox(height: 8),
          Text(
            selectedDate != null
                ? selectedDate.toString().split(' ')[0]
                : 'No date selected',
          ),
          const SizedBox(height: 32),
          const Text('Selected Time'),
          const SizedBox(height: 8),
          Text(
            selectedTime != null
                ? selectedTime.format(context)
                : 'No time selected',
          ),
        ],
      ),
    );
  }
}
