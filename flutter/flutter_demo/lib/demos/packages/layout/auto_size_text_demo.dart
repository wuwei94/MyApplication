import 'package:auto_size_text/auto_size_text.dart';
import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// AutoSizeText — 有限空间自动缩放字号
///
/// 核心机制与避坑点：
/// 1. 约束前提：必须有 bounded constraints，否则无法测量缩放。
/// 2. 参数面：`maxLines` / `minFontSize` / `presetFontSizes` / `AutoSizeGroup` 控制缩放策略。
///
/// 官方参考：
/// https://pub.dev/packages/auto_size_text
class AutoSizeTextDemoPage extends BasicLayoutPage {
  const AutoSizeTextDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<AutoSizeTextDemoPage> createState() =>
      _AutoSizeTextDemoPageState();
}

class _AutoSizeTextDemoPageState
    extends BasicLayoutPageState<AutoSizeTextDemoPage> {
  static const String _headlineText =
      'AutoSizeText 会在有限空间里自动缩放字号，让长标题依然保持可读性。';

  double _sampleWidth = 220;
  int _maxLines = 2;
  bool _usePresetFontSizes = false;

  @override
  List<String> buildList() => const <String>[
        '1. 切换示例宽度',
        '2. 切换最大行数',
        '3. 开关 presetFontSizes',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        setState(() {
          _sampleWidth = _sampleWidth >= 300 ? 160 : _sampleWidth + 40;
        });
      case 1:
        setState(() {
          _maxLines = _maxLines >= 3 ? 1 : _maxLines + 1;
        });
      case 2:
        setState(() {
          _usePresetFontSizes = !_usePresetFontSizes;
        });
    }
  }

  @override
  Widget buildPreview() {
    final ThemeData theme = Theme.of(context);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            'width=${_sampleWidth.round()} maxLines=$_maxLines '
            'preset=$_usePresetFontSizes',
            style: theme.textTheme.titleSmall,
          ),
          const SizedBox(height: 12),
          Row(
            children: <Widget>[
              Text('普通 Text', style: theme.textTheme.labelLarge),
              const SizedBox(width: 12),
              SizedBox(
                width: _sampleWidth,
                child: Text(
                  _headlineText,
                  maxLines: _maxLines,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontSize: 24),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Row(
            children: <Widget>[
              Text('AutoSizeText', style: theme.textTheme.labelLarge),
              const SizedBox(width: 12),
              SizedBox(
                width: _sampleWidth,
                child: AutoSizeText(
                  _headlineText,
                  maxLines: _maxLines,
                  minFontSize: 10,
                  presetFontSizes: _usePresetFontSizes
                      ? const <double>[24, 18, 14, 12, 10]
                      : null,
                  overflowReplacement: const Text('内容过长无法展示'),
                  style: const TextStyle(fontSize: 24),
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          Text('AutoSizeGroup', style: theme.textTheme.titleSmall),
          const SizedBox(height: 8),
          Row(
            children: <Widget>[
              Expanded(
                child: AutoSizeText(
                  'Design system tokens',
                  maxLines: 1,
                  minFontSize: 8,
                  group: _headlineGroup,
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: AutoSizeText(
                  'Operational excellence weekly review',
                  maxLines: 1,
                  minFontSize: 8,
                  group: _headlineGroup,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  final AutoSizeGroup _headlineGroup = AutoSizeGroup();
}
