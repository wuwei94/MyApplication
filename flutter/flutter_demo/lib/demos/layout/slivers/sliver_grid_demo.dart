import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// SliverGrid
/// Demonstrates the usage of SliverGrid widget
class SliverGridDemoPage extends BasicLayoutPage {
  const SliverGridDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<SliverGridDemoPage> createState() =>
      _SliverGridDemoPageState();
}

class _SliverGridDemoPageState extends BasicLayoutPageState<SliverGridDemoPage> {
  @override
  Widget buildPreview() {
    return CustomScrollView(
      slivers: <Widget>[
        SliverAppBar(
          expandedHeight: 150,
          floating: true,
          flexibleSpace: FlexibleSpaceBar(
            title: Text(widget.title),
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
          delegate: SliverChildBuilderDelegate(
            (BuildContext context, int index) {
              return Container(
                margin: const EdgeInsets.all(4),
                decoration: BoxDecoration(
                  color: Colors.green.shade100,
                  borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
                ),
                child: Center(child: Text('Grid $index')),
              );
            },
            childCount: 30,
          ),
        ),
      ],
    );
  }
}
