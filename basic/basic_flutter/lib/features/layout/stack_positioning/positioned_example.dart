import 'package:flutter/material.dart';

/// Positioned Example
/// Demonstrates the usage of Positioned widget for absolute positioning
class PositionedExample extends StatelessWidget {
  const PositionedExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return PositionedRoute(title: title);
  }
}

class PositionedRoute extends StatelessWidget {
  const PositionedRoute({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('Positioned - Absolute Positioning'),
            _buildPositionedDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('Positioned.fill'),
            _buildPositionedFill(),
            const SizedBox(height: 24),
            _buildSectionTitle('Positioned.directional'),
            _buildPositionedDirectional(),
            const SizedBox(height: 24),
            _buildSectionTitle('Align in Stack'),
            _buildAlignInStack(),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(
        text,
        style: const TextStyle(
          fontSize: 18,
          fontWeight: FontWeight.bold,
          color: Colors.blue,
        ),
      ),
    );
  }

  Widget _buildPositionedDemo() {
    return Container(
      width: double.infinity,
      height: 200,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Stack(
        children: [
          Container(
            color: Colors.blue.shade100,
            width: double.infinity,
            height: double.infinity,
          ),
          Positioned(
            left: 0,
            top: 0,
            child: _buildPositionedItem('Top Left', Colors.red),
          ),
          Positioned(
            right: 0,
            top: 0,
            child: _buildPositionedItem('Top Right', Colors.orange),
          ),
          Positioned(
            left: 0,
            bottom: 0,
            child: _buildPositionedItem('Bottom Left', Colors.green),
          ),
          Positioned(
            right: 0,
            bottom: 0,
            child: _buildPositionedItem('Bottom Right', Colors.blue),
          ),
          Positioned(
            left: 60,
            top: 40,
            child: _buildPositionedItem('Custom', Colors.purple),
          ),
        ],
      ),
    );
  }

  Widget _buildPositionedItem(String text, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        text,
        style: const TextStyle(color: Colors.white, fontSize: 12),
      ),
    );
  }

  Widget _buildPositionedFill() {
    return Container(
      width: double.infinity,
      height: 150,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.green.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.green.shade200),
      ),
      child: Stack(
        children: [
          Container(
            color: Colors.green.shade100,
            width: double.infinity,
            height: double.infinity,
          ),
          Positioned.fill(
            left: 20,
            top: 10,
            right: 20,
            bottom: 10,
            child: Container(
              color: Colors.green,
              child: const Center(
                child: Text(
                  'Positioned.fill',
                  style: TextStyle(color: Colors.white),
                ),
              ),
            ),
          ),
          const Positioned(
            top: 0,
            right: 0,
            child: Icon(Icons.star, color: Colors.orange),
          ),
        ],
      ),
    );
  }

  Widget _buildPositionedDirectional() {
    return Container(
      width: double.infinity,
      height: 120,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Stack(
        children: [
          Container(
            color: Colors.orange.shade100,
            width: double.infinity,
            height: double.infinity,
          ),
          Positioned.directional(
            textDirection: TextDirection.ltr,
            start: 10,
            top: 10,
            child: _buildPositionedItem('Start', Colors.orange),
          ),
          Positioned.directional(
            textDirection: TextDirection.ltr,
            end: 10,
            top: 10,
            child: _buildPositionedItem('End', Colors.orange.shade700),
          ),
        ],
      ),
    );
  }

  Widget _buildAlignInStack() {
    return Container(
      width: double.infinity,
      height: 200,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: Stack(
        children: [
          Container(
            color: Colors.purple.shade100,
            width: double.infinity,
            height: double.infinity,
          ),
          const Align(alignment: Alignment.topLeft, child: Text('Top Left')),
          const Align(
            alignment: Alignment.topCenter,
            child: Text('Top Center'),
          ),
          const Align(alignment: Alignment.topRight, child: Text('Top Right')),
          const Align(
            alignment: Alignment.centerLeft,
            child: Text('Center Left'),
          ),
          const Align(alignment: Alignment.center, child: Text('Center')),
          const Align(
            alignment: Alignment.centerRight,
            child: Text('Center Right'),
          ),
          const Align(
            alignment: Alignment.bottomLeft,
            child: Text('Bottom Left'),
          ),
          const Align(
            alignment: Alignment.bottomCenter,
            child: Text('Bottom Center'),
          ),
          const Align(
            alignment: Alignment.bottomRight,
            child: Text('Bottom Right'),
          ),
        ],
      ),
    );
  }
}
