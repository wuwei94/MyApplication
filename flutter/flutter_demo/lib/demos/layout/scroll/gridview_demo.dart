import 'package:flutter/material.dart';

/// GridView
/// Demonstrates the usage of GridView widget
class GridViewDemoPage extends StatelessWidget {
  const GridViewDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return GridViewDemoView(title: title);
  }
}

class GridViewDemoView extends StatelessWidget {
  const GridViewDemoView({super.key, required this.title});

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
            _buildSectionTitle('GridView.count'),
            SizedBox(height: 200, child: _buildGridViewCount()),
            const SizedBox(height: 24),
            _buildSectionTitle('GridView.builder'),
            SizedBox(height: 200, child: _buildGridViewBuilder()),
            const SizedBox(height: 24),
            _buildSectionTitle('GridView.extent'),
            SizedBox(height: 200, child: _buildGridViewExtent()),
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

  Widget _buildGridViewCount() {
    return GridView.count(
      crossAxisCount: 3,
      crossAxisSpacing: 8,
      mainAxisSpacing: 8,
      children: List.generate(9, (index) {
        return Container(
          decoration: BoxDecoration(
            color: Colors.blue.shade100,
            borderRadius: BorderRadius.circular(8),
          ),
          child: Center(child: Text('Item $index')),
        );
      }),
    );
  }

  Widget _buildGridViewBuilder() {
    return GridView.builder(
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 4,
        crossAxisSpacing: 8,
        mainAxisSpacing: 8,
      ),
      itemCount: 12,
      itemBuilder: (context, index) {
        return Container(
          decoration: BoxDecoration(
            color: Colors.green.shade100,
            borderRadius: BorderRadius.circular(8),
          ),
          child: Center(child: Text('$index')),
        );
      },
    );
  }

  Widget _buildGridViewExtent() {
    return GridView.extent(
      maxCrossAxisExtent: 80,
      crossAxisSpacing: 8,
      mainAxisSpacing: 8,
      children: List.generate(12, (index) {
        return Container(
          decoration: BoxDecoration(
            color: Colors.orange.shade100,
            borderRadius: BorderRadius.circular(8),
          ),
          child: Center(child: Text('$index')),
        );
      }),
    );
  }
}
