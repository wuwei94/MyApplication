import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// InheritedWidget
/// Demonstrates state sharing across widget tree
class InheritedWidgetDemoPage extends BasicLayoutPage {
  const InheritedWidgetDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<InheritedWidgetDemoPage> createState() =>
      _InheritedWidgetDemoPageState();
}

class AppData extends InheritedWidget {
  const AppData({
    super.key,
    required this.counter,
    required this.increment,
    required super.child,
  });

  final int counter;
  final VoidCallback increment;

  static AppData of(BuildContext context) {
    final AppData? data = context.dependOnInheritedWidgetOfExactType<AppData>();
    if (data == null) {
      throw StateError('AppData not found in widget tree');
    }
    return data;
  }

  @override
  bool updateShouldNotify(AppData oldWidget) {
    return counter != oldWidget.counter;
  }
}

class _InheritedWidgetDemoPageState
    extends BasicLayoutPageState<InheritedWidgetDemoPage> {
  int _counter = 0;

  void _increment() {
    setState(() => _counter++);
  }

  @override
  List<String> buildList() => const <String>[
        '1. 计数器加一',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _increment();
    }
  }

  @override
  Widget buildPreview() {
    return AppData(
      counter: _counter,
      increment: _increment,
      child: const Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          _CounterDisplay(),
          SizedBox(height: 16),
          _DeepChildWidget(),
          SizedBox(height: 16),
          _IncrementButton(),
        ],
      ),
    );
  }
}

class _CounterDisplay extends StatelessWidget {
  const _CounterDisplay();

  @override
  Widget build(BuildContext context) {
    final AppData data = AppData.of(context);
    return Container(
      padding: const EdgeInsets.all(16),
      margin: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade100,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
      ),
      child: Text(
        'Counter: ${data.counter}',
        style: const TextStyle(fontSize: 24),
      ),
    );
  }
}

class _DeepChildWidget extends StatelessWidget {
  const _DeepChildWidget();

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      margin: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.green.shade100,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
      ),
      child: const Column(
        children: <Widget>[
          Text('Deep Child Widget'),
          SizedBox(height: 8),
          _DeepestWidget(),
        ],
      ),
    );
  }
}

class _DeepestWidget extends StatelessWidget {
  const _DeepestWidget();

  @override
  Widget build(BuildContext context) {
    final AppData data = AppData.of(context);
    return Text(
      'Access from deep tree: ${data.counter}',
      style: const TextStyle(fontWeight: FontWeight.bold),
    );
  }
}

class _IncrementButton extends StatelessWidget {
  const _IncrementButton();

  @override
  Widget build(BuildContext context) {
    final AppData data = AppData.of(context);
    return FloatingActionButton(
      onPressed: data.increment,
      child: const Icon(Icons.add),
    );
  }
}
