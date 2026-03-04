import 'package:flutter/material.dart';

/// Flexible 弹性布局示例页面
class FlexiblePage extends StatelessWidget {
  const FlexiblePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Flexible 弹性布局')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('1. 基础 Flexible'),
            _buildBasicFlexible(),
            const SizedBox(height: 24),
            _buildSectionTitle('2. Flex 比例分配'),
            _buildFlexRatioDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('3. fit: FlexFit.loose (默认)'),
            _buildFlexFitLooseDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('4. fit: FlexFit.tight'),
            _buildFlexFitTightDemo(),
            const SizedBox(height: 24),
            _buildSectionTitle('5. Column 中的 Flexible'),
            _buildColumnFlexibleDemo(),
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

  // 基础 Flexible 示例
  Widget _buildBasicFlexible() {
    return Container(
      color: Colors.grey.shade200,
      height: 80,
      child: Row(
        children: [
          Container(width: 60, height: 60, color: Colors.red),
          Flexible(
            child: Container(
              height: 60,
              color: Colors.green,
              alignment: Alignment.center,
              child: const Text('Flexible', style: TextStyle(color: Colors.white)),
            ),
          ),
          Container(width: 60, height: 60, color: Colors.blue),
        ],
      ),
    );
  }

  // Flex 比例分配示例
  Widget _buildFlexRatioDemo() {
    return Column(
      children: [
        _buildRatioItem('flex: 1 : 2 : 1', [1, 2, 1]),
        _buildRatioItem('flex: 2 : 3 : 1', [2, 3, 1]),
        _buildRatioItem('flex: 1 : 1 : 1', [1, 1, 1]),
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
                Flexible(
                  flex: flexValues[0],
                  child: Container(color: Colors.red, child: const Center(child: Text('1', style: TextStyle(color: Colors.white)))),
                ),
                const SizedBox(width: 4),
                Flexible(
                  flex: flexValues[1],
                  child: Container(color: Colors.green, child: Center(child: Text('${flexValues[1]}', style: const TextStyle(color: Colors.white)))),
                ),
                const SizedBox(width: 4),
                Flexible(
                  flex: flexValues[2],
                  child: Container(color: Colors.blue, child: const Center(child: Text('1', style: TextStyle(color: Colors.white)))),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // FlexFit.loose 示例
  Widget _buildFlexFitLooseDemo() {
    return Container(
      color: Colors.grey.shade200,
      height: 100,
      child: Row(
        children: [
          Flexible(
            fit: FlexFit.loose,
            flex: 1,
            child: Container(
              width: 50,
              height: 50,
              color: Colors.red,
              alignment: Alignment.center,
              child: const Text('loose\n(50x50)', style: TextStyle(color: Colors.white, fontSize: 10)),
            ),
          ),
          Flexible(
            fit: FlexFit.loose,
            flex: 2,
            child: Container(
              height: 80,
              color: Colors.green,
              alignment: Alignment.center,
              child: const Text('loose flex:2\n(高度80)', style: TextStyle(color: Colors.white, fontSize: 10)),
            ),
          ),
        ],
      ),
    );
  }

  // FlexFit.tight 示例
  Widget _buildFlexFitTightDemo() {
    return Container(
      color: Colors.grey.shade200,
      height: 100,
      child: Row(
        children: [
          Flexible(
            fit: FlexFit.tight,
            flex: 1,
            child: Container(
              color: Colors.red,
              alignment: Alignment.center,
              child: const Text('tight flex:1', style: TextStyle(color: Colors.white)),
            ),
          ),
          Flexible(
            fit: FlexFit.tight,
            flex: 2,
            child: Container(
              color: Colors.green,
              alignment: Alignment.center,
              child: const Text('tight flex:2', style: TextStyle(color: Colors.white)),
            ),
          ),
        ],
      ),
    );
  }

  // Column 中的 Flexible
  Widget _buildColumnFlexibleDemo() {
    return Container(
      color: Colors.grey.shade200,
      height: 200,
      child: Column(
        children: [
          Container(height: 40, color: Colors.red),
          Flexible(
            flex: 1,
            child: Container(
              color: Colors.green,
              alignment: Alignment.center,
              child: const Text('Flexible flex:1', style: TextStyle(color: Colors.white)),
            ),
          ),
          Flexible(
            flex: 2,
            child: Container(
              color: Colors.blue,
              alignment: Alignment.center,
              child: const Text('Flexible flex:2', style: TextStyle(color: Colors.white)),
            ),
          ),
        ],
      ),
    );
  }
}
