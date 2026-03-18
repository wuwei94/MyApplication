import 'package:basic_flutter/features/state_management/riverpod/providers/counter_riverpod_provider.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class CounterRiverpodPage extends ConsumerWidget {
  const CounterRiverpodPage({super.key, required this.title});

  final String title;

  void _incrementCounter(WidgetRef ref) {
    final StateController<int> controller = ref.read(
      counterRiverpodProvider.notifier,
    );
    controller.state++;
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: getBody(ref),
      floatingActionButton: getFAB(ref),
    );
  }

  Widget getBody(WidgetRef ref) {
    final int count = ref.watch(counterRiverpodProvider);

    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text('You have pushed the button this many times:'),
          Text('$count'),
        ],
      ),
    );
  }

  Widget getFAB(WidgetRef ref) {
    return FloatingActionButton(
      onPressed: () => _incrementCounter(ref),
      tooltip: 'increment',
      child: const Icon(Icons.add),
    );
  }
}
