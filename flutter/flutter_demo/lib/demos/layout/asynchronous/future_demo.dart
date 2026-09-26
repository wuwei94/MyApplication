import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Future
/// Demonstrates Future usage patterns
class FutureDemoPage extends BasicLayoutPage {
  const FutureDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<FutureDemoPage> createState() => _FutureDemoPageState();
}

class _FutureDemoPageState extends BasicLayoutPageState<FutureDemoPage> {
  String _result = 'Tap an action to start';

  @override
  List<String> buildList() => const <String>[
        '1. 执行 .then/.catchError',
        '2. 执行 async/await',
        '3. 执行 .whenComplete',
        '4. 执行 .timeout',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _thenCatch();
      case 1:
        _asyncAwait();
      case 2:
        _whenComplete();
      case 3:
        _timeout();
    }
  }

  Future<String> _delayedOperation() async {
    await Future<void>.delayed(const Duration(seconds: 2));
    return 'Operation completed!';
  }

  void _thenCatch() {
    setState(() => _result = 'Loading...');
    _delayedOperation()
        .then((String value) {
          setState(() => _result = 'Then: $value');
        })
        .catchError((Object error) {
          setState(() => _result = 'Error: $error');
        });
  }

  Future<void> _asyncAwait() async {
    setState(() => _result = 'Loading...');
    try {
      final String result = await _delayedOperation();
      setState(() => _result = 'Await: $result');
    } on Exception catch (e) {
      setState(() => _result = 'Error: $e');
    }
  }

  void _whenComplete() {
    setState(() => _result = 'Loading...');
    _delayedOperation().whenComplete(() {
      setState(() => _result = 'WhenComplete: Done (success or error)');
    });
  }

  void _timeout() {
    setState(() => _result = 'Loading...');
    Future<void>.delayed(const Duration(seconds: 5))
        .timeout(const Duration(seconds: 1))
        .then((_) => setState(() => _result = 'Completed'))
        .catchError((Object e) => setState(() => _result = 'Timeout: $e'));
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
        child: Text(_result, style: const TextStyle(fontSize: 16)),
      ),
    );
  }
}
