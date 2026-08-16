import 'package:flutter/material.dart';

/// ValueListenableBuilder
/// Demonstrates efficient rebuilding with ValueNotifier
class ValueListenableBuilderDemoPage extends StatelessWidget {
  const ValueListenableBuilderDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilderDemoView(title: title);
  }
}

class ValueListenableBuilderDemoView extends StatefulWidget {
  const ValueListenableBuilderDemoView({super.key, required this.title});

  final String title;

  @override
  State<ValueListenableBuilderDemoView> createState() =>
      _ValueListenableBuilderDemoViewState();
}

class _ValueListenableBuilderDemoViewState
    extends State<ValueListenableBuilderDemoView> {
  final ValueNotifier<int> _counter = ValueNotifier<int>(0);
  final ValueNotifier<bool> _switch = ValueNotifier<bool>(false);

  @override
  void dispose() {
    _counter.dispose();
    _switch.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text('Counter (ValueNotifier):'),
            ValueListenableBuilder<int>(
              valueListenable: _counter,
              builder: (context, value, child) {
                return Text(
                  '$value',
                  style: const TextStyle(
                    fontSize: 48,
                    fontWeight: FontWeight.bold,
                  ),
                );
              },
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                ElevatedButton(
                  onPressed: () => _counter.value--,
                  child: const Text('-'),
                ),
                const SizedBox(width: 16),
                ElevatedButton(
                  onPressed: () => _counter.value++,
                  child: const Text('+'),
                ),
              ],
            ),
            const SizedBox(height: 32),
            const Text('Switch (ValueNotifier):'),
            ValueListenableBuilder<bool>(
              valueListenable: _switch,
              builder: (context, value, child) {
                return Switch(
                  value: value,
                  onChanged: (newValue) => _switch.value = newValue,
                );
              },
            ),
            ValueListenableBuilder<bool>(
              valueListenable: _switch,
              builder: (context, value, child) {
                return Text(
                  value ? 'ON' : 'OFF',
                  style: TextStyle(
                    fontSize: 24,
                    color: value ? Colors.green : Colors.red,
                    fontWeight: FontWeight.bold,
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}
