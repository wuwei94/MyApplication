import 'package:flutter/material.dart';

/// RotationTransition
/// Demonstrates rotation animation
class RotationTransitionDemoPage extends StatelessWidget {
  const RotationTransitionDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return RotationTransitionDemoView(title: title);
  }
}

class RotationTransitionDemoView extends StatefulWidget {
  const RotationTransitionDemoView({super.key, required this.title});

  final String title;

  @override
  State<RotationTransitionDemoView> createState() =>
      _RotationTransitionDemoViewState();
}

class _RotationTransitionDemoViewState extends State<RotationTransitionDemoView>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(seconds: 2),
      vsync: this,
    );
    _animation = Tween<double>(
      begin: 0,
      end: 1,
    ).animate(CurvedAnimation(parent: _controller, curve: Curves.linear));
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _toggleRotation() {
    if (_controller.isAnimating) {
      _controller.stop();
    } else {
      _controller.repeat();
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
            RotationTransition(
              turns: _animation,
              child: Container(
                width: 150,
                height: 150,
                decoration: BoxDecoration(
                  color: Colors.orange,
                  borderRadius: BorderRadius.circular(16),
                ),
                child: const Icon(Icons.refresh, size: 80, color: Colors.white),
              ),
            ),
            const SizedBox(height: 32),
            ElevatedButton(
              onPressed: _toggleRotation,
              child: const Text('Toggle Rotation'),
            ),
          ],
        ),
      ),
    );
  }
}
