import 'package:basic_flutter/core/utils/storage/secure_storage.dart';
import 'package:flutter/material.dart';

/// Flutter Secure Storage
/// https://pub.dev/packages/flutter_secure_storage
class SecureStorageExample extends StatelessWidget {
  const SecureStorageExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SecureStorageRoute(title: title);
  }
}

class SecureStorageRoute extends StatefulWidget {
  const SecureStorageRoute({super.key, required this.title});

  final String title;

  @override
  State<StatefulWidget> createState() => _SecureStorageRouteState();
}

class _SecureStorageRouteState extends State<SecureStorageRoute> {
  static const String _counterKey = 'counter';
  int _counter = 0;

  @override
  void initState() {
    super.initState();
    _loadCounter();
  }

  Future<void> _loadCounter() async {
    final int counter = await SecureStorageUtils.getValue<int>(_counterKey, 0);

    if (!mounted) return;
    setState(() {
      _counter = counter;
    });
  }

  Future<void> _incrementCounter() async {
    final int counter = await SecureStorageUtils.getValue<int>(_counterKey, 0);
    await SecureStorageUtils.setValue(_counterKey, counter + 1);

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
