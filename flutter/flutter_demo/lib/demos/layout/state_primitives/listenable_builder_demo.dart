import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// ListenableBuilder
/// Demonstrates listening to any Listenable
class ListenableBuilderDemoPage extends BasicLayoutPage {
  const ListenableBuilderDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<ListenableBuilderDemoPage> createState() =>
      _ListenableBuilderDemoPageState();
}

class _ListenableBuilderDemoPageState
    extends BasicLayoutPageState<ListenableBuilderDemoPage> {
  final CounterNotifier _counterNotifier = CounterNotifier();
  final TextNotifier _textNotifier = TextNotifier();

  @override
  List<String> buildList() => const <String>[
        '1. 计数器加一',
        '2. 计数器减一',
        '3. 更新文本',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _counterNotifier.increment();
      case 1:
        _counterNotifier.decrement();
      case 2:
        _textNotifier.updateText('Updated ${DateTime.now().second}');
    }
  }

  @override
  void dispose() {
    _counterNotifier.dispose();
    _textNotifier.dispose();
    super.dispose();
  }

  @override
  Widget buildPreview() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          ListenableBuilder(
            listenable: _counterNotifier,
            builder: (BuildContext context, Widget? child) {
              return Text(
                'Counter: ${_counterNotifier.value}',
                style: const TextStyle(
                  fontSize: 32,
                  fontWeight: FontWeight.bold,
                ),
              );
            },
          ),
          const SizedBox(height: 32),
          const Divider(),
          const SizedBox(height: 32),
          const _AnimationDemo(),
          const SizedBox(height: 32),
          const Divider(),
          const SizedBox(height: 32),
          ListenableBuilder(
            listenable: Listenable.merge(
              <Listenable>[_counterNotifier, _textNotifier],
            ),
            builder: (BuildContext context, Widget? child) {
              return Text(
                'Combined: ${_counterNotifier.value} - ${_textNotifier.text}',
                style: const TextStyle(fontSize: 18),
              );
            },
          ),
        ],
      ),
    );
  }
}

class CounterNotifier extends ChangeNotifier {
  int _value = 0;

  int get value => _value;

  void increment() {
    _value++;
    notifyListeners();
  }

  void decrement() {
    _value--;
    notifyListeners();
  }
}

class TextNotifier extends ChangeNotifier {
  String _text = 'Initial';

  String get text => _text;

  void updateText(String newText) {
    _text = newText;
    notifyListeners();
  }
}

class _AnimationDemo extends StatefulWidget {
  const _AnimationDemo();

  @override
  State<_AnimationDemo> createState() => _AnimationDemoState();
}

class _AnimationDemoState extends State<_AnimationDemo>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(seconds: 2),
      vsync: this,
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _controller,
      builder: (BuildContext context, Widget? child) {
        return Container(
          width: 100 + (_controller.value * 100),
          height: 50,
          color: Colors.blue,
          child: const Center(
            child: Text('Animation', style: TextStyle(color: Colors.white)),
          ),
        );
      },
    );
  }
}
