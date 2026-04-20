import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

/// {@template counter_bloc_observer}
/// Custom [BlocObserver] that observes bloc events and transitions.
/// {@endtemplate}
class CounterBlocObserver extends BlocObserver {
  /// {@macro counter_bloc_observer}
  const CounterBlocObserver();

  @override
  void onEvent(Bloc<dynamic, dynamic> bloc, Object? event) {
    super.onEvent(bloc, event);
    logDebug(event);
  }

  @override
  void onTransition(
    Bloc<dynamic, dynamic> bloc,
    Transition<dynamic, dynamic> transition,
  ) {
    super.onTransition(bloc, transition);
    logDebug(transition);
  }
}
