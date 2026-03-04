import 'package:flutter/material.dart';

/// Expanded 扩展布局示例页面
class ExpandedPage extends StatelessWidget {
  const ExpandedPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Expanded 扩展布局')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('1. 基础 Expanded'),
            _buildBasicExpanded(),
            const SizedBox(height: 24),
            _buildSectionTitle('2. Expanded vs Flexible'),
            _buildExpandedVsFlexible(),
            const SizedBox(height: 24),
            _buildSectionTitle('3. Flex 比例'),
            _buildFlexRatioDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('4. 多个 Expanded'),
            _buildMultipleExpanded(),
            const SizedBox(height: 24),
            _buildSectionTitle('5. Column 中的 Expanded'),
            _buildColumnExpandedDemo(),
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

  // 基础 Expanded 示例
  Widget _buildBasicExpanded() {
    return Container(
      color: Colors.grey.shade200,
      height: 80,
      child: Row(
        children: [
          Container(width: 60, height: 60, color: Colors.red),
          Expanded(
            child: Container(
              color: Colors.green,
              alignment: Alignment.center,
              child: const Text('Expanded', style: TextStyle(color: Colors.white)),
            ),
          ),
          Container(width: 60, height: 60, color: Colors.blue),
        ],
      ),
    );
  }

  // Expanded vs Flexible 对比
  Widget _buildExpandedVsFlexible() {
    return Column(
      children: [
        const Text('Flexible (loose):', style: TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          color: Colors.grey.shade200,
          height: 60,
          child: Row(
            children: [
              Flexible(
                child: Container(
                  width: 50,
                  height: 40,
                  color: Colors.orange,
                  alignment: Alignment.center,
                  child: const Text('50x40', style: TextStyle(color: Colors.white, fontSize: 10)),
                ),
              ),
              Container(width: 60, color: Colors.purple),
            ],
          ),
        ),
        const SizedBox(height: 8),
        const Text('Expanded (tight):', style: TextStyle(fontSize: 12, color: Colors.grey)),
        Container(
          color: Colors.grey.shade200,
          height: 60,
          child: Row(
            children: [
              Expanded(
                child: Container(
                  width: 50,
                  height: 40,
                  color: Colors.orange,
                  alignment: Alignment.center,
                  child: const Text('忽略宽高', style: TextStyle(color: Colors.white, fontSize: 10)),
                ),
              ),
              Container(width: 60, color: Colors.purple),
            ],
          ),
        ),
      ],
    );
  }

  // Flex 比例示例
  Widget _buildFlexRatioDemo() {
    return Column(
      children: [
        _buildRatioItem('1 : 1 : 1', [1, 1, 1]),
        _buildRatioItem('1 : 2 : 3', [1, 2, 3]),
      ],
    );
  }

  Widget _buildRatioItem(String label, List<int> flexValues) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
          Container(
            color: Colors.grey.shade200,
            height: 50,
            child: Row(
              children: [
                Expanded(
                  flex: flexValues[0],
                  child: Container(color: Colors.red, child: Center(child: Text('${flexValues[0]}', style: const TextStyle(color: Colors.white)))),
                ),
                const SizedBox(width: 4),
                Expanded(
                  flex: flexValues[1],
                  child: Container(color: Colors.green, child: Center(child: Text('${flexValues[1]}', style: const TextStyle(color: Colors.white)))),
                ),
                const SizedBox(width: 4),
                Expanded(
                  flex: flexValues[2],
                  child: Container(color: Colors.blue, child: Center(child: Text('${flexValues[2]}', style: const TextStyle(color: Colors.white)))),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // 多个 Expanded
  Widget _buildMultipleExpanded() {
    return Container(
      color: Colors.grey.shade200,
      height: 80,
      child: Row(
        children: [
          Expanded(
            flex: 1,
            child: Container(
              color: Colors.red,
              alignment: Alignment.center,
              child: const Text('1', style: TextStyle(color: Colors.white)),
            ),
          ),
          Expanded(
            flex: 2,
            child: Container(
              color: Colors.green,
              alignment: Alignment.center,
              child: const Text('2', style: TextStyle(color: Colors.white)),
            ),
          ),
          Expanded(
            flex: 1,
            child: Container(
              color: Colors.blue,
              alignment: Alignment.center,
              child: const Text('1', style: TextStyle(color: Colors.white)),
            ),
          ),
        ],
      ),
    );
  }

  // Column 中的 Expanded
  Widget _buildColumnExpandedDemo() {
    return Container(
      color: Colors.grey.shade200,
      height: 200,
      child: Column(
        children: [
          Container(
            height: 40,
            color: Colors.red,
            alignment: Alignment.center,
            child: const Text('固定高度 40', style: TextStyle(color: Colors.white)),
          ),
          Expanded(
            child: Container(
              color: Colors.green,
              alignment: Alignment.center,
              child: const Text('Expanded 填充剩余', style: TextStyle(color: Colors.white)),
            ),
          ),
          Container(
            height: 30,
            color: Colors.blue,
            alignment: Alignment.center,
            child: const Text('固定高度 30', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }
}
