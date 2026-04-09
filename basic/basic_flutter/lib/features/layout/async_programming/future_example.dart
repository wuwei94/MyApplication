import 'package:flutter/material.dart';

/// Future Example
/// Demonstrates Future usage patterns
class FutureExample extends StatelessWidget {
  const FutureExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return FutureRoute(title: title);
  }
}

class FutureRoute extends StatefulWidget {
  const FutureRoute({super.key, required this.title});

  final String title;

  @override
  State<FutureRoute> createState() => _FutureRouteState();
}

class _FutureRouteState extends State<FutureRoute> {
  String _result = 'Press a button to start';

  Future<String> _delayedOperation() async {
    await Future<void>.delayed(const Duration(seconds: 2));
    return 'Operation completed!';
  }

  void _thenCatch() {
    setState(() => _result = 'Loading...');
    _delayedOperation()
        .then((value) {
          setState(() => _result = 'Then: $value');
        })
        .catchError((Object error) {
          setState(() => _result = 'Error: $error');
        });
  }

  void _asyncAwait() async {
    setState(() => _result = 'Loading...');
    try {
      final result = await _delayedOperation();
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
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                ElevatedButton(
                  onPressed: _thenCatch,
                  child: const Text('.then/.catchError'),
                ),
                ElevatedButton(
                  onPressed: _asyncAwait,
                  child: const Text('async/await'),
                ),
                ElevatedButton(
                  onPressed: _whenComplete,
                  child: const Text('.whenComplete'),
                ),
                ElevatedButton(
                  onPressed: _timeout,
                  child: const Text('.timeout'),
                ),
              ],
            ),
            const SizedBox(height: 24),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.blue.shade50,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(_result, style: const TextStyle(fontSize: 16)),
            ),
          ],
        ),
      ),
    );
  }
}
