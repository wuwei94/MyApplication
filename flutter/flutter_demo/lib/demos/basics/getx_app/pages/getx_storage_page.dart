import 'package:flutter/material.dart';
import 'package:flutter_demo/demos/basics/getx_app/utils/getx_storage_utils.dart';

class GetXStoragePage extends StatelessWidget {
  const GetXStoragePage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return StorageDemoView(title: title);
  }
}

class StorageDemoView extends StatefulWidget {
  const StorageDemoView({super.key, required this.title});

  final String title;

  @override
  State<StorageDemoView> createState() => _StorageDemoViewState();
}

class _StorageDemoViewState extends State<StorageDemoView> {
  static const String _counterKey = 'counter';
  int _counter = 0;

  @override
  void initState() {
    super.initState();
    _loadCounter();
  }

  Future<void> _loadCounter() async {
    final counter = await GetXStorageUtils.getValue<int>(_counterKey, 0);

    if (!mounted) return;
    setState(() {
      _counter = counter;
    });
  }

  Future<void> _incrementCounter() async {
    final counter = await GetXStorageUtils.getValue<int>(_counterKey, 0);
    await GetXStorageUtils.setValue(_counterKey, counter + 1);

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
