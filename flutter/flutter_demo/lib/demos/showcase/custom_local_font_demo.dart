import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Custom Local Font — 本地自定义字体展示
///
/// 核心机制与避坑点：
/// 1. 字体接入：字体文件放入 `assets/fonts/`，在 `pubspec.yaml` 的 `fonts` 节点注册。
/// 2. 应用方式：`TextStyle(fontFamily: 'Juice')` 按 family 名加载，字重由 `FontWeight` 映射。
///
/// 官方参考：
/// https://api.flutter.dev/flutter/painting/TextStyle/fontFamily.html
class CustomLocalFontDemoPage extends BasicLayoutPage {
  const CustomLocalFontDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<CustomLocalFontDemoPage> createState() =>
      _CustomLocalFontDemoPageState();
}

class _CustomLocalFontDemoPageState
    extends BasicLayoutPageState<CustomLocalFontDemoPage> {
  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildSectionTitle('字体展示'),
          const SizedBox(height: 12),
          _buildFontShowcase(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('不同字体权重'),
          const SizedBox(height: 12),
          _buildWeightShowcase(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('与其他字体对比'),
          const SizedBox(height: 12),
          _buildComparisonDemo(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('使用说明'),
          const SizedBox(height: 12),
          _buildUsageInstructions(),
        ],
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Text(
      title,
      style: const TextStyle(
        fontSize: 18,
        fontWeight: FontWeight.bold,
        color: Colors.blue,
      ),
    );
  }

  Widget _buildFontShowcase() {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
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
    );
  }

  Widget _buildWeightShowcase() {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
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
    );
  }

  Widget _buildComparisonDemo() {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text('System Default Font', style: TextStyle(fontSize: 18)),
        SizedBox(height: 12),
        Text(
          'Juice Custom Font',
          style: TextStyle(fontFamily: 'Juice', fontSize: 18),
        ),
      ],
    );
  }

  Widget _buildUsageInstructions() {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
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
    );
  }
}
