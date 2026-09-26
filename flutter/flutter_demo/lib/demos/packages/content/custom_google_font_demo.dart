import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:google_fonts/google_fonts.dart';

/// Custom Google Font — Google Fonts 字体展示
///
/// 核心机制与避坑点：
/// 1. 加载语义：`GoogleFonts.roboto()` 按需下载并缓存字体，首次可能有网络耗时。
/// 2. 权重映射：通过 `FontWeight` 映射到对应字重，无需手写字体文件。
///
/// 官方参考：
/// https://pub.dev/packages/google_fonts
class CustomGoogleFontDemoPage extends BasicLayoutPage {
  const CustomGoogleFontDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<CustomGoogleFontDemoPage> createState() =>
      _CustomGoogleFontDemoPageState();
}

class _CustomGoogleFontDemoPageState
    extends BasicLayoutPageState<CustomGoogleFontDemoPage> {
  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildSectionTitle('字体展示'),
          Text('Roboto Font', style: GoogleFonts.roboto(fontSize: 28)),
          const SizedBox(height: 12),
          Text('Hello World 你好世界', style: GoogleFonts.roboto(fontSize: 22)),
          const SizedBox(height: 12),
          Text(
            'The quick brown fox jumps over the lazy dog',
            style: GoogleFonts.roboto(fontSize: 18),
          ),
          const SizedBox(height: 24),
          _buildSectionTitle('不同字体权重'),
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
          const SizedBox(height: 24),
          _buildSectionTitle('与其他字体对比'),
          const Text('System Default Font', style: TextStyle(fontSize: 18)),
          const SizedBox(height: 12),
          Text('Roboto Google Font', style: GoogleFonts.roboto(fontSize: 18)),
        ],
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(
        title,
        style: const TextStyle(
          fontSize: 18,
          fontWeight: FontWeight.bold,
          color: Colors.blue,
        ),
      ),
    );
  }
}
