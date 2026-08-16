import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_demo/core/utils/logger/logger.dart';

/// {@template counter_cubit_observer}
/// Custom [BlocObserver] that observes cubit state changes.
/// {@endtemplate}
class CounterCubitObserver extends BlocObserver {
  /// {@macro counter_cubit_observer}
  const CounterCubitObserver();

  @override
  void onChange(BlocBase<dynamic> bloc, Change<dynamic> change) {
    super.onChange(bloc, change);
    if (bloc is Cubit<dynamic>) {
      logDebug(change);
    }
  }
}
