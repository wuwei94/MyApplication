import 'package:flutter/material.dart';

/// SliverAppBar Example
/// Demonstrates collapsible app bar
class SliverAppBarDemoPage extends StatelessWidget {
  const SliverAppBarDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SliverAppBarDemoView(title: title);
  }
}

class SliverAppBarDemoView extends StatelessWidget {
  const SliverAppBarDemoView({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: CustomScrollView(
        slivers: [
          SliverAppBar(
            expandedHeight: 250,
            floating: true,
            snap: true,
            pinned: true,
            flexibleSpace: FlexibleSpaceBar(
              title: Text(title),
              background: Image.network(
                'https://picsum.photos/400/300',
                fit: BoxFit.cover,
              ),
            ),
          ),
          SliverList(
            delegate: SliverChildBuilderDelegate(
              (context, index) => ListTile(
                title: Text('Item $index'),
                subtitle: Text('Subtitle for item $index'),
                leading: CircleAvatar(
                  backgroundColor: Colors.blue,
                  child: Text('$index'),
                ),
              ),
              childCount: 30,
            ),
          ),
        ],
      ),
    );
  }
}
