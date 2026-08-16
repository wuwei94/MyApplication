import 'package:flutter/material.dart';

/// DecoratedBox
/// Demonstrates the usage of DecoratedBox widget
class DecoratedBoxDemoPage extends StatelessWidget {
  const DecoratedBoxDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return DecoratedBoxDemoView(title: title);
  }
}

class DecoratedBoxDemoView extends StatelessWidget {
  const DecoratedBoxDemoView({super.key, required this.title});

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
            _buildSectionTitle('BoxDecoration with Border'),
            _buildBorderDecoration(),
            const SizedBox(height: 24),
            _buildSectionTitle('BoxDecoration with BorderRadius'),
            _buildBorderRadiusDecoration(),
            const SizedBox(height: 24),
            _buildSectionTitle('BoxDecoration with Shadow'),
            _buildShadowDecoration(),
            const SizedBox(height: 24),
            _buildSectionTitle('Gradient Decoration'),
            _buildGradientDecoration(),
            const SizedBox(height: 24),
            _buildSectionTitle('Shape Decoration'),
            _buildShapeDecoration(),
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

  Widget _buildBorderDecoration() {
    return DecoratedBox(
      decoration: BoxDecoration(
        border: Border.all(color: Colors.blue, width: 2),
        color: Colors.blue.shade50,
      ),
      child: const SizedBox(
        width: double.infinity,
        height: 60,
        child: Center(child: Text('Border Decoration')),
      ),
    );
  }

  Widget _buildBorderRadiusDecoration() {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.green,
        borderRadius: BorderRadius.circular(16),
      ),
      child: const SizedBox(
        width: double.infinity,
        height: 60,
        child: Center(
          child: Text('BorderRadius', style: TextStyle(color: Colors.white)),
        ),
      ),
    );
  }

  Widget _buildShadowDecoration() {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.2),
            blurRadius: 8,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: const SizedBox(
        width: double.infinity,
        height: 80,
        child: Center(child: Text('Shadow')),
      ),
    );
  }

  Widget _buildGradientDecoration() {
    return DecoratedBox(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [Colors.blue.shade400, Colors.purple.shade400],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(12),
      ),
      child: const SizedBox(
        width: double.infinity,
        height: 80,
        child: Center(
          child: Text('Linear Gradient', style: TextStyle(color: Colors.white)),
        ),
      ),
    );
  }

  Widget _buildShapeDecoration() {
    return const Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        DecoratedBox(
          decoration: BoxDecoration(
            color: Colors.orange,
            shape: BoxShape.circle,
          ),
          child: SizedBox(width: 80, height: 80),
        ),
        DecoratedBox(
          decoration: BoxDecoration(
            color: Colors.purple,
            borderRadius: BorderRadius.all(Radius.circular(20)),
          ),
          child: SizedBox(width: 80, height: 80),
        ),
      ],
    );
  }
}
