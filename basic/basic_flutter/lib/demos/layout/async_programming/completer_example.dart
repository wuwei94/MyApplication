import 'dart:async';

import 'package:flutter/material.dart';

/// Completer Example
/// Demonstrates manual Future completion
class CompleterExample extends StatelessWidget {
  const CompleterExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return CompleterRoute(title: title);
  }
}

class CompleterRoute extends StatefulWidget {
  const CompleterRoute({super.key, required this.title});

  final String title;

  @override
  State<CompleterRoute> createState() => _CompleterRouteState();
}

class _CompleterRouteState extends State<CompleterRoute> {
  String _status = 'Idle';
  Completer<String>? _completer;

  Future<String> _asyncOperation() {
    _completer = Completer<String>();
    setState(() => _status = 'Operation started...');
    return _completer!.future;
  }

  void _startOperation() {
    _asyncOperation()
        .then((result) {
          setState(() => _status = 'Completed: $result');
        })
        .catchError((Object error) {
          setState(() => _status = 'Error: $error');
        });
  }

  void _completeSuccess() {
    _completer?.complete('Success at ${DateTime.now()}');
  }

  void _completeError() {
    _completer?.completeError('Failed at ${DateTime.now()}');
  }

  void _timeoutOperation() {
    final completer = Completer<String>();

    Timer(const Duration(seconds: 3), () {
      if (!completer.isCompleted) {
        completer.complete('Auto-completed after timeout');
      }
    });

    completer.future.then((result) {
      setState(() => _status = 'Timeout result: $result');
    });

    setState(() => _status = 'Waiting for timeout...');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.blue.shade50,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(
                'Status: $_status',
                style: const TextStyle(fontSize: 16),
              ),
            ),
            const SizedBox(height: 24),
            const Text(
              'Manual Completion:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                ElevatedButton(
                  onPressed: _startOperation,
                  child: const Text('Start'),
                ),
                const SizedBox(width: 8),
                ElevatedButton(
                  onPressed: _completeSuccess,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.green,
                  ),
                  child: const Text('Complete'),
                ),
                const SizedBox(width: 8),
                ElevatedButton(
                  onPressed: _completeError,
                  style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                  child: const Text('Error'),
                ),
              ],
            ),
            const SizedBox(height: 24),
            const Text(
              'Auto Timeout:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            ElevatedButton(
              onPressed: _timeoutOperation,
              child: const Text('Start with Timeout'),
            ),
          ],
        ),
      ),
    );
  }
}
