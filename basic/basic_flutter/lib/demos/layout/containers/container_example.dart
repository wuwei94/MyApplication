import 'package:flutter/material.dart';

/// Container Example
/// Demonstrates various usages of Container: size, decoration, transform, constraints, etc.
class ContainerDemoPage extends StatelessWidget {
  const ContainerDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ContainerDemoView(title: title);
  }
}

class ContainerDemoView extends StatelessWidget {
  const ContainerDemoView({super.key, required this.title});

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
            _buildSectionTitle('Basic Container'),
            _buildBasicContainer(),
            const SizedBox(height: 24),
            _buildSectionTitle('Decoration Effects'),
            _buildDecorationContainer(),
            const SizedBox(height: 24),
            _buildSectionTitle('Shadow Effects'),
            _buildShadowContainer(),
            const SizedBox(height: 24),
            _buildSectionTitle('Transform Effects'),
            _buildTransformContainer(),
            const SizedBox(height: 24),
            _buildSectionTitle('Constraints & Alignment'),
            _buildConstraintContainer(),
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

  Widget _buildBasicContainer() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 100,
            height: 50,
            color: Colors.blue,
            child: const Center(
              child: Text('Fixed Size', style: TextStyle(color: Colors.white)),
            ),
          ),
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.all(16),
            margin: const EdgeInsets.only(top: 8),
            color: Colors.green,
            child: const Text(
              'With padding and margin',
              style: TextStyle(color: Colors.white),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDecorationContainer() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: double.infinity,
            height: 60,
            decoration: BoxDecoration(
              color: Colors.purple,
              borderRadius: BorderRadius.circular(8),
            ),
            child: const Center(
              child: Text(
                'Rounded Rectangle',
                style: TextStyle(color: Colors.white),
              ),
            ),
          ),
          const SizedBox(height: 12),
          Container(
            width: 80,
            height: 80,
            decoration: const BoxDecoration(
              color: Colors.orange,
              shape: BoxShape.circle,
            ),
            child: const Center(
              child: Text('Circle', style: TextStyle(color: Colors.white)),
            ),
          ),
          const SizedBox(height: 12),
          Container(
            width: double.infinity,
            height: 60,
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [Colors.blue.shade400, Colors.purple.shade400],
                begin: Alignment.centerLeft,
                end: Alignment.centerRight,
              ),
              borderRadius: BorderRadius.circular(8),
            ),
            child: const Center(
              child: Text(
                'Gradient Background',
                style: TextStyle(color: Colors.white),
              ),
            ),
          ),
          const SizedBox(height: 12),
          Container(
            width: double.infinity,
            height: 60,
            decoration: BoxDecoration(
              border: Border.all(color: Colors.red, width: 2),
              borderRadius: BorderRadius.circular(8),
            ),
            child: const Center(child: Text('Border Decoration')),
          ),
        ],
      ),
    );
  }

  Widget _buildShadowContainer() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.teal.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.teal.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: double.infinity,
            height: 80,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(8),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.1),
                  blurRadius: 4,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: const Center(child: Text('Light Shadow')),
          ),
          const SizedBox(height: 16),
          Container(
            width: double.infinity,
            height: 80,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(8),
              boxShadow: [
                BoxShadow(
                  color: Colors.teal.withValues(alpha: 0.3),
                  blurRadius: 12,
                  spreadRadius: 2,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: const Center(child: Text('Colored Shadow')),
          ),
          const SizedBox(height: 16),
          Container(
            width: double.infinity,
            height: 80,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(8),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.1),
                  blurRadius: 8,
                  offset: const Offset(-4, -4),
                ),
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.1),
                  blurRadius: 8,
                  offset: const Offset(4, 4),
                ),
              ],
            ),
            child: const Center(child: Text('Multi-direction Shadow')),
          ),
        ],
      ),
    );
  }

  Widget _buildTransformContainer() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 100,
            height: 60,
            color: Colors.orange,
            transform: Matrix4.rotationZ(0.1),
            child: const Center(
              child: Text('Rotate', style: TextStyle(color: Colors.white)),
            ),
          ),
          const SizedBox(height: 24),
          Container(
            width: 100,
            height: 60,
            color: Colors.red,
            transform: Matrix4.skewX(0.2),
            child: const Center(
              child: Text('Skew', style: TextStyle(color: Colors.white)),
            ),
          ),
          const SizedBox(height: 24),
          Container(
            width: 100,
            height: 60,
            color: Colors.green,
            alignment: Alignment.center,
            foregroundDecoration: BoxDecoration(
              color: Colors.black.withValues(alpha: 0.3),
              borderRadius: BorderRadius.circular(4),
            ),
            child: const Text(
              'Foreground',
              style: TextStyle(color: Colors.white),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildConstraintContainer() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.indigo.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.indigo.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: double.infinity,
            height: 80,
            color: Colors.indigo.shade100,
            alignment: Alignment.center,
            child: Container(
              width: 50,
              height: 50,
              color: Colors.indigo,
              alignment: Alignment.center,
              child: const Text(
                'Center',
                style: TextStyle(color: Colors.white, fontSize: 12),
              ),
            ),
          ),
          const SizedBox(height: 12),
          Container(
            width: double.infinity,
            constraints: const BoxConstraints(minHeight: 60, maxHeight: 120),
            color: Colors.indigo.shade200,
            alignment: Alignment.center,
            child: const Text('Constrained Height'),
          ),
          const SizedBox(height: 12),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16),
            color: Colors.indigo.shade300,
            child: const Text(
              'Adaptive Content',
              style: TextStyle(color: Colors.white),
            ),
          ),
        ],
      ),
    );
  }
}
