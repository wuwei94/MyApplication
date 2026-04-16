import 'package:flutter/material.dart';

/// Stack Example
/// Demonstrates the usage of Stack widget for overlaying children
class StackDemoPage extends StatelessWidget {
  const StackDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return StackDemoView(title: title);
  }
}

class StackDemoView extends StatelessWidget {
  const StackDemoView({super.key, required this.title});

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
            _buildSectionTitle('Basic Stack'),
            _buildBasicStack(),
            const SizedBox(height: 24),
            _buildSectionTitle('Stack with Alignment'),
            _buildStackAlignment(),
            const SizedBox(height: 24),
            _buildSectionTitle('Stack with Fit'),
            _buildStackFit(),
            const SizedBox(height: 24),
            _buildSectionTitle('Stack with Clip'),
            _buildStackClip(),
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

  Widget _buildBasicStack() {
    return Container(
      width: double.infinity,
      height: 150,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Stack(
        children: [
          Container(color: Colors.red, width: 150, height: 150),
          Container(color: Colors.green, width: 120, height: 120),
          Container(color: Colors.blue, width: 90, height: 90),
          Container(color: Colors.yellow, width: 60, height: 60),
        ],
      ),
    );
  }

  Widget _buildStackAlignment() {
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
        alignment: Alignment.center,
        children: [
          Container(color: Colors.green.shade100, width: 150, height: 150),
          Container(color: Colors.green.shade300, width: 100, height: 100),
          Container(color: Colors.green.shade500, width: 50, height: 50),
        ],
      ),
    );
  }

  Widget _buildStackFit() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Column(
        children: [
          const Text('StackFit.loose (default)'),
          Container(
            height: 100,
            color: Colors.orange.shade100,
            child: Stack(
              fit: StackFit.loose,
              children: [
                Container(color: Colors.orange, width: 50, height: 50),
              ],
            ),
          ),
          const SizedBox(height: 8),
          const Text('StackFit.expand'),
          Container(
            height: 100,
            color: Colors.orange.shade200,
            child: Stack(
              fit: StackFit.expand,
              children: [Container(color: Colors.orange.shade700)],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStackClip() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: Column(
        children: [
          const Text('Clip.hardEdge'),
          Container(
            height: 100,
            color: Colors.purple.shade100,
            child: Stack(
              clipBehavior: Clip.hardEdge,
              children: [
                Positioned(
                  left: 50,
                  top: 50,
                  child: Container(
                    width: 100,
                    height: 100,
                    color: Colors.purple,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
