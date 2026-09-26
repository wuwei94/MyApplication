import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// SliverList
/// Demonstrates the usage of SliverList widget
class SliverListDemoPage extends BasicLayoutPage {
  const SliverListDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<SliverListDemoPage> createState() =>
      _SliverListDemoPageState();
}

class _SliverListDemoPageState extends BasicLayoutPageState<SliverListDemoPage> {
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
              color: Colors.blue,
              child: const Center(
                child: Icon(Icons.list, size: 80, color: Colors.white54),
              ),
            ),
          ),
        ),
        SliverList(
          delegate: SliverChildBuilderDelegate(
            (BuildContext context, int index) {
              return ListTile(
                leading: CircleAvatar(child: Text('$index')),
                title: Text('SliverList Item $index'),
                subtitle: const Text('Part of CustomScrollView'),
              );
            },
            childCount: 20,
          ),
        ),
      ],
    );
  }
}
