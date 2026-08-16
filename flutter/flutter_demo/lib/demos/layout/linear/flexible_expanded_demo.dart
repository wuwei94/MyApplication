import 'package:flutter/material.dart';

/// Flexible & Expanded
/// Demonstrates the usage of Flexible and Expanded widgets
class FlexibleExpandedDemoPage extends StatelessWidget {
  const FlexibleExpandedDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return FlexibleExpandedDemoView(title: title);
  }
}

class FlexibleExpandedDemoView extends StatelessWidget {
  const FlexibleExpandedDemoView({super.key, required this.title});

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
            _buildSectionTitle('Expanded - Fill Remaining Space'),
            _buildExpandedDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('Multiple Expanded'),
            _buildMultipleExpanded(),
            const SizedBox(height: 24),
            _buildSectionTitle('Flexible with Flex'),
            _buildFlexibleFlex(),
            const SizedBox(height: 24),
            _buildSectionTitle('Flexible.loose vs Flexible.tight'),
            _buildFlexibleFit(),
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

  Widget _buildExpandedDemo() {
    return Container(
      height: 80,
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Row(
        children: [
          Container(width: 60, color: Colors.red),
          Expanded(
            child: Container(
              margin: const EdgeInsets.symmetric(horizontal: 8),
              color: Colors.green,
              child: const Center(
                child: Text('Expanded', style: TextStyle(color: Colors.white)),
              ),
            ),
          ),
          Container(width: 60, color: Colors.blue),
        ],
      ),
    );
  }

  Widget _buildMultipleExpanded() {
    return Container(
      height: 80,
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: Colors.green.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.green.shade200),
      ),
      child: Row(
        children: [
          Expanded(
            child: Container(
              margin: const EdgeInsets.only(right: 4),
              color: Colors.green,
              child: const Center(
                child: Text('1/3', style: TextStyle(color: Colors.white)),
              ),
            ),
          ),
          Expanded(
            child: Container(
              margin: const EdgeInsets.symmetric(horizontal: 4),
              color: Colors.green.shade600,
              child: const Center(
                child: Text('1/3', style: TextStyle(color: Colors.white)),
              ),
            ),
          ),
          Expanded(
            child: Container(
              margin: const EdgeInsets.only(left: 4),
              color: Colors.green.shade800,
              child: const Center(
                child: Text('1/3', style: TextStyle(color: Colors.white)),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFlexibleFlex() {
    return Container(
      height: 80,
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Row(
        children: [
          Flexible(
            flex: 1,
            child: Container(
              margin: const EdgeInsets.only(right: 4),
              color: Colors.orange,
              child: const Center(
                child: Text('1', style: TextStyle(color: Colors.white)),
              ),
            ),
          ),
          Flexible(
            flex: 2,
            child: Container(
              margin: const EdgeInsets.symmetric(horizontal: 4),
              color: Colors.orange.shade600,
              child: const Center(
                child: Text('2', style: TextStyle(color: Colors.white)),
              ),
            ),
          ),
          Flexible(
            flex: 3,
            child: Container(
              margin: const EdgeInsets.only(left: 4),
              color: Colors.orange.shade800,
              child: const Center(
                child: Text('3', style: TextStyle(color: Colors.white)),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFlexibleFit() {
    return Container(
      height: 100,
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: Row(
        children: [
          Flexible(
            child: Container(
              width: 50,
              color: Colors.purple,
              child: const Center(
                child: Text(
                  'loose',
                  style: TextStyle(color: Colors.white, fontSize: 10),
                ),
              ),
            ),
          ),
          const SizedBox(width: 8),
          Flexible(
            fit: FlexFit.tight,
            child: Container(
              width: 50,
              color: Colors.purple.shade700,
              child: const Center(
                child: Text(
                  'tight',
                  style: TextStyle(color: Colors.white, fontSize: 10),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
