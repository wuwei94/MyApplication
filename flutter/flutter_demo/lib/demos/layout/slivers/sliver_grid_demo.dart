import 'package:flutter/material.dart';

/// SliverGrid
/// Demonstrates the usage of SliverGrid widget
class SliverGridDemoPage extends StatelessWidget {
  const SliverGridDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SliverGridDemoView(title: title);
  }
}

class SliverGridDemoView extends StatelessWidget {
  const SliverGridDemoView({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: CustomScrollView(
        slivers: [
          SliverAppBar(
            expandedHeight: 150,
            floating: true,
            flexibleSpace: FlexibleSpaceBar(
              title: Text(title),
              background: Container(
                color: Colors.green,
                child: const Center(
                  child: Icon(Icons.grid_on, size: 60, color: Colors.white54),
                ),
              ),
            ),
          ),
          SliverGrid(
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 3,
              crossAxisSpacing: 8,
              mainAxisSpacing: 8,
            ),
            delegate: SliverChildBuilderDelegate((context, index) {
              return Container(
                margin: const EdgeInsets.all(4),
                decoration: BoxDecoration(
                  color: Colors.green.shade100,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Center(child: Text('Grid $index')),
              );
            }, childCount: 30),
          ),
        ],
      ),
    );
  }
}
