import 'package:flutter/material.dart';

/// ConstrainedBox
/// Demonstrates various constraint usages of ConstrainedBox
class ConstrainedBoxDemoPage extends StatelessWidget {
  const ConstrainedBoxDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ConstrainedBoxDemoView(title: title);
  }
}

class ConstrainedBoxDemoView extends StatelessWidget {
  const ConstrainedBoxDemoView({super.key, required this.title});

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
            _buildSectionTitle('BoxConstraints - Min/Max'),
            _buildMinMaxConstraints(),
            const SizedBox(height: 24),
            _buildSectionTitle('BoxConstraints.tight - Fixed Size'),
            _buildTightConstraints(),
            const SizedBox(height: 24),
            _buildSectionTitle('BoxConstraints.expand'),
            _buildExpandConstraints(),
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

  Widget _buildMinMaxConstraints() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Column(
        children: [
          ConstrainedBox(
            constraints: const BoxConstraints(
              minWidth: 100,
              maxWidth: 200,
              minHeight: 50,
            ),
            child: Container(
              color: Colors.blue,
              child: const Text(
                'Min/Max Constraints',
                style: TextStyle(color: Colors.white),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTightConstraints() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.green.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.green.shade200),
      ),
      child: ConstrainedBox(
        constraints: BoxConstraints.tight(const Size(150, 80)),
        child: Container(
          color: Colors.green,
          child: const Center(
            child: Text('150x80', style: TextStyle(color: Colors.white)),
          ),
        ),
      ),
    );
  }

  Widget _buildExpandConstraints() {
    return Container(
      height: 100,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: ConstrainedBox(
        constraints: const BoxConstraints.expand(),
        child: Container(
          color: Colors.orange,
          child: const Center(
            child: Text('Expand', style: TextStyle(color: Colors.white)),
          ),
        ),
      ),
    );
  }
}
