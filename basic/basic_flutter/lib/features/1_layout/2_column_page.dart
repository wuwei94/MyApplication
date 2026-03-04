import 'package:flutter/material.dart';

/// Column 垂直布局示例页面
class ColumnPage extends StatelessWidget {
  const ColumnPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Column 垂直布局')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('1. 基础 Column'),
            _buildBasicColumn(),
            const SizedBox(height: 24),
            _buildSectionTitle('2. MainAxisAlignment 主轴对齐'),
            _buildMainAxisAlignmentDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('3. CrossAxisAlignment 交叉轴对齐'),
            _buildCrossAxisAlignmentDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('4. MainAxisSize 主轴尺寸'),
            _buildMainAxisSizeDemo(),
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

  // 基础 Column 示例
  Widget _buildBasicColumn() {
    return Container(
      color: Colors.grey.shade200,
      padding: const EdgeInsets.all(8),
      child: Column(
        children: [
          Container(width: 50, height: 50, color: Colors.red),
          Container(width: 50, height: 50, color: Colors.green),
          Container(width: 50, height: 50, color: Colors.blue),
        ],
      ),
    );
  }

  // MainAxisAlignment 主轴对齐示例
  Widget _buildMainAxisAlignmentDemo() {
    return Column(
      children: [
        _buildAlignmentItem('start', MainAxisAlignment.start),
        _buildAlignmentItem('center', MainAxisAlignment.center),
        _buildAlignmentItem('end', MainAxisAlignment.end),
        _buildAlignmentItem('spaceAround', MainAxisAlignment.spaceAround),
        _buildAlignmentItem('spaceBetween', MainAxisAlignment.spaceBetween),
        _buildAlignmentItem('spaceEvenly', MainAxisAlignment.spaceEvenly),
      ],
    );
  }

  Widget _buildAlignmentItem(String label, MainAxisAlignment alignment) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
          Container(
            height: 80,
            color: Colors.grey.shade200,
            padding: const EdgeInsets.all(4),
            child: Column(
              mainAxisAlignment: alignment,
              children: [
                Container(width: 30, height: 15, color: Colors.red),
                Container(width: 30, height: 15, color: Colors.green),
                Container(width: 30, height: 15, color: Colors.blue),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // CrossAxisAlignment 交叉轴对齐示例
  Widget _buildCrossAxisAlignmentDemo() {
    return Column(
      children: [
        _buildCrossAlignmentItem('start', CrossAxisAlignment.start),
        _buildCrossAlignmentItem('center', CrossAxisAlignment.center),
        _buildCrossAlignmentItem('end', CrossAxisAlignment.end),
        _buildCrossAlignmentItem('stretch', CrossAxisAlignment.stretch),
      ],
    );
  }

  Widget _buildCrossAlignmentItem(String label, CrossAxisAlignment alignment) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
          Container(
            width: double.infinity,
            color: Colors.grey.shade200,
            padding: const EdgeInsets.all(4),
            child: Column(
              crossAxisAlignment: alignment,
              children: [
                Container(width: 30, height: 20, color: Colors.red),
                Container(width: 60, height: 20, color: Colors.green),
                Container(width: 45, height: 20, color: Colors.blue),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // MainAxisSize 主轴尺寸示例
  Widget _buildMainAxisSizeDemo() {
    return Column(
      children: [
        _buildMainAxisSizeItem('max (默认)', MainAxisSize.max),
        _buildMainAxisSizeItem('min', MainAxisSize.min),
      ],
    );
  }

  Widget _buildMainAxisSizeItem(String label, MainAxisSize size) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
          Container(
            height: 100,
            width: double.infinity,
            color: Colors.grey.shade200,
            padding: const EdgeInsets.all(4),
            child: Column(
              mainAxisSize: size,
              children: [
                Container(width: 50, height: 30, color: Colors.red),
                Container(width: 50, height: 30, color: Colors.green),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
