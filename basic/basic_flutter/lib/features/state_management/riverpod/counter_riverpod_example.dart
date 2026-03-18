import 'package:basic_flutter/features/state_management/riverpod/pages/counter_riverpod_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Riverpod
/// https://pub.dev/packages/flutter_riverpod
class CounterRiverpodExample extends StatelessWidget {
  const CounterRiverpodExample({super.key});

  @override
  Widget build(BuildContext context) {
    return const ProviderScope(
      child: CounterRiverpodPage(title: 'Riverpod Example'),
    );
  }
}
