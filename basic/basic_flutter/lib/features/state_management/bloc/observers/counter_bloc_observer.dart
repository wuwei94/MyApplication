import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

/// {@template counter_bloc_observer}
/// Custom [BlocObserver] that observes all bloc and cubit state_management changes.
/// {@endtemplate}
class CounterBlocObserver extends BlocObserver {
  /// {@macro counter_bloc_observer}
  const CounterBlocObserver();

  @override
  void onChange(BlocBase<dynamic> bloc, Change<dynamic> change) {
    super.onChange(bloc, change);
    if (bloc is Cubit) {
      logDebug(change);
    }
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
