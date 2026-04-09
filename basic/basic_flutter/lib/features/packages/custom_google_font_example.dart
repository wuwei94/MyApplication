import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

/// Custom Google Font
/// 使用 Google Fonts 第三方字体的示例页面
class CustomGoogleFontExample extends StatelessWidget {
  const CustomGoogleFontExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return CustomGoogleFontRoute(title: title);
  }
}

class CustomGoogleFontRoute extends StatelessWidget {
  const CustomGoogleFontRoute({super.key, required this.title});

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
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Roboto Font',
            style: GoogleFonts.roboto(
              fontSize: 28,
            ),
          ),
          const SizedBox(height: 12),
          Text(
            'Hello World 你好世界',
            style: GoogleFonts.roboto(
              fontSize: 22,
            ),
          ),
          const SizedBox(height: 12),
          Text(
            'The quick brown fox jumps over the lazy dog',
            style: GoogleFonts.roboto(
              fontSize: 18,
            ),
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
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Regular (400)',
            style: GoogleFonts.roboto(
              fontSize: 18,
              fontWeight: FontWeight.w400,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Medium (500)',
            style: GoogleFonts.roboto(
              fontSize: 18,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'SemiBold (600)',
            style: GoogleFonts.roboto(
              fontSize: 18,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Bold (700)',
            style: GoogleFonts.roboto(
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
        color: Colors.orange[50],
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange[200]!),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'System Default Font',
            style: TextStyle(fontSize: 18),
          ),
          const SizedBox(height: 12),
          Text(
            'Roboto Google Font',
            style: GoogleFonts.roboto(
              fontSize: 18,
            ),
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
            '1. 添加 google_fonts 依赖到 pubspec.yaml',
            style: TextStyle(fontSize: 14, height: 1.6),
          ),
          Text(
            '2. 导入 package:google_fonts/google_fonts.dart',
            style: TextStyle(fontSize: 14, height: 1.6),
          ),
          Text(
            '3. 使用 GoogleFonts.roboto() 等方法应用字体',
            style: TextStyle(fontSize: 14, height: 1.6),
          ),
          Text(
            '4. 字体会自动从网络下载并缓存',
            style: TextStyle(fontSize: 14, height: 1.6),
          ),
        ],
      ),
    );
  }
}
