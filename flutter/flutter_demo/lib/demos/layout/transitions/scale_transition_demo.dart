import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// ScaleTransition
/// Demonstrates scale animation
class ScaleTransitionDemoPage extends BasicLayoutPage {
  const ScaleTransitionDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<ScaleTransitionDemoPage> createState() =>
      _ScaleTransitionDemoPageState();
}

class _ScaleTransitionDemoPageState
    extends BasicLayoutPageState<ScaleTransitionDemoPage>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  List<String> buildList() => const <String>[
        '1. 切换缩放',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _toggleScale();
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

  void _toggleScale() {
    if (_controller.status == AnimationStatus.completed) {
      _controller.reverse();
    } else {
      _controller.forward();
    }
  }

  @override
  Widget buildPreview() {
    return Center(
      child: ScaleTransition(
        scale: _animation,
        child: Container(
          width: 150,
          height: 150,
          color: Colors.orange,
          child: const Center(
            child: Text(
              'Scaling Box',
              style: TextStyle(color: Colors.white, fontSize: 20),
            ),
          ),
        ),
      ),
    );
  }
}
