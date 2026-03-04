import 'dart:math';

import 'package:flutter/material.dart';

/// Flow 流式布局示例页面
class FlowPage extends StatelessWidget {
  const FlowPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Flow 流式布局')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('1. 基础 Flow'),
            _buildBasicFlow(),
            const SizedBox(height: 24),
            _buildSectionTitle('2. Flow 对齐方式'),
            _buildFlowAlignmentDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('3. 自定义 FlowDelegate'),
            _buildCustomFlowDelegate(),
            const SizedBox(height: 24),
            _buildSectionTitle('4. 实际应用 - 瀑布流'),
            _buildWaterfallFlow(),
            const SizedBox(height: 24),
            _buildSectionTitle('5. 实际应用 - 圆形菜单'),
            _buildCircleMenu(),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(
        title,
        style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.blue),
      ),
    );
  }

  // 基础 Flow 示例
  Widget _buildBasicFlow() {
    return Container(
      color: Colors.grey.shade200,
      padding: const EdgeInsets.all(8),
      child: Flow(
        delegate: _SimpleFlowDelegate(margin: const EdgeInsets.all(4)),
        children: [
          _buildBox('A', Colors.red),
          _buildBox('B', Colors.green),
          _buildBox('C', Colors.blue),
          _buildBox('D', Colors.orange),
          _buildBox('E', Colors.purple),
          _buildBox('F', Colors.teal),
          _buildBox('G', Colors.pink),
          _buildBox('H', Colors.cyan),
        ],
      ),
    );
  }

  Widget _buildBox(String text, Color color) {
    return Container(
      width: 60,
      height: 40,
      color: color,
      alignment: Alignment.center,
      child: Text(text, style: const TextStyle(color: Colors.white)),
    );
  }

  // Flow 对齐方式示例
  Widget _buildFlowAlignmentDemo() {
    return Column(
      children: [
        _buildFlowAlignmentItem('左对齐', _LeftAlignFlowDelegate()),
        const SizedBox(height: 8),
        _buildFlowAlignmentItem('居中对齐', _CenterAlignFlowDelegate()),
        const SizedBox(height: 8),
        _buildFlowAlignmentItem('右对齐', _RightAlignFlowDelegate()),
      ],
    );
  }

  Widget _buildFlowAlignmentItem(String label, FlowDelegate delegate) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          color: Colors.grey.shade200,
          padding: const EdgeInsets.all(8),
          child: Flow(
            delegate: delegate,
            children: [
              _buildSmallBox('1', Colors.red),
              _buildSmallBox('2', Colors.green),
              _buildSmallBox('3', Colors.blue),
              _buildSmallBox('4', Colors.orange),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildSmallBox(String text, Color color) {
    return Container(
      width: 50,
      height: 30,
      margin: const EdgeInsets.all(2),
      color: color,
      alignment: Alignment.center,
      child: Text(text, style: const TextStyle(color: Colors.white, fontSize: 10)),
    );
  }

  // 自定义 FlowDelegate 示例
  Widget _buildCustomFlowDelegate() {
    return Container(
      color: Colors.grey.shade200,
      padding: const EdgeInsets.all(8),
      child: Flow(
        delegate: _StaggeredFlowDelegate(spacing: 8, runSpacing: 8),
        children: [
          _buildHeightBox('1', Colors.red, 40),
          _buildHeightBox('2', Colors.green, 60),
          _buildHeightBox('3', Colors.blue, 35),
          _buildHeightBox('4', Colors.orange, 50),
          _buildHeightBox('5', Colors.purple, 45),
          _buildHeightBox('6', Colors.teal, 55),
        ],
      ),
    );
  }

  Widget _buildHeightBox(String text, Color color, double height) {
    return Container(
      width: 60,
      height: height,
      color: color,
      alignment: Alignment.center,
      child: Text(text, style: const TextStyle(color: Colors.white)),
    );
  }

  // 瀑布流示例
  Widget _buildWaterfallFlow() {
    final heights = [80.0, 120.0, 60.0, 100.0, 90.0, 70.0, 110.0, 85.0];
    final colors = [Colors.red, Colors.green, Colors.blue, Colors.orange, Colors.purple, Colors.teal, Colors.pink, Colors.cyan];

    return Container(
      color: Colors.grey.shade200,
      padding: const EdgeInsets.all(8),
      height: 250,
      child: Flow(
        delegate: _WaterfallFlowDelegate(columnCount: 3, spacing: 8),
        children: heights.asMap().entries.map((entry) {
          return Container(
            width: double.infinity,
            height: entry.value,
            decoration: BoxDecoration(
              color: colors[entry.key % colors.length],
              borderRadius: BorderRadius.circular(8),
            ),
            alignment: Alignment.center,
            child: Text('${entry.key + 1}', style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
          );
        }).toList(),
      ),
    );
  }

  // 圆形菜单示例
  Widget _buildCircleMenu() {
    return Center(
      child: Container(
        width: 200,
        height: 200,
        decoration: BoxDecoration(
          color: Colors.grey.shade200,
          shape: BoxShape.circle,
        ),
        child: Flow(
          delegate: _CircleFlowDelegate(radius: 70),
          children: [
            _buildCircleItem(Icons.home, Colors.red),
            _buildCircleItem(Icons.search, Colors.green),
            _buildCircleItem(Icons.favorite, Colors.blue),
            _buildCircleItem(Icons.settings, Colors.orange),
            _buildCircleItem(Icons.person, Colors.purple),
            _buildCircleItem(Icons.notifications, Colors.teal),
          ],
        ),
      ),
    );
  }

  Widget _buildCircleItem(IconData icon, Color color) {
    return Container(
      width: 40,
      height: 40,
      decoration: BoxDecoration(
        color: color,
        shape: BoxShape.circle,
        boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.2), blurRadius: 4)],
      ),
      child: Icon(icon, color: Colors.white, size: 20),
    );
  }
}

