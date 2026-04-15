import 'package:flutter/material.dart';

/// ScaleTransition Example
/// Demonstrates scale animation
class ScaleTransitionExample extends StatelessWidget {
  const ScaleTransitionExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ScaleTransitionRoute(title: title);
  }
}

class ScaleTransitionRoute extends StatefulWidget {
  const ScaleTransitionRoute({super.key, required this.title});

  final String title;

  @override
  State<ScaleTransitionRoute> createState() => _ScaleTransitionRouteState();
}

class _ScaleTransitionRouteState extends State<ScaleTransitionRoute>
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
