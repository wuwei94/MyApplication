import 'dart:async';

import 'package:flutter/material.dart';

/// Stream Example
/// Demonstrates Stream usage patterns
class StreamDemoPage extends StatelessWidget {
  const StreamDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return StreamDemoView(title: title);
  }
}

class StreamDemoView extends StatefulWidget {
  const StreamDemoView({super.key, required this.title});

  final String title;

  @override
  State<StreamDemoView> createState() => _StreamDemoViewState();
}

class _StreamDemoViewState extends State<StreamDemoView> {
  StreamSubscription<int>? _subscription;
  StreamController<int>? _controller;
  final List<int> _values = [];
  bool _isPaused = false;

  void _startStream() {
    _controller?.close();
    _controller = StreamController<int>();
    _values.clear();

    int counter = 0;
    Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_controller?.isClosed ?? true) {
        timer.cancel();
        return;
      }
      _controller?.add(counter++);
      if (counter >= 10) {
        timer.cancel();
        _controller?.close();
      }
    });

    _subscription = _controller?.stream.listen(
      (data) {
        setState(() => _values.add(data));
      },
      onDone: () {
        setState(() => _values.add(-1)); // -1 marks done
      },
    );
  }

  void _pauseResume() {
    if (_isPaused) {
      _subscription?.resume();
    } else {
      _subscription?.pause();
    }
    setState(() => _isPaused = !_isPaused);
  }

  void _cancel() {
    _subscription?.cancel();
    _controller?.close();
    setState(() {
      _values.clear();
      _isPaused = false;
    });
  }

  @override
  void dispose() {
    _subscription?.cancel();
    _controller?.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Row(
              children: [
                ElevatedButton(
                  onPressed: _startStream,
                  child: const Text('Start Stream'),
                ),
                const SizedBox(width: 8),
                ElevatedButton(
                  onPressed: _pauseResume,
                  child: Text(_isPaused ? 'Resume' : 'Pause'),
                ),
                const SizedBox(width: 8),
                ElevatedButton(
                  onPressed: _cancel,
                  style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                  child: const Text('Cancel'),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Expanded(
              child: Container(
                width: double.infinity,
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.green.shade50,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: _values.isEmpty
                    ? const Center(child: Text('Press Start to begin'))
                    : Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: _values.map((v) {
                          if (v == -1) {
                            return Chip(
                              label: const Text('DONE'),
                              backgroundColor: Colors.green.shade200,
                            );
                          }
                          return Chip(label: Text('$v'));
                        }).toList(),
                      ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
