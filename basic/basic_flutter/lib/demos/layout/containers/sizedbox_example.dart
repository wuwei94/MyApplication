import 'package:flutter/material.dart';

/// SizedBox Example
/// Demonstrates various usages of SizedBox: fixed size, expand, shrink, square, etc.
class SizedBoxDemoPage extends StatelessWidget {
  const SizedBoxDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SizedBoxDemoView(title: title);
  }
}

class SizedBoxDemoView extends StatelessWidget {
  const SizedBoxDemoView({super.key, required this.title});

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
            _buildSectionTitle('Fixed Size'),
            _buildFixedSize(),
            const SizedBox(height: 24),
            _buildSectionTitle('SizedBox.expand - Fill Available Space'),
            _buildExpandSize(),
            const SizedBox(height: 24),
            _buildSectionTitle('SizedBox.shrink - Shrink to Child'),
            _buildShrinkSize(),
            const SizedBox(height: 24),
            _buildSectionTitle('SizedBox.square - Square'),
            _buildSquareSize(),
            const SizedBox(height: 24),
            _buildSectionTitle('Used as Spacing'),
            _buildAsSpacing(),
            const SizedBox(height: 24),
            _buildSectionTitle('Practical Examples'),
            _buildPracticalExample(),
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

  Widget _buildFixedSize() {
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
          Row(
            children: [
              Container(width: 50, height: 50, color: Colors.red),
              const SizedBox(width: 16),
              Container(width: 50, height: 50, color: Colors.green),
              const SizedBox(width: 16),
              Container(width: 50, height: 50, color: Colors.blue),
            ],
          ),
          const SizedBox(height: 16),
          const Text('Fixed width and height SizedBox:'),
          const SizedBox(height: 8),
          SizedBox(
            width: 150,
            height: 60,
            child: ElevatedButton(
              onPressed: () {},
              child: const Text('Fixed Button'),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildExpandSize() {
    return Container(
      width: double.infinity,
      height: 150,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.green.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.green.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('SizedBox.expand fills parent container:'),
          const SizedBox(height: 8),
          Expanded(
            child: Container(
              color: Colors.green.shade100,
              child: SizedBox.expand(
                child: Container(
                  margin: const EdgeInsets.all(8),
                  color: Colors.green,
                  child: const Center(
                    child: Text(
                      'expand',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildShrinkSize() {
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
          const Text('SizedBox.shrink shrinks to child size:'),
          const SizedBox(height: 8),
          Container(
            width: double.infinity,
            color: Colors.orange.shade100,
            child: SizedBox.shrink(
              child: Container(
                padding: const EdgeInsets.all(16),
                color: Colors.orange,
                child: const Text(
                  'shrink',
                  style: TextStyle(color: Colors.white),
                ),
              ),
            ),
          ),
          const SizedBox(height: 16),
          const Text('Empty SizedBox.shrink():'),
          Container(
            width: double.infinity,
            color: Colors.orange.shade200,
            child: const SizedBox.shrink(),
          ),
        ],
      ),
    );
  }

  Widget _buildSquareSize() {
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
          const Text('SizedBox.square creates a square:'),
          const SizedBox(height: 8),
          SizedBox.square(
            dimension: 80,
            child: Container(
              color: Colors.purple,
              child: const Center(
                child: Text('80x80', style: TextStyle(color: Colors.white)),
              ),
            ),
          ),
          const SizedBox(height: 16),
          const Text('Different size squares:'),
          Row(
            children: [
              SizedBox.square(
                dimension: 40,
                child: Container(color: Colors.purple.shade300),
              ),
              const SizedBox(width: 8),
              SizedBox.square(
                dimension: 60,
                child: Container(color: Colors.purple.shade500),
              ),
              const SizedBox(width: 8),
              SizedBox.square(
                dimension: 80,
                child: Container(color: Colors.purple.shade700),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildAsSpacing() {
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
          const Text('Horizontal spacing:'),
          Row(
            children: [
              Container(width: 40, height: 40, color: Colors.teal),
              const SizedBox(width: 8),
              Container(width: 40, height: 40, color: Colors.teal.shade300),
              const SizedBox(width: 16),
              Container(width: 40, height: 40, color: Colors.teal.shade500),
              const SizedBox(width: 24),
              Container(width: 40, height: 40, color: Colors.teal.shade700),
            ],
          ),
          const SizedBox(height: 16),
          const Text('Vertical spacing:'),
          Column(
            children: [
              Container(width: double.infinity, height: 30, color: Colors.teal),
              const SizedBox(height: 8),
              Container(
                width: double.infinity,
                height: 30,
                color: Colors.teal.shade300,
              ),
              const SizedBox(height: 16),
              Container(
                width: double.infinity,
                height: 30,
                color: Colors.teal.shade500,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildPracticalExample() {
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
          const Text('Fixed size image placeholder:'),
          const SizedBox(height: 8),
          SizedBox(
            width: 100,
            height: 100,
            child: Container(
              color: Colors.indigo.shade100,
              child: const Center(child: Icon(Icons.image, size: 40)),
            ),
          ),
          const SizedBox(height: 16),
          const Text('Divider:'),
          const SizedBox(height: 8),
          const Row(
            children: [
              Text('Left'),
              SizedBox(width: 8),
              Expanded(child: Divider()),
              SizedBox(width: 8),
              Text('Right'),
            ],
          ),
          const SizedBox(height: 16),
          const Text('Limit maximum size:'),
          const SizedBox(height: 8),
          Container(
            width: double.infinity,
            color: Colors.indigo.shade100,
            child: Center(
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 200),
                child: Container(
                  width: double.infinity,
                  height: 50,
                  color: Colors.indigo,
                  child: const Center(
                    child: Text(
                      'Max width 200',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
