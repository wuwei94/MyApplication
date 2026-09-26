import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Stream
/// Demonstrates Stream usage patterns
class StreamDemoPage extends BasicLayoutPage {
  const StreamDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<StreamDemoPage> createState() => _StreamDemoPageState();
}

class _StreamDemoPageState extends BasicLayoutPageState<StreamDemoPage> {
  StreamSubscription<int>? _subscription;
  StreamController<int>? _controller;
  final List<int> _values = <int>[];
  bool _isPaused = false;

  @override
  List<String> buildList() => const <String>[
        '1. 启动计时流',
        '2. 暂停/恢复订阅',
        '3. 取消订阅并清空',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _startStream();
      case 1:
        _pauseResume();
      case 2:
        _cancel();
    }
  }

  void _startStream() {
    _controller?.close();
    _controller = StreamController<int>();
    _values.clear();

    int counter = 0;
    Timer.periodic(const Duration(seconds: 1), (Timer timer) {
      final StreamController<int>? controller = _controller;
      if (controller == null || controller.isClosed) {
        timer.cancel();
        return;
      }
      controller.add(counter++);
      if (counter >= 10) {
        timer.cancel();
        controller.close();
      }
    });

    final StreamController<int>? controller = _controller;
    if (controller != null) {
      _subscription = controller.stream.listen(
        (int data) {
          setState(() => _values.add(data));
        },
        onDone: () {
          setState(() => _values.add(-1)); // -1 marks done
        },
      );
    }
    setState(() {});
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
  Widget buildPreview() {
    return Padding(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.green.shade50,
          borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
        ),
        child: _values.isEmpty
            ? const Center(child: Text('Tap start to begin'))
            : Wrap(
                spacing: 8,
                runSpacing: 8,
                children: _values.map((int v) {
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
    );
  }
}
