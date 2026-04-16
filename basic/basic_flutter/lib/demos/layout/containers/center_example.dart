import 'package:flutter/material.dart';

/// Center Example
/// Demonstrates various usages of Center: center alignment, combining with Container, etc.
class CenterDemoPage extends StatelessWidget {
  const CenterDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return CenterDemoView(title: title);
  }
}

class CenterDemoView extends StatelessWidget {
  const CenterDemoView({super.key, required this.title});

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
            _buildSectionTitle('Basic Center'),
            _buildBasicCenter(),
            const SizedBox(height: 24),
            _buildSectionTitle('Center with Size'),
            _buildCenterWithSize(),
            const SizedBox(height: 24),
            _buildSectionTitle('Multiple Children Centered'),
            _buildMultipleChildren(),
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

  Widget _buildBasicCenter() {
    return Container(
      width: double.infinity,
      height: 150,
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Center(
        child: Container(
          width: 80,
          height: 80,
          decoration: BoxDecoration(
            color: Colors.blue,
            borderRadius: BorderRadius.circular(8),
          ),
          child: const Icon(
            Icons.center_focus_strong,
            color: Colors.white,
            size: 40,
          ),
        ),
      ),
    );
  }

  Widget _buildCenterWithSize() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.green.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.green.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Center in fixed size container:'),
          const SizedBox(height: 8),
          Container(
            width: 200,
            height: 100,
            color: Colors.green.shade100,
            child: Center(
              child: Container(
                width: 60,
                height: 60,
                color: Colors.green,
                child: const Center(
                  child: Text('Center', style: TextStyle(color: Colors.white)),
                ),
              ),
            ),
          ),
          const SizedBox(height: 16),
          const Text('Center in adaptive container:'),
          const SizedBox(height: 8),
          Container(
            width: double.infinity,
            height: 100,
            color: Colors.green.shade200,
            child: Center(
              child: ElevatedButton(
                onPressed: () {},
                child: const Text('Centered Button'),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMultipleChildren() {
    return Container(
      width: double.infinity,
      height: 200,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.check_circle, color: Colors.green, size: 48),
            const SizedBox(height: 8),
            const Text(
              'Success',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 4),
            const Text('Multiple children centered'),
            const SizedBox(height: 12),
            ElevatedButton(onPressed: () {}, child: const Text('OK')),
          ],
        ),
      ),
    );
  }

  Widget _buildPracticalExample() {
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
          const Text('Empty State Page:'),
          const SizedBox(height: 8),
          Container(
            height: 200,
            color: Colors.purple.shade100,
            child: Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(Icons.inbox, size: 64, color: Colors.purple.shade300),
                  const SizedBox(height: 16),
                  Text(
                    'No Data',
                    style: TextStyle(
                      fontSize: 16,
                      color: Colors.purple.shade700,
                    ),
                  ),
                  const SizedBox(height: 8),
                  TextButton(onPressed: () {}, child: const Text('Refresh')),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          const Text('Loading State:'),
          const SizedBox(height: 8),
          Container(
            height: 150,
            color: Colors.purple.shade200,
            child: const Center(child: CircularProgressIndicator()),
          ),
        ],
      ),
    );
  }
}
