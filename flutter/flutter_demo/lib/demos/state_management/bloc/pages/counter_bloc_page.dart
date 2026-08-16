import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_demo/demos/state_management/bloc/blocs/counter_bloc.dart';

class CounterBlocPage extends StatelessWidget {
  const CounterBlocPage({super.key, required this.title});

  final String title;

  void _incrementCounter(BuildContext context) {
    context.read<CounterBloc>().add(const CounterIncrementPressed());
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
          BlocBuilder<CounterBloc, int>(
            builder: (BuildContext context, int count) {
              return Text('$count');
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
