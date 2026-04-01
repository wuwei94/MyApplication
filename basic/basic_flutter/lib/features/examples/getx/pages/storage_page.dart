import 'package:basic_flutter/features/examples/getx/utils/storage_utils.dart';
import 'package:flutter/material.dart';

class StoragePage extends StatelessWidget {
  const StoragePage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return StorageRoute(title: title);
  }
}

class StorageRoute extends StatefulWidget {
  const StorageRoute({super.key, required this.title});

  final String title;

  @override
  State<StatefulWidget> createState() => _StorageRouteState();
}

class _StorageRouteState extends State<StorageRoute> {
  static const String _counterKey = 'counter';
  int _counter = 0;

  @override
  void initState() {
    super.initState();
    _loadCounter();
  }

  Future<void> _loadCounter() async {
    final counter = await StorageUtils.getValue<int>(_counterKey, 0);

    if (!mounted) return;
    setState(() {
      _counter = counter;
    });
  }

  Future<void> _incrementCounter() async {
    final counter = await StorageUtils.getValue<int>(_counterKey, 0);
    await StorageUtils.setValue(_counterKey, counter + 1);

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
          Text("$_counter"),
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
