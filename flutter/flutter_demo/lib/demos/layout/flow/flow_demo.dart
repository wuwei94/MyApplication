import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Flow
/// Demonstrates custom flow layout with Flow widget
class FlowDemoPage extends BasicLayoutPage {
  const FlowDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<FlowDemoPage> createState() => _FlowDemoPageState();
}

class _FlowDemoPageState extends BasicLayoutPageState<FlowDemoPage> {
  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildSectionTitle('Flow - 自定义布局'),
          _buildBasicFlow(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Flow 菜单动画'),
          _buildFlowMenu(),
        ],
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

  Widget _buildBasicFlow() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Flow(
        delegate: _SimpleFlowDelegate(padding: const EdgeInsets.all(8)),
        children: <Widget>[
          _buildFlowItem('Flutter', Colors.red),
          _buildFlowItem('Dart', Colors.green),
          _buildFlowItem('Android', Colors.blue),
          _buildFlowItem('iOS', Colors.orange),
          _buildFlowItem('Web', Colors.purple),
          _buildFlowItem('Desktop', Colors.teal),
        ],
      ),
    );
  }

  Widget _buildFlowMenu() {
    return Container(
      height: 200,
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: const _FlowMenuDemo(),
    );
  }

  Widget _buildFlowItem(String label, Color color) {
    return Container(
      margin: const EdgeInsets.all(4),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.2),
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
        border: Border.all(color: color),
      ),
      child: Text(label, style: TextStyle(color: color)),
    );
  }
}

class _SimpleFlowDelegate extends FlowDelegate {
  _SimpleFlowDelegate({this.padding = EdgeInsets.zero});

  final EdgeInsets padding;

  @override
  void paintChildren(FlowPaintingContext context) {
    double x = padding.left;
    double y = padding.top;
    double maxHeight = 0.0;

    for (int i = 0; i < context.childCount; i++) {
      final Size? size = context.getChildSize(i);
      if (size == null) continue;

      if (x + size.width > context.size.width - padding.right) {
        x = padding.left;
        y += maxHeight + 8;
        maxHeight = 0;
      }

      context.paintChild(i, transform: Matrix4.translationValues(x, y, 0));
      x += size.width + 8;
      maxHeight = maxHeight > size.height ? maxHeight : size.height;
    }
  }

  @override
  Size getSize(BoxConstraints constraints) {
    return Size(constraints.maxWidth, double.infinity);
  }

  @override
  bool shouldRepaint(covariant FlowDelegate oldDelegate) => false;
}

class _FlowMenuDemo extends StatefulWidget {
  const _FlowMenuDemo();

  @override
  State<_FlowMenuDemo> createState() => _FlowMenuDemoState();
}

class _FlowMenuDemoState extends State<_FlowMenuDemo>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  bool _isOpen = false;

  static const List<IconData> _icons = <IconData>[
    Icons.home,
    Icons.search,
    Icons.favorite,
    Icons.settings,
    Icons.person,
  ];

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 300),
      vsync: this,
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _toggleMenu() {
    setState(() {
      _isOpen = !_isOpen;
      if (_isOpen) {
        _controller.forward();
      } else {
        _controller.reverse();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Flow(
      delegate: _FlowMenuDelegate(controller: _controller),
      children: <Widget>[
        ...List<Widget>.generate(_icons.length, (int index) {
          return FloatingActionButton.small(
            heroTag: 'flow_$index',
            onPressed: () {},
            backgroundColor: Colors.purple,
            child: Icon(_icons[index], color: Colors.white),
          );
        }),
        FloatingActionButton(
          heroTag: 'flow_menu',
          onPressed: _toggleMenu,
          backgroundColor: Colors.purple.shade700,
          child: AnimatedIcon(
            icon: AnimatedIcons.menu_close,
            progress: _controller,
            color: Colors.white,
          ),
        ),
      ],
    );
  }
}

class _FlowMenuDelegate extends FlowDelegate {
  _FlowMenuDelegate({required this.controller}) : super(repaint: controller);

  final Animation<double> controller;

  @override
  void paintChildren(FlowPaintingContext context) {
    final Size size = context.size;
    final double xStart = size.width - 70;
    final double yStart = size.height - 70;

    context.paintChild(
      context.childCount - 1,
      transform: Matrix4.translationValues(xStart, yStart, 0),
    );

    for (int i = context.childCount - 2; i >= 0; i--) {
      final Size? childSize = context.getChildSize(i);
      if (childSize == null) continue;

      final double radius = 80 * controller.value;
      final double angle = (i / (context.childCount - 2)) * pi / 2;
      final double x =
          xStart - radius * cos(angle) - childSize.width / 2 + 28;
      final double y =
          yStart - radius * sin(angle) - childSize.height / 2 + 28;

      context.paintChild(
        i,
        transform: Matrix4.translationValues(x, y, 0),
        opacity: controller.value,
      );
    }
  }

  @override
  bool shouldRepaint(covariant FlowDelegate oldDelegate) => true;
}
