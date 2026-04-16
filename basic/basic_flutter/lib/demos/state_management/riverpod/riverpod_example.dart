import 'package:basic_flutter/demos/state_management/riverpod/pages/counter_riverpod_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Riverpod
/// https://pub.dev/packages/flutter_riverpod
class RiverpodCounterDemoPage extends StatelessWidget {
  const RiverpodCounterDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ProviderScope(child: CounterRiverpodPage(title: title));
  }
}
