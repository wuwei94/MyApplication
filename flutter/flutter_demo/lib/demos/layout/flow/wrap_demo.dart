import 'package:flutter/material.dart';

/// Wrap
/// Demonstrates the usage of Wrap widget for flow layout
class WrapDemoPage extends StatelessWidget {
  const WrapDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return WrapDemoView(title: title);
  }
}

class WrapDemoView extends StatelessWidget {
  const WrapDemoView({super.key, required this.title});

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
            _buildSectionTitle('Basic Wrap'),
            _buildBasicWrap(),
            const SizedBox(height: 24),
            _buildSectionTitle('Wrap with Spacing'),
            _buildWrapSpacing(),
            const SizedBox(height: 24),
            _buildSectionTitle('Wrap Alignment'),
            _buildWrapAlignment(),
            const SizedBox(height: 24),
            _buildSectionTitle('Vertical Wrap'),
            _buildVerticalWrap(),
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

  Widget _buildBasicWrap() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Wrap(
        children: [
          _buildChip('Flutter'),
          _buildChip('Dart'),
          _buildChip('Android'),
          _buildChip('iOS'),
          _buildChip('Web'),
          _buildChip('Desktop'),
          _buildChip('Mobile'),
          _buildChip('Cross Platform'),
        ],
      ),
    );
  }

  Widget _buildWrapSpacing() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.green.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.green.shade200),
      ),
      child: Wrap(
        spacing: 16,
        runSpacing: 12,
        children: [
          _buildChip('Spacing'),
          _buildChip('Between'),
          _buildChip('Items'),
          _buildChip('Is'),
          _buildChip('16px'),
          _buildChip('Run'),
          _buildChip('Spacing'),
          _buildChip('12px'),
        ],
      ),
    );
  }

  Widget _buildWrapAlignment() {
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
          _buildAlignmentRow('start', WrapAlignment.start),
          const SizedBox(height: 8),
          _buildAlignmentRow('center', WrapAlignment.center),
          const SizedBox(height: 8),
          _buildAlignmentRow('end', WrapAlignment.end),
          const SizedBox(height: 8),
          _buildAlignmentRow('spaceBetween', WrapAlignment.spaceBetween),
        ],
      ),
    );
  }

  Widget _buildAlignmentRow(String label, WrapAlignment alignment) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          color: Colors.orange.shade100,
          child: Wrap(
            alignment: alignment,
            children: [
              _buildSmallChip('A'),
              _buildSmallChip('B'),
              _buildSmallChip('C'),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildVerticalWrap() {
    return Container(
      width: double.infinity,
      height: 150,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: Wrap(
        direction: Axis.vertical,
        spacing: 8,
        children: [
          _buildChip('1'),
          _buildChip('2'),
          _buildChip('3'),
          _buildChip('4'),
          _buildChip('5'),
          _buildChip('6'),
          _buildChip('7'),
          _buildChip('8'),
        ],
      ),
    );
  }

  Widget _buildChip(String label) {
    return Container(
      margin: const EdgeInsets.all(4),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.blue.shade100,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.blue.shade300),
      ),
      child: Text(label, style: TextStyle(color: Colors.blue.shade800)),
    );
  }

  Widget _buildSmallChip(String label) {
    return Container(
      margin: const EdgeInsets.all(2),
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.orange.shade200,
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(label, style: const TextStyle(fontSize: 12)),
    );
  }
}
