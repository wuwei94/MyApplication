import 'dart:async';
import 'dart:isolate';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Isolate
/// Demonstrates multi-threading with Isolate
class IsolateDemoPage extends BasicLayoutPage {
  const IsolateDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<IsolateDemoPage> createState() =>
      _IsolateDemoPageState();
}

class _IsolateDemoPageState extends BasicLayoutPageState<IsolateDemoPage> {
  String _result = 'Tap an action to start';
  bool _isRunning = false;
  Isolate? _isolate;
  ReceivePort? _receivePort;

  @override
  List<String> buildList() => const <String>[
        '1. Spawn Isolate 执行',
        '2. compute 后台执行',
        '3. 结束 Isolate',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _spawnIsolate();
      case 1:
        _runWithCompute();
      case 2:
        _killIsolate();
        setState(() {
          _isRunning = false;
          _result = 'Isolate killed';
        });
    }
  }

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

    final ReceivePort receivePort = ReceivePort();
    _receivePort = receivePort;

    _isolate = await Isolate.spawn(_heavyTask, receivePort.sendPort);

    receivePort.listen((dynamic message) {
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

  Future<void> _runWithCompute() async {
    setState(() {
      _isRunning = true;
      _result = 'Running with compute...';
    });

    final int result = await compute(_computeTask, 1000000000);

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
  Widget buildPreview() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          if (_isRunning)
            const CircularProgressIndicator()
          else
            Container(
              padding: const EdgeInsets.all(16),
              margin: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.blue.shade50,
                borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
              ),
              child: Text(
                _result,
                style: const TextStyle(fontSize: 16),
                textAlign: TextAlign.center,
              ),
            ),
        ],
      ),
    );
  }
}
