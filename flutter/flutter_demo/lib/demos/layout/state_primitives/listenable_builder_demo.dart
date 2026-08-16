import 'package:flutter/material.dart';

/// ListenableBuilder
/// Demonstrates listening to any Listenable
class ListenableBuilderDemoPage extends StatelessWidget {
  const ListenableBuilderDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ListenableBuilderDemoView(title: title);
  }
}

class ListenableBuilderDemoView extends StatefulWidget {
  const ListenableBuilderDemoView({super.key, required this.title});

  final String title;

  @override
  State<ListenableBuilderDemoView> createState() => _ListenableBuilderDemoViewState();
}

class _ListenableBuilderDemoViewState extends State<ListenableBuilderDemoView> {
  final _counterNotifier = CounterNotifier();
  final _textNotifier = TextNotifier();

  @override
  void dispose() {
    _counterNotifier.dispose();
    _textNotifier.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // ListenableBuilder with custom notifier
            ListenableBuilder(
              listenable: _counterNotifier,
              builder: (context, child) {
                return Text(
                  'Counter: ${_counterNotifier.value}',
                  style: const TextStyle(
                    fontSize: 32,
                    fontWeight: FontWeight.bold,
                  ),
                );
              },
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                ElevatedButton(
                  onPressed: () => _counterNotifier.decrement(),
                  child: const Text('-'),
                ),
                const SizedBox(width: 16),
                ElevatedButton(
                  onPressed: () => _counterNotifier.increment(),
                  child: const Text('+'),
                ),
              ],
            ),
            const SizedBox(height: 32),
            const Divider(),
            const SizedBox(height: 32),
            // ListenableBuilder with AnimationController
            const _AnimationDemo(),
            const SizedBox(height: 32),
            const Divider(),
            const SizedBox(height: 32),
            // ListenableBuilder with multiple listenables
            ListenableBuilder(
              listenable: Listenable.merge([_counterNotifier, _textNotifier]),
              builder: (context, child) {
                return Column(
                  children: [
                    Text(
                      'Combined: ${_counterNotifier.value} - ${_textNotifier.text}',
                      style: const TextStyle(fontSize: 18),
                    ),
                  ],
                );
              },
            ),
            ElevatedButton(
              onPressed: () =>
                  _textNotifier.updateText('Updated ${DateTime.now().second}'),
              child: const Text('Update Text'),
            ),
          ],
        ),
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
      builder: (context, child) {
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
