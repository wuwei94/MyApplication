import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// LayoutBuilder
/// Demonstrates responsive layout based on parent constraints
class LayoutBuilderDemoPage extends BasicLayoutPage {
  const LayoutBuilderDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<LayoutBuilderDemoPage> createState() =>
      _LayoutBuilderDemoPageState();
}

class _LayoutBuilderDemoPageState
    extends BasicLayoutPageState<LayoutBuilderDemoPage> {
  @override
  Widget buildPreview() {
    return Column(
      children: <Widget>[
        Expanded(
          child: LayoutBuilder(
            builder: (BuildContext context, BoxConstraints constraints) {
              if (constraints.maxWidth > 600) {
                return _buildWideLayout();
              } else {
                return _buildNarrowLayout();
              }
            },
          ),
        ),
        Container(
          padding: const EdgeInsets.all(16),
          color: Colors.grey.shade200,
          child: LayoutBuilder(
            builder: (BuildContext context, BoxConstraints constraints) {
              return Text(
                'Width: ${constraints.maxWidth.toStringAsFixed(1)}\n'
                'Height: ${constraints.maxHeight.toStringAsFixed(1)}',
                textAlign: TextAlign.center,
              );
            },
          ),
        ),
      ],
    );
  }

  Widget _buildWideLayout() {
    return Row(
      children: <Widget>[
        Expanded(
          child: Container(
            color: Colors.blue.shade100,
            child: const Center(
              child: Text('Sidebar', style: TextStyle(fontSize: 24)),
            ),
          ),
        ),
        Expanded(
          flex: 2,
          child: Container(
            color: Colors.green.shade100,
            child: const Center(
              child: Text('Main Content', style: TextStyle(fontSize: 24)),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildNarrowLayout() {
    return Column(
      children: <Widget>[
        Container(
          height: 100,
          color: Colors.blue.shade100,
          child: const Center(
            child: Text('Header', style: TextStyle(fontSize: 24)),
          ),
        ),
        Expanded(
          child: Container(
            color: Colors.green.shade100,
            child: const Center(
              child: Text('Content', style: TextStyle(fontSize: 24)),
            ),
          ),
        ),
      ],
    );
  }
}
