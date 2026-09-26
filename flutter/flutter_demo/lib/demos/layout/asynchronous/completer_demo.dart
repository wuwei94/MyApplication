import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Completer
/// Demonstrates manual Future completion
class CompleterDemoPage extends BasicLayoutPage {
  const CompleterDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<CompleterDemoPage> createState() =>
      _CompleterDemoPageState();
}

class _CompleterDemoPageState extends BasicLayoutPageState<CompleterDemoPage> {
  String _status = 'Idle';
  Completer<String>? _completer;

  @override
  List<String> buildList() => const <String>[
        '1. 启动手动完成操作',
        '2. 成功完成',
        '3. 失败完成',
        '4. 启动超时自动完成',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _startOperation();
      case 1:
        _completeSuccess();
      case 2:
        _completeError();
      case 3:
        _timeoutOperation();
    }
  }

  Future<String> _asyncOperation() {
    final Completer<String> completer = Completer<String>();
    _completer = completer;
    setState(() => _status = 'Operation started...');
    return completer.future;
  }

  void _startOperation() {
    _asyncOperation()
        .then((String result) {
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
    final Completer<String> completer = Completer<String>();

    Timer(const Duration(seconds: 3), () {
      if (!completer.isCompleted) {
        completer.complete('Auto-completed after timeout');
      }
    });

    completer.future.then((String result) {
      setState(() => _status = 'Timeout result: $result');
    });

    setState(() => _status = 'Waiting for timeout...');
  }

  @override
  Widget buildPreview() {
    return Padding(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.blue.shade50,
          borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
        ),
        child: Text(
          'Status: $_status',
          style: const TextStyle(fontSize: 16),
        ),
      ),
    );
  }
}
