import 'package:basic_flutter/demos/state_manager/bloc/cubits/counter_bloc_cubit.dart';
import 'package:basic_flutter/demos/state_manager/bloc/observers/counter_bloc_observer.dart';
import 'package:basic_flutter/demos/state_manager/bloc/pages/counter_bloc_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

/// BloC
/// https://pub.dev/packages/flutter_bloc
class CounterBlocExample extends StatefulWidget {
  const CounterBlocExample({super.key, required this.title});

  final String title;

  @override
  State<CounterBlocExample> createState() => _CounterBlocExampleState();
}

class _CounterBlocExampleState extends State<CounterBlocExample> {
  late final BlocObserver _previousObserver;

  @override
  void initState() {
    super.initState();
    _previousObserver = Bloc.observer;
    Bloc.observer = const CounterBlocObserver();
  }

  @override
  void dispose() {
    Bloc.observer = _previousObserver;
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => CounterBlocCubit(),
      child: CounterBlocPage(title: widget.title),
    );
  }
}
