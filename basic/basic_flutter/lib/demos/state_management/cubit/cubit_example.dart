import 'package:basic_flutter/demos/state_management/cubit/cubits/counter_cubit.dart';
import 'package:basic_flutter/demos/state_management/cubit/observers/counter_cubit_observer.dart';
import 'package:basic_flutter/demos/state_management/cubit/pages/counter_cubit_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

/// Cubit
/// https://pub.dev/packages/flutter_bloc
class CubitCounterDemoPage extends StatefulWidget {
  const CubitCounterDemoPage({super.key, required this.title});

  final String title;

  @override
  State<CubitCounterDemoPage> createState() => _CubitCounterDemoPageState();
}

class _CubitCounterDemoPageState extends State<CubitCounterDemoPage> {
  late final BlocObserver _previousObserver;

  @override
  void initState() {
    super.initState();
    _previousObserver = Bloc.observer;
    Bloc.observer = const CounterCubitObserver();
  }

  @override
  void dispose() {
    Bloc.observer = _previousObserver;
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => CounterCubit(),
      child: CounterCubitPage(title: widget.title),
    );
  }
}
