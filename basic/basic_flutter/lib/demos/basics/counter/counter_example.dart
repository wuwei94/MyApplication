import 'package:flutter/material.dart';

class CounterDemoPage extends StatelessWidget {
  const CounterDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return CounterDemoView(title: title);
  }
}

class CounterDemoView extends StatefulWidget {
  const CounterDemoView({super.key, required this.title});

  final String title;

  @override
  State<CounterDemoView> createState() => _CounterDemoViewState();
}

class _CounterDemoViewState extends State<CounterDemoView> {
  int _counter = 0;

  void _incrementCounter() {
    setState(() => _counter++);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text('You have pushed the button this many times:'),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: const Icon(Icons.add),
      ),
    );
  }
}
