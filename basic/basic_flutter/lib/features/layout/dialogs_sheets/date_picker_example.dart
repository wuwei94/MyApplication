import 'package:flutter/material.dart';

/// DatePicker Example
/// Demonstrates date and time pickers
class DatePickerExample extends StatelessWidget {
  const DatePickerExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return DatePickerRoute(title: title);
  }
}

class DatePickerRoute extends StatefulWidget {
  const DatePickerRoute({super.key, required this.title});

  final String title;

  @override
  State<DatePickerRoute> createState() => _DatePickerRouteState();
}

class _DatePickerRouteState extends State<DatePickerRoute> {
  DateTime? _selectedDate;
  TimeOfDay? _selectedTime;

  Future<void> _pickDate() async {
    final date = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime(2020),
      lastDate: DateTime(2030),
    );
    if (date != null) {
      setState(() {
        _selectedDate = date;
      });
    }
  }

  Future<void> _pickTime() async {
    final time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.now(),
    );
    if (time != null) {
      setState(() {
        _selectedTime = time;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: _pickDate,
              child: const Text('Pick Date'),
            ),
            const SizedBox(height: 16),
            Text(
              _selectedDate != null
                  ? 'Selected: ${_selectedDate!.toString().split(' ')[0]}'
                  : 'No date selected',
            ),
            const SizedBox(height: 32),
            ElevatedButton(
              onPressed: _pickTime,
              child: const Text('Pick Time'),
            ),
            const SizedBox(height: 16),
            Text(
              _selectedTime != null
                  ? 'Selected: ${_selectedTime!.format(context)}'
                  : 'No time selected',
            ),
          ],
        ),
      ),
    );
  }
}
