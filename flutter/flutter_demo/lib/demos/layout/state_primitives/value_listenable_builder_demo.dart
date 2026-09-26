import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// ValueListenableBuilder
/// Demonstrates efficient rebuilding with ValueNotifier
class ValueListenableBuilderDemoPage extends BasicLayoutPage {
  const ValueListenableBuilderDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<ValueListenableBuilderDemoPage> createState() =>
      _ValueListenableBuilderDemoPageState();
}

class _ValueListenableBuilderDemoPageState
    extends BasicLayoutPageState<ValueListenableBuilderDemoPage> {
  final ValueNotifier<int> _counter = ValueNotifier<int>(0);
  final ValueNotifier<bool> _switch = ValueNotifier<bool>(false);

  @override
  List<String> buildList() => const <String>[
        '1. 计数器加一',
        '2. 计数器减一',
        '3. 切换开关',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _counter.value++;
      case 1:
        _counter.value--;
      case 2:
        _switch.value = !_switch.value;
    }
  }

  @override
  void dispose() {
    _counter.dispose();
    _switch.dispose();
    super.dispose();
  }

  @override
  Widget buildPreview() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          const Text('Counter (ValueNotifier):'),
          ValueListenableBuilder<int>(
            valueListenable: _counter,
            builder: (BuildContext context, int value, Widget? child) {
              return Text(
                '$value',
                style: const TextStyle(
                  fontSize: 48,
                  fontWeight: FontWeight.bold,
                ),
              );
            },
          ),
          const SizedBox(height: 32),
          const Text('Switch (ValueNotifier):'),
          ValueListenableBuilder<bool>(
            valueListenable: _switch,
            builder: (BuildContext context, bool value, Widget? child) {
              return Switch(
                value: value,
                onChanged: (bool newValue) => _switch.value = newValue,
              );
            },
          ),
          ValueListenableBuilder<bool>(
            valueListenable: _switch,
            builder: (BuildContext context, bool value, Widget? child) {
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
    );
  }
}
