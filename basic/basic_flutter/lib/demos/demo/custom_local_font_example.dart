import 'package:flutter/material.dart';

/// Custom Local Font
/// 使用本地自定义字体的示例页面
class CustomLocalFontExample extends StatelessWidget {
  const CustomLocalFontExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return CustomLocalFontRoute(title: title);
  }
}

class CustomLocalFontRoute extends StatelessWidget {
  const CustomLocalFontRoute({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('字体展示'),
            const SizedBox(height: 16),
            _buildFontShowcase(),
            const SizedBox(height: 32),
            _buildSectionTitle('不同字体权重'),
            const SizedBox(height: 16),
            _buildWeightShowcase(),
            const SizedBox(height: 32),
            _buildSectionTitle('与其他字体对比'),
            const SizedBox(height: 16),
            _buildComparisonDemo(),
            const SizedBox(height: 32),
            _buildSectionTitle('使用说明'),
            const SizedBox(height: 16),
            _buildUsageInstructions(),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Text(
      title,
      style: const TextStyle(
        fontSize: 20,
        fontWeight: FontWeight.bold,
        color: Colors.blue,
      ),
    );
  }

  Widget _buildFontShowcase() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: const BoxDecoration(
        color: Color(0xFFF5F5F5),
        borderRadius: BorderRadius.all(Radius.circular(12)),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Juice Font',
            style: TextStyle(fontFamily: 'Juice', fontSize: 28),
          ),
          SizedBox(height: 12),
          Text(
            'Hello World 你好世界',
            style: TextStyle(fontFamily: 'Juice', fontSize: 22),
          ),
          SizedBox(height: 12),
          Text(
            'The quick brown fox jumps over the lazy dog',
            style: TextStyle(fontFamily: 'Juice', fontSize: 18),
          ),
        ],
      ),
    );
  }

  Widget _buildWeightShowcase() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: const BoxDecoration(
        color: Color(0xFFF5F5F5),
        borderRadius: BorderRadius.all(Radius.circular(12)),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Regular (400)',
            style: TextStyle(
              fontFamily: 'Juice',
              fontSize: 18,
              fontWeight: FontWeight.w400,
            ),
          ),
          SizedBox(height: 8),
          Text(
            'Medium (500)',
            style: TextStyle(
              fontFamily: 'Juice',
              fontSize: 18,
              fontWeight: FontWeight.w500,
            ),
          ),
          SizedBox(height: 8),
          Text(
            'SemiBold (600)',
            style: TextStyle(
              fontFamily: 'Juice',
              fontSize: 18,
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 8),
          Text(
            'Bold (700)',
            style: TextStyle(
              fontFamily: 'Juice',
              fontSize: 18,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildComparisonDemo() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue[50],
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.blue[200]!),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('System Default Font', style: TextStyle(fontSize: 18)),
          SizedBox(height: 12),
          Text(
            'Juice Custom Font',
            style: TextStyle(fontFamily: 'Juice', fontSize: 18),
          ),
        ],
      ),
    );
  }

  Widget _buildUsageInstructions() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.green[50],
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.green[200]!),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '1. 将字体文件放入 assets/fonts/ 目录',
            style: TextStyle(fontSize: 14, height: 1.6),
          ),
          Text(
            '2. 在 pubspec.yaml 中配置 fonts 节点',
            style: TextStyle(fontSize: 14, height: 1.6),
          ),
          Text(
            '3. 使用 fontFamily: "Juice" 应用字体',
            style: TextStyle(fontSize: 14, height: 1.6),
          ),
        ],
      ),
    );
  }
}
