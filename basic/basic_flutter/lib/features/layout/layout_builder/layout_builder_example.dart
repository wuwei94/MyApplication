import 'package:flutter/material.dart';

/// LayoutBuilder Example
/// Demonstrates responsive layout based on parent constraints
class LayoutBuilderExample extends StatelessWidget {
  const LayoutBuilderExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilderRoute(title: title);
  }
}

class LayoutBuilderRoute extends StatelessWidget {
  const LayoutBuilderRoute({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: Column(
        children: [
          Expanded(
            child: LayoutBuilder(
              builder: (context, constraints) {
                if (constraints.maxWidth > 600) {
                  return _buildWideLayout();
                } else {
                  return _buildNarrowLayout();
                }
              },
            ),
          ),
          Container(
            padding: const EdgeInsets.all(16),
            color: Colors.grey.shade200,
            child: LayoutBuilder(
              builder: (context, constraints) {
                return Text(
                  'Width: ${constraints.maxWidth.toStringAsFixed(1)}\nHeight: ${constraints.maxHeight.toStringAsFixed(1)}',
                  textAlign: TextAlign.center,
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildWideLayout() {
    return Row(
      children: [
        Expanded(
          child: Container(
            color: Colors.blue.shade100,
            child: const Center(
              child: Text('Sidebar', style: TextStyle(fontSize: 24)),
            ),
          ),
        ),
        Expanded(
          flex: 2,
          child: Container(
            color: Colors.green.shade100,
            child: const Center(
              child: Text('Main Content', style: TextStyle(fontSize: 24)),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildNarrowLayout() {
    return Column(
      children: [
        Container(
          height: 100,
          color: Colors.blue.shade100,
          child: const Center(
            child: Text('Header', style: TextStyle(fontSize: 24)),
          ),
        ),
        Expanded(
          child: Container(
            color: Colors.green.shade100,
            child: const Center(
              child: Text('Content', style: TextStyle(fontSize: 24)),
            ),
          ),
        ),
      ],
    );
  }
}
