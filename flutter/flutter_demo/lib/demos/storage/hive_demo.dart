import 'package:flutter/material.dart';
import 'package:hive_flutter/hive_flutter.dart';

/// Hive
/// https://pub.dev/packages/hive
class HiveDemoPage extends StatelessWidget {
  const HiveDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return HiveDemoView(title: title);
  }
}

class HiveDemoView extends StatefulWidget {
  const HiveDemoView({super.key, required this.title});

  final String title;

  @override
  State<HiveDemoView> createState() => _HiveDemoViewState();
}

class _HiveDemoViewState extends State<HiveDemoView> {
  static const String _counterKey = 'counter';
  static const String _boxName = 'hive_demo_box';
  static Future<void>? _initFuture;
  static Box<dynamic>? _box;
  int _counter = 0;

  static Future<Box<dynamic>> _getBox() async {
    await (_initFuture ??= _init());
    return _box!;
  }

  static Future<void> _init() async {
    WidgetsFlutterBinding.ensureInitialized();
    await Hive.initFlutter();
    _box = await Hive.openBox<dynamic>(_boxName);
  }

  @override
  void initState() {
    super.initState();
    _loadCounter();
  }

  Future<void> _loadCounter() async {
    final Box<dynamic> box = await _getBox();
    final int counter = box.get(_counterKey, defaultValue: 0) as int;

    if (!mounted) return;
    setState(() {
      _counter = counter;
    });
  }

  Future<void> _incrementCounter() async {
    final Box<dynamic> box = await _getBox();
    final int counter = box.get(_counterKey, defaultValue: 0) as int;
    await box.put(_counterKey, counter + 1);

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