// 简单 FlowDelegate - 从左到右、从上到下排列
class _SimpleFlowDelegate extends FlowDelegate {
  final EdgeInsets margin;

  _SimpleFlowDelegate({this.margin = EdgeInsets.zero});

  @override
  void paintChildren(FlowPaintingContext context) {
    var x = margin.left;
    var y = margin.top;
    var maxHeight = 0.0;

    for (var i = 0; i < context.childCount; i++) {
      final size = context.getChildSize(i)!;

      if (x + size.width + margin.right > context.size.width) {
        x = margin.left;
        y += maxHeight + margin.bottom;
        maxHeight = 0;
      }

      context.paintChild(i, transform: Matrix4.translationValues(x, y, 0));
      x += size.width + margin.right;
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

// 左对齐 FlowDelegate
class _LeftAlignFlowDelegate extends FlowDelegate {
  @override
  void paintChildren(FlowPaintingContext context) {
    var x = 0.0;
    var y = 0.0;
    var rowHeight = 0.0;

    for (var i = 0; i < context.childCount; i++) {
      final size = context.getChildSize(i)!;

      if (x + size.width > context.size.width) {
        x = 0;
        y += rowHeight + 4;
        rowHeight = 0;
      }

      context.paintChild(i, transform: Matrix4.translationValues(x, y, 0));
      x += size.width + 4;
      rowHeight = rowHeight > size.height ? rowHeight : size.height;
    }
  }

  @override
  Size getSize(BoxConstraints constraints) => Size(constraints.maxWidth, 100);

  @override
  bool shouldRepaint(covariant FlowDelegate oldDelegate) => false;
}

// 居中对齐 FlowDelegate
class _CenterAlignFlowDelegate extends FlowDelegate {
  @override
  void paintChildren(FlowPaintingContext context) {
    final rows = <List<_FlowItem>>[];
    var currentRow = <_FlowItem>[];
    var currentRowWidth = 0.0;

    for (var i = 0; i < context.childCount; i++) {
      final size = context.getChildSize(i)!;

      if (currentRowWidth + size.width > context.size.width && currentRow.isNotEmpty) {
        rows.add(currentRow);
        currentRow = [];
        currentRowWidth = 0;
      }

      currentRow.add(_FlowItem(index: i, width: size.width, height: size.height));
      currentRowWidth += size.width + 4;
    }

    if (currentRow.isNotEmpty) {
      rows.add(currentRow);
    }

    var y = 0.0;
    for (final row in rows) {
      final rowWidth = row.fold<double>(0, (sum, item) => sum + item.width) + (row.length - 1) * 4;
      var x = (context.size.width - rowWidth) / 2;
      var rowHeight = 0.0;

      for (final item in row) {
        context.paintChild(item.index, transform: Matrix4.translationValues(x, y, 0));
        x += item.width + 4;
        rowHeight = rowHeight > item.height ? rowHeight : item.height;
      }

      y += rowHeight + 4;
    }
  }

  @override
  Size getSize(BoxConstraints constraints) => Size(constraints.maxWidth, 100);

  @override
  bool shouldRepaint(covariant FlowDelegate oldDelegate) => false;
}

// 右对齐 FlowDelegate
class _RightAlignFlowDelegate extends FlowDelegate {
  @override
  void paintChildren(FlowPaintingContext context) {
    final rows = <List<_FlowItem>>[];
    var currentRow = <_FlowItem>[];
    var currentRowWidth = 0.0;

    for (var i = 0; i < context.childCount; i++) {
      final size = context.getChildSize(i)!;

      if (currentRowWidth + size.width > context.size.width && currentRow.isNotEmpty) {
        rows.add(currentRow);
        currentRow = [];
        currentRowWidth = 0;
      }

      currentRow.add(_FlowItem(index: i, width: size.width, height: size.height));
      currentRowWidth += size.width + 4;
    }

    if (currentRow.isNotEmpty) {
      rows.add(currentRow);
    }

    var y = 0.0;
    for (final row in rows) {
      final rowWidth = row.fold<double>(0, (sum, item) => sum + item.width) + (row.length - 1) * 4;
      var x = context.size.width - rowWidth;
      var rowHeight = 0.0;

      for (final item in row) {
        context.paintChild(item.index, transform: Matrix4.translationValues(x, y, 0));
        x += item.width + 4;
        rowHeight = rowHeight > item.height ? rowHeight : item.height;
      }

      y += rowHeight + 4;
    }
  }

  @override
  Size getSize(BoxConstraints constraints) => Size(constraints.maxWidth, 100);

  @override
  bool shouldRepaint(covariant FlowDelegate oldDelegate) => false;
}

class _FlowItem {
  final int index;
  final double width;
  final double height;

  _FlowItem({required this.index, required this.width, required this.height});
}

// 交错 FlowDelegate
class _StaggeredFlowDelegate extends FlowDelegate {
  final double spacing;
  final double runSpacing;

  _StaggeredFlowDelegate({this.spacing = 8, this.runSpacing = 8});

  @override
  void paintChildren(FlowPaintingContext context) {
    var x = 0.0;
    var y = 0.0;
    final columnWidths = <double>[];
    final columnHeights = <double>[];

    for (var i = 0; i < context.childCount; i++) {
      final size = context.getChildSize(i)!;

      // 找到最短的列
      var minColumn = 0;
      double minHeight = columnHeights.isEmpty ? 0 : columnHeights[0];
      for (var j = 1; j < columnHeights.length; j++) {
        if (columnHeights[j] < minHeight) {
          minHeight = columnHeights[j];
          minColumn = j;
        }
      }

      if (columnHeights.isEmpty || minHeight + size.height > context.size.height) {
        // 创建新列
        x = columnWidths.isEmpty ? 0 : columnWidths.last + size.width + spacing;
        y = 0;
        columnWidths.add(x);
        columnHeights.add(size.height + runSpacing);
      } else {
        // 添加到最短列
        x = columnWidths[minColumn];
        y = minHeight;
        columnHeights[minColumn] += size.height + runSpacing;
      }

      context.paintChild(i, transform: Matrix4.translationValues(x, y, 0));
    }
  }

  @override
  Size getSize(BoxConstraints constraints) => Size(constraints.maxWidth, 200);

  @override
  bool shouldRepaint(covariant FlowDelegate oldDelegate) => false;
}

// 瀑布流 FlowDelegate
class _WaterfallFlowDelegate extends FlowDelegate {
  final int columnCount;
  final double spacing;

  _WaterfallFlowDelegate({required this.columnCount, this.spacing = 8});

  @override
  void paintChildren(FlowPaintingContext context) {
    final columnHeights = List<double>.filled(columnCount, 0);
    final columnWidth = (context.size.width - (columnCount - 1) * spacing) / columnCount;

    for (var i = 0; i < context.childCount; i++) {
      final size = context.getChildSize(i)!;

      // 找到最短的列
      var minColumn = 0;
      var minHeight = columnHeights[0];
      for (var j = 1; j < columnCount; j++) {
        if (columnHeights[j] < minHeight) {
          minHeight = columnHeights[j];
          minColumn = j;
        }
      }

      final x = minColumn * (columnWidth + spacing);
      final y = minHeight;

      // 缩放子元素以适应列宽
      final scale = columnWidth / size.width;
      context.paintChild(
        i,
        transform: Matrix4.translationValues(x, y, 0)..scale(scale, 1),
      );

      columnHeights[minColumn] += size.height + spacing;
    }
  }

  @override
  Size getSize(BoxConstraints constraints) => Size(constraints.maxWidth, 250);

  @override
  bool shouldRepaint(covariant FlowDelegate oldDelegate) => false;
}

// 圆形 FlowDelegate
class _CircleFlowDelegate extends FlowDelegate {
  final double radius;

  _CircleFlowDelegate({required this.radius});

  @override
  void paintChildren(FlowPaintingContext context) {
    final centerX = context.size.width / 2;
    final centerY = context.size.height / 2;
    final angleStep = 2 * 3.14159 / context.childCount;

    for (var i = 0; i < context.childCount; i++) {
      final angle = i * angleStep - 3.14159 / 2;
      final x = centerX + radius * cos(angle) - 20;
      final y = centerY + radius * sin(angle) - 20;

      context.paintChild(i, transform: Matrix4.translationValues(x, y, 0));
    }
  }

  @override
  Size getSize(BoxConstraints constraints) => const Size(200, 200);

  @override
  bool shouldRepaint(covariant FlowDelegate oldDelegate) => false;
}
