import 'package:flutter_bloc/flutter_bloc.dart';

/// {@template counter_cubit}
/// A simple [Cubit] that manages an `int` as its state.
/// {@endtemplate}
class CounterBlocCubit extends Cubit<int> {
  CounterBlocCubit() : super(0);

  void increment() => emit(state + 1);

  void decrement() => emit(state - 1);

  void reset() => emit(0);
}
