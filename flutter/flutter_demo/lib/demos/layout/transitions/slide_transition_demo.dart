import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// SlideTransition
/// Demonstrates slide animation
class SlideTransitionDemoPage extends BasicLayoutPage {
  const SlideTransitionDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<SlideTransitionDemoPage> createState() =>
      _SlideTransitionDemoPageState();
}

class _SlideTransitionDemoPageState
    extends BasicLayoutPageState<SlideTransitionDemoPage>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<Offset> _animation;

  @override
  List<String> buildList() => const <String>[
        '1. 切换滑动',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _toggleSlide();
    }
  }

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 500),
      vsync: this,
    );
    _animation = Tween<Offset>(
      begin: const Offset(-1.0, 0.0),
      end: Offset.zero,
    ).animate(CurvedAnimation(parent: _controller, curve: Curves.easeInOut));
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _toggleSlide() {
    if (_controller.status == AnimationStatus.completed) {
      _controller.reverse();
    } else {
      _controller.forward();
    }
  }

  @override
  Widget buildPreview() {
    return Center(
      child: ClipRect(
        child: SlideTransition(
          position: _animation,
          child: Container(
            width: 200,
            height: 100,
            color: Colors.green,
            child: const Center(
              child: Text(
                'Sliding Box',
                style: TextStyle(color: Colors.white, fontSize: 20),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
