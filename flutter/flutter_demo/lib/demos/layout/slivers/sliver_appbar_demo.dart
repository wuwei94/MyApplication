import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// SliverAppBar
/// Demonstrates collapsible app bar
class SliverAppBarDemoPage extends BasicLayoutPage {
  const SliverAppBarDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<SliverAppBarDemoPage> createState() =>
      _SliverAppBarDemoPageState();
}

class _SliverAppBarDemoPageState
    extends BasicLayoutPageState<SliverAppBarDemoPage> {
  @override
  Widget buildPreview() {
    return CustomScrollView(
      slivers: <Widget>[
        SliverAppBar(
          expandedHeight: 250,
          floating: true,
          snap: true,
          pinned: true,
          flexibleSpace: FlexibleSpaceBar(
            title: Text(widget.title),
            background: Image.network(
              'https://picsum.photos/400/300',
              fit: BoxFit.cover,
            ),
          ),
        ),
        SliverList(
          delegate: SliverChildBuilderDelegate(
            (BuildContext context, int index) => ListTile(
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
    );
  }
}
