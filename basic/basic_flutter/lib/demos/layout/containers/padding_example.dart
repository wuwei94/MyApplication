import 'package:flutter/material.dart';

/// Padding Example
/// Demonstrates various usages of Padding: different EdgeInsets constructors
class PaddingDemoPage extends StatelessWidget {
  const PaddingDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return PaddingDemoView(title: title);
  }
}

class PaddingDemoView extends StatelessWidget {
  const PaddingDemoView({super.key, required this.title});

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
            _buildSectionTitle('EdgeInsets.all - Uniform Padding'),
            _buildAllPadding(),
            const SizedBox(height: 24),
            _buildSectionTitle('EdgeInsets.symmetric - Symmetric Padding'),
            _buildSymmetricPadding(),
            const SizedBox(height: 24),
            _buildSectionTitle('EdgeInsets.only - Single Side Padding'),
            _buildOnlyPadding(),
            const SizedBox(height: 24),
            _buildSectionTitle('EdgeInsets.fromLTRB - Individual Settings'),
            _buildLTRBPadding(),
            const SizedBox(height: 24),
            _buildSectionTitle('EdgeInsets.zero - Zero Padding'),
            _buildZeroPadding(),
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

  Widget _buildAllPadding() {
    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: double.infinity,
            color: Colors.blue.shade100,
            child: const Padding(
              padding: EdgeInsets.all(8),
              child: Text('EdgeInsets.all(8)'),
            ),
          ),
          Container(
            width: double.infinity,
            color: Colors.blue.shade200,
            child: const Padding(
              padding: EdgeInsets.all(16),
              child: Text('EdgeInsets.all(16)'),
            ),
          ),
          Container(
            width: double.infinity,
            color: Colors.blue.shade300,
            child: const Padding(
              padding: EdgeInsets.all(24),
              child: Text(
                'EdgeInsets.all(24)',
                style: TextStyle(color: Colors.white),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSymmetricPadding() {
    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        color: Colors.green.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.green.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: double.infinity,
            color: Colors.green.shade100,
            child: const Padding(
              padding: EdgeInsets.symmetric(horizontal: 32),
              child: Text('horizontal: 32'),
            ),
          ),
          const SizedBox(height: 8),
          Container(
            width: double.infinity,
            color: Colors.green.shade200,
            child: const Padding(
              padding: EdgeInsets.symmetric(vertical: 16),
              child: Text('vertical: 16'),
            ),
          ),
          const SizedBox(height: 8),
          Container(
            width: double.infinity,
            color: Colors.green.shade300,
            child: const Padding(
              padding: EdgeInsets.symmetric(horizontal: 24, vertical: 12),
              child: Text(
                'horizontal: 24, vertical: 12',
                style: TextStyle(color: Colors.white),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildOnlyPadding() {
    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: double.infinity,
            color: Colors.orange.shade100,
            child: const Padding(
              padding: EdgeInsets.only(left: 32),
              child: Text('only left: 32'),
            ),
          ),
          const SizedBox(height: 8),
          Container(
            width: double.infinity,
            color: Colors.orange.shade200,
            child: const Padding(
              padding: EdgeInsets.only(top: 16, bottom: 8),
              child: Text('top: 16, bottom: 8'),
            ),
          ),
          const SizedBox(height: 8),
          Container(
            width: double.infinity,
            color: Colors.orange.shade300,
            child: const Padding(
              padding: EdgeInsets.only(left: 16, top: 8, right: 32, bottom: 24),
              child: Text(
                'left: 16, top: 8, right: 32, bottom: 24',
                style: TextStyle(color: Colors.white),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLTRBPadding() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: Container(
        color: Colors.purple.shade100,
        child: const Padding(
          padding: EdgeInsets.fromLTRB(40, 16, 20, 32),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('fromLTRB(40, 16, 20, 32)'),
              SizedBox(height: 4),
              Text('Left: 40', style: TextStyle(fontSize: 12)),
              Text('Top: 16', style: TextStyle(fontSize: 12)),
              Text('Right: 20', style: TextStyle(fontSize: 12)),
              Text('Bottom: 32', style: TextStyle(fontSize: 12)),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildZeroPadding() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.red.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.red.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Using EdgeInsets.zero to remove padding:'),
          const SizedBox(height: 8),
          Container(
            color: Colors.red.shade100,
            child: const ListTile(
              leading: Icon(Icons.star),
              title: Text('Default padding'),
              subtitle: Text('ListTile has default padding'),
            ),
          ),
          const SizedBox(height: 8),
          Container(
            color: Colors.red.shade200,
            child: const Padding(
              padding: EdgeInsets.zero,
              child: ListTile(
                leading: Icon(Icons.star),
                title: Text('EdgeInsets.zero'),
                subtitle: Text('Padding removed'),
              ),
            ),
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
        color: Colors.teal.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.teal.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Card Layout Example:'),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Card Title',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  const Text('Card content with Padding.all(16)'),
                  const SizedBox(height: 12),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: [
                      TextButton(onPressed: () {}, child: const Text('Cancel')),
                      const SizedBox(width: 8),
                      ElevatedButton(
                        onPressed: () {},
                        child: const Text('Confirm'),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          const Text('List Item Example:'),
          const SizedBox(height: 8),
          Container(
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: Colors.teal.shade200),
            ),
            child: const Column(
              children: [
                Padding(
                  padding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  child: Row(
                    children: [
                      Icon(Icons.person),
                      SizedBox(width: 12),
                      Expanded(child: Text('User Profile')),
                      Icon(Icons.chevron_right),
                    ],
                  ),
                ),
                Divider(height: 1),
                Padding(
                  padding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  child: Row(
                    children: [
                      Icon(Icons.settings),
                      SizedBox(width: 12),
                      Expanded(child: Text('Settings')),
                      Icon(Icons.chevron_right),
                    ],
                  ),
                ),
                Divider(height: 1),
                Padding(
                  padding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  child: Row(
                    children: [
                      Icon(Icons.help),
                      SizedBox(width: 12),
                      Expanded(child: Text('Help')),
                      Icon(Icons.chevron_right),
                    ],
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
