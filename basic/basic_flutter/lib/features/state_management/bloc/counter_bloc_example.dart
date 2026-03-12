import 'package:basic_flutter/features/state_management/bloc/cubits/counter_bloc_cubit.dart';
import 'package:basic_flutter/features/state_management/bloc/pages/counter_bloc_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

/// BloC
/// https://pub.dev/packages/flutter_bloc
class CounterBlocExample extends StatelessWidget {
  const CounterBlocExample({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => CounterBlocCubit(),
      child: const CounterBlocPage(title: 'BloC Example'),
    );
  }
}
