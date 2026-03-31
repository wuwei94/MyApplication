import 'package:basic_flutter/core/utils/storage/hive_storage.dart';
import 'package:flutter/material.dart';

/// Hive
/// https://pub.dev/packages/hive
class HiveExample extends StatelessWidget {
  const HiveExample({super.key});

  @override
  Widget build(BuildContext context) {
    return const HiveRoute(title: 'Hive Example');
  }
}

class HiveRoute extends StatefulWidget {
  const HiveRoute({super.key, required this.title});

  final String title;

  @override
  State<StatefulWidget> createState() => _HiveRouteState();
}

class _HiveRouteState extends State<HiveRoute> {
  static const String _counterKey = 'counter';
  int _counter = 0;

  @override
  void initState() {
    super.initState();
    _loadCounter();
  }

  Future<void> _loadCounter() async {
    final int counter = await HiveStorageUtils.getValue<int>(_counterKey, 0);

    if (!mounted) return;
    setState(() {
      _counter = counter;
    });
  }

  Future<void> _incrementCounter() async {
    final int counter = await HiveStorageUtils.getValue<int>(_counterKey, 0);
    await HiveStorageUtils.setValue(_counterKey, counter + 1);

    if (!mounted) return;
    setState(() {
      _counter = counter + 1;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
      floatingActionButton: getFAB(),
    );
  }

  Widget getBody() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text('You have pushed the button this many times:'),
          Text('$_counter'),
        ],
      ),
    );
  }

  Widget getFAB() {
    return FloatingActionButton(
      onPressed: _incrementCounter,
      tooltip: 'Increment',
      child: const Icon(Icons.add),
    );
  }
}
