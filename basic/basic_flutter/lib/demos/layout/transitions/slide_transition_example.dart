import 'package:flutter/material.dart';

/// SlideTransition Example
/// Demonstrates slide animation
class SlideTransitionDemoPage extends StatelessWidget {
  const SlideTransitionDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SlideTransitionDemoView(title: title);
  }
}

class SlideTransitionDemoView extends StatefulWidget {
  const SlideTransitionDemoView({super.key, required this.title});

  final String title;

  @override
  State<SlideTransitionDemoView> createState() => _SlideTransitionDemoViewState();
}

class _SlideTransitionDemoViewState extends State<SlideTransitionDemoView>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<Offset> _animation;

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
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ClipRect(
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
            const SizedBox(height: 32),
            ElevatedButton(
              onPressed: _toggleSlide,
              child: const Text('Toggle Slide'),
            ),
          ],
        ),
      ),
    );
  }
}
