import 'dart:async';
import 'dart:isolate';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

/// Isolate
/// Demonstrates multi-threading with Isolate
class IsolateDemoPage extends StatelessWidget {
  const IsolateDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return IsolateDemoView(title: title);
  }
}

class IsolateDemoView extends StatefulWidget {
  const IsolateDemoView({super.key, required this.title});

  final String title;

  @override
  State<IsolateDemoView> createState() => _IsolateDemoViewState();
}

class _IsolateDemoViewState extends State<IsolateDemoView> {
  String _result = 'Press a button to start';
  bool _isRunning = false;
  Isolate? _isolate;
  ReceivePort? _receivePort;

  static void _heavyTask(SendPort sendPort) {
    int sum = 0;
    for (int i = 0; i < 1000000000; i++) {
      sum += i;
    }
    sendPort.send(sum);
  }

  Future<void> _spawnIsolate() async {
    setState(() {
      _isRunning = true;
      _result = 'Running in isolate...';
    });

    _receivePort = ReceivePort();

    _isolate = await Isolate.spawn(_heavyTask, _receivePort!.sendPort);

    _receivePort!.listen((message) {
      setState(() {
        _result = 'Result from isolate: $message';
        _isRunning = false;
      });
      _killIsolate();
    });
  }

  void _killIsolate() {
    _isolate?.kill(priority: Isolate.immediate);
    _isolate = null;
    _receivePort?.close();
    _receivePort = null;
  }

  void _runWithCompute() async {
    setState(() {
      _isRunning = true;
      _result = 'Running with compute...';
    });

    final result = await compute(_computeTask, 1000000000);

    setState(() {
      _result = 'Result from compute: $result';
      _isRunning = false;
    });
  }

  static int _computeTask(int n) {
    int sum = 0;
    for (int i = 0; i < n; i++) {
      sum += i;
    }
    return sum;
  }

  @override
  void dispose() {
    _killIsolate();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            if (_isRunning)
              const CircularProgressIndicator()
            else
              Container(
                padding: const EdgeInsets.all(16),
                margin: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.blue.shade50,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  _result,
                  style: const TextStyle(fontSize: 16),
                  textAlign: TextAlign.center,
                ),
              ),
            const SizedBox(height: 32),
            ElevatedButton(
              onPressed: _isRunning ? null : _spawnIsolate,
              child: const Text('Spawn Isolate'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _isRunning ? null : _runWithCompute,
              child: const Text('Run with Compute'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _killIsolate,
              style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
              child: const Text('Kill Isolate'),
            ),
          ],
        ),
      ),
    );
  }
}
