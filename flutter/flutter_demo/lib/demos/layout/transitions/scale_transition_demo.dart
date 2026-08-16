import 'package:flutter/material.dart';

/// ScaleTransition
/// Demonstrates scale animation
class ScaleTransitionDemoPage extends StatelessWidget {
  const ScaleTransitionDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ScaleTransitionDemoView(title: title);
  }
}

class ScaleTransitionDemoView extends StatefulWidget {
  const ScaleTransitionDemoView({super.key, required this.title});

  final String title;

  @override
  State<ScaleTransitionDemoView> createState() => _ScaleTransitionDemoViewState();
}

class _ScaleTransitionDemoViewState extends State<ScaleTransitionDemoView>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 500),
      vsync: this,
    );
    _animation = CurvedAnimation(
      parent: _controller,
      curve: Curves.easeInOutBack,
    );
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
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ScaleTransition(
              scale: _animation,
              child: Container(
                width: 150,
                height: 150,
                decoration: BoxDecoration(
                  color: Colors.blue,
                  borderRadius: BorderRadius.circular(16),
                ),
                child: const Icon(Icons.zoom_in, size: 80, color: Colors.white),
              ),
            ),
            const SizedBox(height: 32),
            ElevatedButton(
              onPressed: _toggleScale,
              child: const Text('Toggle Scale'),
            ),
          ],
        ),
      ),
    );
  }
}
