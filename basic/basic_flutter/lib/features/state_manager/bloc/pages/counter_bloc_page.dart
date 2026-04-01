import 'package:basic_flutter/features/state_manager/bloc/cubits/counter_bloc_cubit.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class CounterBlocPage extends StatelessWidget {
  const CounterBlocPage({super.key, required this.title});

  final String title;

  void _incrementCounter(BuildContext context) {
    final CounterBlocCubit cubit = context.read<CounterBlocCubit>();
    cubit.increment();
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
          BlocBuilder<CounterBlocCubit, int>(
            builder: (context, count) {
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
