import 'package:flutter/material.dart';

/// Wrap 流式布局示例页面
class WrapPage extends StatelessWidget {
  const WrapPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Wrap 流式布局')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('1. 基础 Wrap'),
            _buildBasicWrap(),
            const SizedBox(height: 24),
            _buildSectionTitle('2. 主轴对齐 (alignment)'),
            _buildAlignmentDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('3. 交叉轴对齐 (crossAxisAlignment)'),
            _buildCrossAxisAlignmentDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('4. 间距设置'),
            _buildSpacingDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('5. 实际应用 - 标签云'),
            _buildTagCloudDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('6. 实际应用 - 按钮组'),
            _buildButtonGroupDemo(),
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

  // 基础 Wrap 示例
  Widget _buildBasicWrap() {
    return Container(
      color: Colors.grey.shade200,
      padding: const EdgeInsets.all(8),
      child: Wrap(
        children: [
          _buildBox('A', Colors.red),
          _buildBox('B', Colors.green),
          _buildBox('C', Colors.blue),
          _buildBox('D', Colors.orange),
          _buildBox('E', Colors.purple),
          _buildBox('F', Colors.teal),
        ],
      ),
    );
  }

  Widget _buildBox(String text, Color color) {
    return Container(
      width: 60,
      height: 40,
      margin: const EdgeInsets.all(4),
      color: color,
      alignment: Alignment.center,
      child: Text(text, style: const TextStyle(color: Colors.white)),
    );
  }

  // 主轴对齐示例
  Widget _buildAlignmentDemo() {
    return Column(
      children: [
        _buildAlignmentItem('start', WrapAlignment.start),
        _buildAlignmentItem('center', WrapAlignment.center),
        _buildAlignmentItem('end', WrapAlignment.end),
        _buildAlignmentItem('spaceAround', WrapAlignment.spaceAround),
        _buildAlignmentItem('spaceBetween', WrapAlignment.spaceBetween),
        _buildAlignmentItem('spaceEvenly', WrapAlignment.spaceEvenly),
      ],
    );
  }

  Widget _buildAlignmentItem(String label, WrapAlignment alignment) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
          Container(
            color: Colors.grey.shade200,
            padding: const EdgeInsets.all(4),
            child: Wrap(
              alignment: alignment,
              children: [
                _buildSmallBox('1', Colors.red),
                _buildSmallBox('2', Colors.green),
                _buildSmallBox('3', Colors.blue),
              ],
            ),
          ),
        ],
      ),
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

  // 交叉轴对齐示例
  Widget _buildCrossAxisAlignmentDemo() {
    return Column(
      children: [
        _buildCrossAlignmentItem('start', WrapCrossAlignment.start),
        _buildCrossAlignmentItem('center', WrapCrossAlignment.center),
        _buildCrossAlignmentItem('end', WrapCrossAlignment.end),
      ],
    );
  }

  Widget _buildCrossAlignmentItem(String label, WrapCrossAlignment alignment) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
          Container(
            color: Colors.grey.shade200,
            padding: const EdgeInsets.all(4),
            child: Wrap(
              crossAxisAlignment: alignment,
              children: [
                Container(width: 40, height: 20, color: Colors.red, margin: const EdgeInsets.all(2)),
                Container(width: 40, height: 40, color: Colors.green, margin: const EdgeInsets.all(2)),
                Container(width: 40, height: 30, color: Colors.blue, margin: const EdgeInsets.all(2)),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // 间距设置示例
  Widget _buildSpacingDemo() {
    return Column(
      children: [
        const Text('spacing: 8, runSpacing: 8', style: TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          color: Colors.grey.shade200,
          padding: const EdgeInsets.all(8),
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _buildBox('1', Colors.red),
              _buildBox('2', Colors.green),
              _buildBox('3', Colors.blue),
              _buildBox('4', Colors.orange),
              _buildBox('5', Colors.purple),
            ],
          ),
        ),
        const SizedBox(height: 8),
        const Text('spacing: 16, runSpacing: 4', style: TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          color: Colors.grey.shade200,
          padding: const EdgeInsets.all(8),
          child: Wrap(
            spacing: 16,
            runSpacing: 4,
            children: [
              _buildBox('1', Colors.red),
              _buildBox('2', Colors.green),
              _buildBox('3', Colors.blue),
              _buildBox('4', Colors.orange),
              _buildBox('5', Colors.purple),
            ],
          ),
        ),
      ],
    );
  }

  // 标签云示例
  Widget _buildTagCloudDemo() {
    final tags = ['Flutter', 'Dart', 'Android', 'iOS', 'Web', 'Desktop', 'Mobile', 'UI', 'UX', 'Design', 'Development', 'Programming'];
    final colors = [Colors.red, Colors.green, Colors.blue, Colors.orange, Colors.purple, Colors.teal, Colors.pink, Colors.cyan];

    return Container(
      color: Colors.grey.shade200,
      padding: const EdgeInsets.all(12),
      child: Wrap(
        spacing: 8,
        runSpacing: 8,
        children: tags.asMap().entries.map((entry) {
          return Chip(
            label: Text(entry.value),
            backgroundColor: colors[entry.key % colors.length].withOpacity(0.2),
            side: BorderSide(color: colors[entry.key % colors.length]),
          );
        }).toList(),
      ),
    );
  }

  // 按钮组示例
  Widget _buildButtonGroupDemo() {
    return Container(
      color: Colors.grey.shade200,
      padding: const EdgeInsets.all(12),
      child: Wrap(
        spacing: 8,
        runSpacing: 8,
        alignment: WrapAlignment.center,
        children: [
          ElevatedButton.icon(
            onPressed: () {},
            icon: const Icon(Icons.home),
            label: const Text('首页'),
          ),
          ElevatedButton.icon(
            onPressed: () {},
            icon: const Icon(Icons.search),
            label: const Text('搜索'),
          ),
          ElevatedButton.icon(
            onPressed: () {},
            icon: const Icon(Icons.favorite),
            label: const Text('收藏'),
          ),
          ElevatedButton.icon(
            onPressed: () {},
            icon: const Icon(Icons.settings),
            label: const Text('设置'),
          ),
          ElevatedButton.icon(
            onPressed: () {},
            icon: const Icon(Icons.person),
            label: const Text('我的'),
          ),
          ElevatedButton.icon(
            onPressed: () {},
            icon: const Icon(Icons.notifications),
            label: const Text('消息'),
          ),
        ],
      ),
    );
  }
}
