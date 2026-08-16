import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Flutter Secure Storage
/// https://pub.dev/packages/flutter_secure_storage
class SecureStorageDemoPage extends StatelessWidget {
  const SecureStorageDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SecureStorageDemoView(title: title);
  }
}

class SecureStorageDemoView extends StatefulWidget {
  const SecureStorageDemoView({super.key, required this.title});

  final String title;

  @override
  State<SecureStorageDemoView> createState() => _SecureStorageDemoViewState();
}

class _SecureStorageDemoViewState extends State<SecureStorageDemoView> {
  static const String _counterKey = 'counter';
  static const FlutterSecureStorage _storage = FlutterSecureStorage();
  int _counter = 0;

  @override
  void initState() {
    super.initState();
    _loadCounter();
  }

  Future<void> _loadCounter() async {
    final String? value = await _storage.read(key: _counterKey);
    final int counter = int.tryParse(value ?? '') ?? 0;

    if (!mounted) return;
    setState(() {
      _counter = counter;
    });
  }

  Future<void> _incrementCounter() async {
    final String? value = await _storage.read(key: _counterKey);
    final int counter = int.tryParse(value ?? '') ?? 0;
    await _storage.write(key: _counterKey, value: '${counter + 1}');

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
