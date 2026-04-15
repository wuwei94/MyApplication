import 'package:flutter/material.dart';

/// FadeTransition Example
/// Demonstrates fade animation
class FadeTransitionExample extends StatelessWidget {
  const FadeTransitionExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return FadeTransitionRoute(title: title);
  }
}

class FadeTransitionRoute extends StatefulWidget {
  const FadeTransitionRoute({super.key, required this.title});

  final String title;

  @override
  State<FadeTransitionRoute> createState() => _FadeTransitionRouteState();
}

class _FadeTransitionRouteState extends State<FadeTransitionRoute>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

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
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            FadeTransition(
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
            const SizedBox(height: 32),
            ElevatedButton(
              onPressed: _toggleFade,
              child: const Text('Toggle Fade'),
            ),
          ],
        ),
      ),
    );
  }
}
