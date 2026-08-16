import 'package:flutter/material.dart';

/// CustomScrollView
/// Demonstrates custom scrollable with slivers
class CustomScrollViewDemoPage extends StatelessWidget {
  const CustomScrollViewDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return CustomScrollViewDemoView(title: title);
  }
}

class CustomScrollViewDemoView extends StatelessWidget {
  const CustomScrollViewDemoView({super.key, required this.title});

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
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [Colors.blue, Colors.purple],
                  ),
                ),
              ),
            ),
          ),
          SliverToBoxAdapter(
            child: Container(
              padding: const EdgeInsets.all(16),
              child: const Text(
                'CustomScrollView allows you to combine multiple sliver widgets',
                style: TextStyle(fontSize: 16),
              ),
            ),
          ),
          SliverGrid(
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 3,
              crossAxisSpacing: 8,
              mainAxisSpacing: 8,
            ),
            delegate: SliverChildBuilderDelegate(
              (context, index) => Container(
                color: Colors.blue.shade100,
                child: Center(child: Text('Grid $index')),
              ),
              childCount: 9,
            ),
          ),
          SliverList(
            delegate: SliverChildBuilderDelegate(
              (context, index) => ListTile(
                title: Text('List Item $index'),
                leading: CircleAvatar(child: Text('$index')),
              ),
              childCount: 10,
            ),
          ),
        ],
      ),
    );
  }
}
