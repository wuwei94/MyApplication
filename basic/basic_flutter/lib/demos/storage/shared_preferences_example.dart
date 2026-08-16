import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Shared Preferences
/// https://pub.dev/packages/shared_preferences
class SharedPreferencesDemoPage extends StatelessWidget {
  const SharedPreferencesDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SharedPreferencesDemoView(title: title);
  }
}

class SharedPreferencesDemoView extends StatefulWidget {
  const SharedPreferencesDemoView({super.key, required this.title});

  final String title;

  @override
  State<SharedPreferencesDemoView> createState() =>
      _SharedPreferencesDemoViewState();
}

class _SharedPreferencesDemoViewState extends State<SharedPreferencesDemoView> {
  static const String _counterKey = 'counter';
  static final SharedPreferencesAsync _prefs = SharedPreferencesAsync();
  int _counter = 0;

  @override
  void initState() {
    super.initState();
    _loadCounter();
  }

  Future<void> _loadCounter() async {
    final int counter = await _prefs.getInt(_counterKey) ?? 0;

    if (!mounted) return;
    setState(() {
      _counter = counter;
    });
  }

  Future<void> _incrementCounter() async {
    final int counter = await _prefs.getInt(_counterKey) ?? 0;
    await _prefs.setInt(_counterKey, counter + 1);

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
