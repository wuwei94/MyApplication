import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// StreamBuilder
/// Demonstrates real-time data handling with StreamBuilder
class StreamBuilderDemoPage extends BasicLayoutPage {
  const StreamBuilderDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<StreamBuilderDemoPage> createState() =>
      _StreamBuilderDemoPageState();
}

class _StreamBuilderDemoPageState
    extends BasicLayoutPageState<StreamBuilderDemoPage> {
  StreamController<int>? _controller;
  Timer? _timer;
  int _counter = 0;
  bool _isRunning = false;

  @override
  List<String> buildList() => const <String>[
        '1. 启动计时流',
        '2. 停止计时流',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _startStream();
      case 1:
        _stopStream();
    }
  }

  void _startStream() {
    if (_isRunning) return;

    _controller = StreamController<int>.broadcast();
    _counter = 0;
    _isRunning = true;

    _timer = Timer.periodic(const Duration(seconds: 1), (Timer timer) {
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
  Widget buildPreview() {
    final StreamController<int>? controller = _controller;
    return Padding(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Container(
        width: double.infinity,
        decoration: BoxDecoration(
          color: Colors.green.shade50,
          borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
          border: Border.all(color: Colors.green.shade200),
        ),
        child: controller == null
            ? const Center(child: Text('Tap start to begin stream'))
            : StreamBuilder<int>(
                stream: controller.stream,
                initialData: 0,
                builder: (
                  BuildContext context,
                  AsyncSnapshot<int> snapshot,
                ) {
                  if (snapshot.hasData) {
                    final int value = snapshot.data ?? 0;
                    return Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: <Widget>[
                          Text(
                            '$value',
                            style: const TextStyle(
                              fontSize: 72,
                              fontWeight: FontWeight.bold,
                              color: Colors.green,
                            ),
                          ),
                          const SizedBox(height: 16),
                          LinearProgressIndicator(
                            value: value / 10,
                            backgroundColor: Colors.green.shade100,
                            valueColor: AlwaysStoppedAnimation<Color>(
                              Colors.green.shade600,
                            ),
                          ),
                          const SizedBox(height: 8),
                          Text('Progress: ${value * 10}%'),
                        ],
                      ),
                    );
                  } else if (snapshot.connectionState ==
                      ConnectionState.done) {
                    return const Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: <Widget>[
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
                  return const Center(child: CircularProgressIndicator());
                },
              ),
      ),
    );
  }
}
