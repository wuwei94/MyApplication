import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// RotationTransition
/// Demonstrates rotation animation
class RotationTransitionDemoPage extends BasicLayoutPage {
  const RotationTransitionDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<RotationTransitionDemoPage> createState() =>
      _RotationTransitionDemoPageState();
}

class _RotationTransitionDemoPageState
    extends BasicLayoutPageState<RotationTransitionDemoPage>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  List<String> buildList() => const <String>[
        '1. 切换旋转',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _toggleRotation();
    }
  }

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(seconds: 1),
      vsync: this,
    );
    _animation = CurvedAnimation(parent: _controller, curve: Curves.easeInOut);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _toggleRotation() {
    if (_controller.status == AnimationStatus.completed) {
      _controller.reverse();
    } else {
      _controller.forward();
    }
  }

  @override
  Widget buildPreview() {
    return Center(
      child: RotationTransition(
        turns: _animation,
        child: Container(
          width: 150,
          height: 150,
          color: Colors.purple,
          child: const Center(
            child: Text(
              'Rotating Box',
              style: TextStyle(color: Colors.white, fontSize: 20),
            ),
          ),
        ),
      ),
    );
  }
}
