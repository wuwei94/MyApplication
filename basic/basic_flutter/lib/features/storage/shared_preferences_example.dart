import 'package:basic_flutter/core/utils/storage/shared_preferences.dart';
import 'package:flutter/material.dart';

/// Shared Preferences
/// https://pub.dev/packages/shared_preferences
class SharedPreferencesExample extends StatelessWidget {
  const SharedPreferencesExample({super.key});

  @override
  Widget build(BuildContext context) {
    return const SharedPreferencesRoute(title: 'SharedPreferences Example');
  }
}

class SharedPreferencesRoute extends StatefulWidget {
  const SharedPreferencesRoute({super.key, required this.title});

  final String title;

  @override
  State<StatefulWidget> createState() => _SharedPreferencesRouteState();
}

class _SharedPreferencesRouteState extends State<SharedPreferencesRoute> {
  static const String _counterKey = 'counter';
  int _counter = 0;

  @override
  void initState() {
    super.initState();
    _loadCounter();
  }

  Future<void> _loadCounter() async {
    final counter = await SharedPreferencesUtils.getValue<int>(_counterKey, 0);

    if (!mounted) return;
    setState(() {
      _counter = counter;
    });
  }

  Future<void> _incrementCounter() async {
    final counter = await SharedPreferencesUtils.getValue<int>(_counterKey, 0);
    await SharedPreferencesUtils.setValue(_counterKey, counter + 1);

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
