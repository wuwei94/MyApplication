import 'package:flutter/material.dart';

/// Column Example
/// Demonstrates vertical layout with Column
class ColumnDemoPage extends StatelessWidget {
  const ColumnDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ColumnDemoView(title: title);
  }
}

class ColumnDemoView extends StatelessWidget {
  const ColumnDemoView({super.key, required this.title});

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
            _buildSectionTitle('Basic Column'),
            _buildBasicColumn(),
            const SizedBox(height: 24),
            _buildSectionTitle('MainAxisAlignment'),
            _buildMainAxisAlignment(),
            const SizedBox(height: 24),
            _buildSectionTitle('CrossAxisAlignment'),
            _buildCrossAxisAlignment(),
            const SizedBox(height: 24),
            _buildSectionTitle('Expanded in Column'),
            _buildExpandedColumn(),
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

  Widget _buildBasicColumn() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(width: 100, height: 40, color: Colors.red),
          const SizedBox(height: 8),
          Container(width: 150, height: 40, color: Colors.green),
          const SizedBox(height: 8),
          Container(width: 200, height: 40, color: Colors.blue),
        ],
      ),
    );
  }

  Widget _buildMainAxisAlignment() {
    return Container(
      height: 200,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.green.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.green.shade200),
      ),
      child: Row(
        children: [
          Expanded(
            child: _buildColumnAlignment('start', MainAxisAlignment.start),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: _buildColumnAlignment('center', MainAxisAlignment.center),
          ),
          const SizedBox(width: 8),
          Expanded(child: _buildColumnAlignment('end', MainAxisAlignment.end)),
        ],
      ),
    );
  }

  Widget _buildColumnAlignment(String label, MainAxisAlignment alignment) {
    return Column(
      children: [
        Text(label, style: const TextStyle(fontSize: 12)),
        Expanded(
          child: Container(
            color: Colors.green.shade100,
            child: Column(
              mainAxisAlignment: alignment,
              children: [
                Container(width: 30, height: 20, color: Colors.green),
                Container(width: 30, height: 20, color: Colors.green.shade400),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildCrossAxisAlignment() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Column(
        children: [
          _buildCrossColumn('start', CrossAxisAlignment.start),
          const SizedBox(height: 8),
          _buildCrossColumn('center', CrossAxisAlignment.center),
          const SizedBox(height: 8),
          _buildCrossColumn('end', CrossAxisAlignment.end),
          const SizedBox(height: 8),
          _buildCrossColumn('stretch', CrossAxisAlignment.stretch),
        ],
      ),
    );
  }

  Widget _buildCrossColumn(String label, CrossAxisAlignment alignment) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          color: Colors.orange.shade100,
          child: Column(
            crossAxisAlignment: alignment,
            children: [
              Container(width: 50, height: 20, color: Colors.orange),
              Container(width: 100, height: 20, color: Colors.orange.shade400),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildExpandedColumn() {
    return Container(
      height: 200,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: Column(
        children: [
          Container(height: 40, color: Colors.purple),
          Expanded(
            child: Container(
              margin: const EdgeInsets.symmetric(vertical: 8),
              color: Colors.purple.shade400,
              child: const Center(
                child: Text('Expanded', style: TextStyle(color: Colors.white)),
              ),
            ),
          ),
          Container(height: 40, color: Colors.purple.shade800),
        ],
      ),
    );
  }
}
