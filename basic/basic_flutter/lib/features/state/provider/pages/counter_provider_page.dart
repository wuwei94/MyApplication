import 'package:basic_flutter/features/state/provider/notifiers/counter_provider_change_notifier.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class CounterProviderPage extends StatelessWidget {
  const CounterProviderPage({super.key, required this.title});

  final String title;

  void _incrementCounter(BuildContext context) {
    context.read<CounterProviderChangeNotifier>().increment();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: getBody(),
      floatingActionButton: getFAB(context),
    );
  }

  Widget getBody() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text('You have pushed the button this many times:'),
          Consumer<CounterProviderChangeNotifier>(
            builder: (context, counter, child) {
              return Text('${counter.value}');
            },
          ),
        ],
      ),
    );
  }

  Widget getFAB(BuildContext context) {
    return FloatingActionButton(
      onPressed: () => _incrementCounter(context),
      tooltip: 'increment',
      child: const Icon(Icons.add),
    );
  }
}
