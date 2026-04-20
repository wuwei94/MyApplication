import 'package:flutter_bloc/flutter_bloc.dart';

abstract class CounterBlocEvent {
  const CounterBlocEvent();
}

class CounterIncrementPressed extends CounterBlocEvent {
  const CounterIncrementPressed();
}

class CounterDecrementPressed extends CounterBlocEvent {
  const CounterDecrementPressed();
}

class CounterResetPressed extends CounterBlocEvent {
  const CounterResetPressed();
}

class CounterBloc extends Bloc<CounterBlocEvent, int> {
  CounterBloc() : super(0) {
    on<CounterIncrementPressed>(_onIncrementPressed);
    on<CounterDecrementPressed>(_onDecrementPressed);
    on<CounterResetPressed>(_onResetPressed);
  }

  void _onIncrementPressed(CounterIncrementPressed event, Emitter<int> emit) {
    emit(state + 1);
  }

  void _onDecrementPressed(CounterDecrementPressed event, Emitter<int> emit) {
    emit(state - 1);
  }

  void _onResetPressed(CounterResetPressed event, Emitter<int> emit) {
    emit(0);
  }
}
