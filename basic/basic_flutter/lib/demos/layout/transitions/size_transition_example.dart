import 'package:flutter/material.dart';

/// SizeTransition Example
/// Demonstrates size animation
class SizeTransitionDemoPage extends StatelessWidget {
  const SizeTransitionDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SizeTransitionDemoView(title: title);
  }
}

class SizeTransitionDemoView extends StatefulWidget {
  const SizeTransitionDemoView({super.key, required this.title});

  final String title;

  @override
  State<SizeTransitionDemoView> createState() => _SizeTransitionDemoViewState();
}

class _SizeTransitionDemoViewState extends State<SizeTransitionDemoView>
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
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            SizeTransition(
              sizeFactor: _animation,
              axis: Axis.vertical,
              alignment: -1,
              child: Center(
                child: Container(
                  width: 200,
                  height: 150,
                  decoration: BoxDecoration(
                    color: Colors.green,
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: const Center(
                    child: Text(
                      'Size Transition',
                      style: TextStyle(color: Colors.white, fontSize: 20),
                    ),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 32),
            ElevatedButton(
              onPressed: _toggleSize,
              child: const Text('Toggle Size'),
            ),
          ],
        ),
      ),
    );
  }
}
