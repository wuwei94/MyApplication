import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// SizeTransition
/// Demonstrates size animation
class SizeTransitionDemoPage extends BasicLayoutPage {
  const SizeTransitionDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<SizeTransitionDemoPage> createState() =>
      _SizeTransitionDemoPageState();
}

class _SizeTransitionDemoPageState
    extends BasicLayoutPageState<SizeTransitionDemoPage>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  List<String> buildList() => const <String>[
        '1. 切换尺寸展开',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _toggleSize();
    }
  }

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 500),
      vsync: this,
    );
    _animation = CurvedAnimation(parent: _controller, curve: Curves.easeInOut);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _toggleSize() {
    if (_controller.status == AnimationStatus.completed) {
      _controller.reverse();
    } else {
      _controller.forward();
    }
  }

  @override
  Widget buildPreview() {
    return Center(
      child: SizeTransition(
        sizeFactor: _animation,
        axis: Axis.vertical,
        child: Container(
          width: 200,
          height: 150,
          color: Colors.teal,
          child: const Center(
            child: Text(
              'Sizing Box',
              style: TextStyle(color: Colors.white, fontSize: 20),
            ),
          ),
        ),
      ),
    );
  }
}
