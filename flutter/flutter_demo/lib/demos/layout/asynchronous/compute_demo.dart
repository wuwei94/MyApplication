import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Compute
/// Demonstrates background computation using isolate
class ComputeDemoPage extends BasicLayoutPage {
  const ComputeDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<ComputeDemoPage> createState() =>
      _ComputeDemoPageState();
}

class _ComputeDemoPageState extends BasicLayoutPageState<ComputeDemoPage> {
  int _result = 0;
  bool _isCalculating = false;

  // Heavy computation function (must be top-level or static)
  static int _heavyCalculation(int n) {
    int sum = 0;
    for (int i = 0; i < n; i++) {
      sum += i;
      // Simulate heavy work
      for (int j = 0; j < 1000000; j++) {}
    }
    return sum;
  }

  @override
  List<String> buildList() => const <String>[
        '1. 主线程执行重计算',
        '2. compute 后台执行',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _runOnMainThread();
      case 1:
        _runWithCompute();
    }
  }

  Future<void> _runOnMainThread() async {
    setState(() {
      _isCalculating = true;
      _result = 0;
    });

    // This blocks the UI
    final int result = _heavyCalculation(100);

    setState(() {
      _result = result;
      _isCalculating = false;
    });
  }

  Future<void> _runWithCompute() async {
    setState(() {
      _isCalculating = true;
      _result = 0;
    });

    // This runs on background isolate
    final int result = await compute(_heavyCalculation, 100);

    setState(() {
      _result = result;
      _isCalculating = false;
    });
  }

  @override
  Widget buildPreview() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          if (_isCalculating)
            const CircularProgressIndicator()
          else
            Text(
              'Result: $_result',
              style: const TextStyle(
                fontSize: 32,
                fontWeight: FontWeight.bold,
              ),
            ),
          const SizedBox(height: 32),
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text(
              'Try running on main thread and notice the UI freezes. '
              'Then try with compute for smooth UI.',
              textAlign: TextAlign.center,
            ),
          ),
        ],
      ),
    );
  }
}
