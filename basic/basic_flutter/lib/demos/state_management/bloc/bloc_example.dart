import 'package:basic_flutter/demos/state_management/bloc/cubits/counter_bloc_cubit.dart';
import 'package:basic_flutter/demos/state_management/bloc/observers/counter_bloc_observer.dart';
import 'package:basic_flutter/demos/state_management/bloc/pages/counter_bloc_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

/// BloC
/// https://pub.dev/packages/flutter_bloc
class BlocCounterDemoPage extends StatefulWidget {
  const BlocCounterDemoPage({super.key, required this.title});

  final String title;

  @override
  State<BlocCounterDemoPage> createState() => _BlocCounterDemoPageState();
}

class _BlocCounterDemoPageState extends State<BlocCounterDemoPage> {
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
