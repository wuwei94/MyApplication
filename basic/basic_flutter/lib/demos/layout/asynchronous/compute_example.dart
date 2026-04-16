import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

/// Compute Example
/// Demonstrates background computation using isolate
class ComputeDemoPage extends StatelessWidget {
  const ComputeDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ComputeDemoView(title: title);
  }
}

class ComputeDemoView extends StatefulWidget {
  const ComputeDemoView({super.key, required this.title});

  final String title;

  @override
  State<ComputeDemoView> createState() => _ComputeDemoViewState();
}

class _ComputeDemoViewState extends State<ComputeDemoView> {
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

  Future<void> _runOnMainThread() async {
    setState(() {
      _isCalculating = true;
      _result = 0;
    });

    // This blocks the UI
    final result = _heavyCalculation(100);

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
    final result = await compute(_heavyCalculation, 100);

    setState(() {
      _result = result;
      _isCalculating = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
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
            ElevatedButton(
              onPressed: _isCalculating ? null : _runOnMainThread,
              child: const Text('Run on Main Thread (Blocks UI)'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _isCalculating ? null : _runWithCompute,
              child: const Text('Run with Compute (Background)'),
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
      ),
    );
  }
}
