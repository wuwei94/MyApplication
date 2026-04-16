import 'package:flutter/material.dart';

/// InheritedWidget Example
/// Demonstrates state sharing across widget tree
class InheritedWidgetDemoPage extends StatelessWidget {
  const InheritedWidgetDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return InheritedWidgetDemoView(title: title);
  }
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
    return context.dependOnInheritedWidgetOfExactType<AppData>()!;
  }

  @override
  bool updateShouldNotify(AppData oldWidget) {
    return counter != oldWidget.counter;
  }
}

class InheritedWidgetDemoView extends StatefulWidget {
  const InheritedWidgetDemoView({super.key, required this.title});

  final String title;

  @override
  State<InheritedWidgetDemoView> createState() => _InheritedWidgetDemoViewState();
}

class _InheritedWidgetDemoViewState extends State<InheritedWidgetDemoView> {
  int _counter = 0;

  void _increment() {
    setState(() => _counter++);
  }

  @override
  Widget build(BuildContext context) {
    return AppData(
      counter: _counter,
      increment: _increment,
      child: Scaffold(
        appBar: AppBar(title: Text(widget.title)),
        body: const Column(
          children: [
            _CounterDisplay(),
            SizedBox(height: 16),
            _DeepChildWidget(),
          ],
        ),
        floatingActionButton: const _IncrementButton(),
      ),
    );
  }
}

class _CounterDisplay extends StatelessWidget {
  const _CounterDisplay();

  @override
  Widget build(BuildContext context) {
    final data = AppData.of(context);
    return Container(
      padding: const EdgeInsets.all(16),
      margin: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade100,
        borderRadius: BorderRadius.circular(8),
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
        borderRadius: BorderRadius.circular(8),
      ),
      child: const Column(
        children: [
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
    final data = AppData.of(context);
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
    final data = AppData.of(context);
    return FloatingActionButton(
      onPressed: data.increment,
      child: const Icon(Icons.add),
    );
  }
}
