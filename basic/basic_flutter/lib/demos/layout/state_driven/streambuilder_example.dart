import 'dart:async';

import 'package:flutter/material.dart';

/// StreamBuilder Example
/// Demonstrates real-time data handling with StreamBuilder
class StreamBuilderExample extends StatelessWidget {
  const StreamBuilderExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return StreamBuilderRoute(title: title);
  }
}

class StreamBuilderRoute extends StatefulWidget {
  const StreamBuilderRoute({super.key, required this.title});

  final String title;

  @override
  State<StreamBuilderRoute> createState() => _StreamBuilderRouteState();
}

class _StreamBuilderRouteState extends State<StreamBuilderRoute> {
  StreamController<int>? _controller;
  Timer? _timer;
  int _counter = 0;
  bool _isRunning = false;

  void _startStream() {
    if (_isRunning) return;

    _controller = StreamController<int>.broadcast();
    _counter = 0;
    _isRunning = true;

    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      _counter++;
      _controller?.add(_counter);
      if (_counter >= 10) {
        _stopStream();
      }
    });

    setState(() {});
  }

  void _stopStream() {
    _timer?.cancel();
    _controller?.close();
    _isRunning = false;
    setState(() {});
  }

  @override
  void dispose() {
    _stopStream();
    super.dispose();
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
            _buildSectionTitle('StreamBuilder Demo'),
            Row(
              children: [
                ElevatedButton(
                  onPressed: _isRunning ? null : _startStream,
                  child: const Text('Start Stream'),
                ),
                const SizedBox(width: 8),
                ElevatedButton(
                  onPressed: _isRunning ? _stopStream : null,
                  style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                  child: const Text('Stop Stream'),
                ),
              ],
            ),
            const SizedBox(height: 24),
            _buildSectionTitle('Stream Data'),
            Expanded(
              child: Container(
                width: double.infinity,
                decoration: BoxDecoration(
                  color: Colors.green.shade50,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.green.shade200),
                ),
                child: _controller == null
                    ? const Center(child: Text('Press Start to begin stream'))
                    : StreamBuilder<int>(
                        stream: _controller!.stream,
                        initialData: 0,
                        builder: (context, snapshot) {
                          if (snapshot.hasData) {
                            return Center(
                              child: Column(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  Text(
                                    '${snapshot.data}',
                                    style: const TextStyle(
                                      fontSize: 72,
                                      fontWeight: FontWeight.bold,
                                      color: Colors.green,
                                    ),
                                  ),
                                  const SizedBox(height: 16),
                                  LinearProgressIndicator(
                                    value: (snapshot.data ?? 0) / 10,
                                    backgroundColor: Colors.green.shade100,
                                    valueColor: AlwaysStoppedAnimation<Color>(
                                      Colors.green.shade600,
                                    ),
                                  ),
                                  const SizedBox(height: 8),
                                  Text(
                                    'Progress: ${((snapshot.data ?? 0) * 10).toInt()}%',
                                  ),
                                ],
                              ),
                            );
                          } else if (snapshot.connectionState ==
                              ConnectionState.done) {
                            return const Center(
                              child: Column(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  Icon(
                                    Icons.check_circle,
                                    color: Colors.green,
                                    size: 48,
                                  ),
                                  SizedBox(height: 16),
                                  Text(
                                    'Stream Completed!',
                                    style: TextStyle(fontSize: 18),
                                  ),
                                ],
                              ),
                            );
                          }
                          return const Center(
                            child: CircularProgressIndicator(),
                          );
                        },
                      ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(
        text,
        style: const TextStyle(
          fontSize: 18,
          fontWeight: FontWeight.bold,
          color: Colors.blue,
        ),
      ),
    );
  }
}
