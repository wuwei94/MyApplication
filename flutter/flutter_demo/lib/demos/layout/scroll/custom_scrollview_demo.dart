import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// CustomScrollView
/// Demonstrates custom scrollable with slivers
class CustomScrollViewDemoPage extends BasicLayoutPage {
  const CustomScrollViewDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<CustomScrollViewDemoPage> createState() =>
      _CustomScrollViewDemoPageState();
}

class _CustomScrollViewDemoPageState
    extends BasicLayoutPageState<CustomScrollViewDemoPage> {
  @override
  Widget buildPreview() {
    return CustomScrollView(
      slivers: <Widget>[
        SliverAppBar(
          expandedHeight: 200,
          floating: false,
          pinned: true,
          flexibleSpace: FlexibleSpaceBar(
            title: Text(widget.title),
            background: Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: <Color>[Colors.blue, Colors.purple],
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
            (BuildContext context, int index) => Container(
              color: Colors.blue.shade100,
              child: Center(child: Text('Grid $index')),
            ),
            childCount: 9,
          ),
        ),
        SliverList(
          delegate: SliverChildBuilderDelegate(
            (BuildContext context, int index) => ListTile(
              title: Text('List Item $index'),
              leading: CircleAvatar(child: Text('$index')),
            ),
            childCount: 10,
          ),
        ),
      ],
    );
  }
}
