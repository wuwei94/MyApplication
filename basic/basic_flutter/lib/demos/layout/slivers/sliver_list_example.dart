import 'package:flutter/material.dart';

/// SliverList Example
/// Demonstrates the usage of SliverList widget
class SliverListDemoPage extends StatelessWidget {
  const SliverListDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SliverListDemoView(title: title);
  }
}

class SliverListDemoView extends StatelessWidget {
  const SliverListDemoView({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: CustomScrollView(
        slivers: [
          SliverAppBar(
            expandedHeight: 200,
            floating: false,
            pinned: true,
            flexibleSpace: FlexibleSpaceBar(
              title: Text(title),
              background: Container(
                color: Colors.blue,
                child: const Center(
                  child: Icon(Icons.list, size: 80, color: Colors.white54),
                ),
              ),
            ),
          ),
          SliverList(
            delegate: SliverChildBuilderDelegate((context, index) {
              return ListTile(
                leading: CircleAvatar(child: Text('$index')),
                title: Text('SliverList Item $index'),
                subtitle: const Text('Part of CustomScrollView'),
              );
            }, childCount: 20),
          ),
        ],
      ),
    );
  }
}
