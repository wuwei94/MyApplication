import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// FadeTransition
/// Demonstrates fade animation
class FadeTransitionDemoPage extends BasicLayoutPage {
  const FadeTransitionDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<FadeTransitionDemoPage> createState() =>
      _FadeTransitionDemoPageState();
}

class _FadeTransitionDemoPageState
    extends BasicLayoutPageState<FadeTransitionDemoPage>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  List<String> buildList() => const <String>[
        '1. 切换淡入淡出',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _toggleFade();
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

  void _toggleFade() {
    if (_controller.status == AnimationStatus.completed) {
      _controller.reverse();
    } else {
      _controller.forward();
    }
  }

  @override
  Widget buildPreview() {
    return Center(
      child: FadeTransition(
        opacity: _animation,
        child: Container(
          width: 200,
          height: 200,
          color: Colors.blue,
          child: const Center(
            child: Text(
              'Fading Box',
              style: TextStyle(color: Colors.white, fontSize: 24),
            ),
          ),
        ),
      ),
    );
  }
}
