import 'package:auto_size_text/auto_size_text.dart';
import 'package:flutter/material.dart';

/// AutoSizeText
/// https://pub.dev/packages/auto_size_text
class AutoSizeTextDemoPage extends StatelessWidget {
  const AutoSizeTextDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return AutoSizeTextDemoView(title: title);
  }
}

class AutoSizeTextDemoView extends StatefulWidget {
  const AutoSizeTextDemoView({super.key, required this.title});

  final String title;

  @override
  State<AutoSizeTextDemoView> createState() => _AutoSizeTextDemoViewState();
}

class _AutoSizeTextDemoViewState extends State<AutoSizeTextDemoView> {
  static const String _headlineText = 'AutoSizeText 会在有限空间里自动缩放字号，让长标题依然保持可读性。';

  static const List<String> _groupTitles = <String>[
    'Design system tokens',
    'International growth dashboard',
    'Operational excellence weekly review',
  ];

  final AutoSizeGroup _headlineGroup = AutoSizeGroup();

  double _sampleWidth = 220;
  int _maxLines = 2;
  bool _usePresetFontSizes = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
        children: <Widget>[
          const _SectionCard(
            title: 'AutoSizeText 3.0.0',
            subtitle: '这个包适合卡片标题、Banner 文案、按钮文案等“空间固定但内容长度不固定”的场景。',
            child: Wrap(
              spacing: 8,
              runSpacing: 8,
              children: <Widget>[
                _TagChip(label: 'bounded constraints'),
                _TagChip(label: 'maxLines'),
                _TagChip(label: 'minFontSize'),
                _TagChip(label: 'AutoSizeGroup'),
                _TagChip(label: 'overflowReplacement'),
              ],
            ),
          ),
          const SizedBox(height: 16),
          _buildComparisonSection(context),
          const SizedBox(height: 16),
          _buildGroupSection(context),
          const SizedBox(height: 16),
          _buildAdvancedSection(context),
        ],
      ),
    );
  }

  Widget _buildComparisonSection(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final TextStyle headlineStyle =
        theme.textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w700) ??
        const TextStyle(fontSize: 28, fontWeight: FontWeight.w700);

    return _SectionCard(
      title: '基础对比',
      subtitle: '同样的长标题放进同样宽度的容器里，普通 Text 只能截断；AutoSizeText 会先尝试缩小字号。',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            '示例宽度 ${_sampleWidth.round()} px',
            style: theme.textTheme.titleSmall?.copyWith(
              fontWeight: FontWeight.w600,
            ),
          ),
          Slider(
            value: _sampleWidth,
            min: 140,
            max: 320,
            divisions: 9,
            label: _sampleWidth.round().toString(),
            onChanged: (double value) {
              setState(() {
                _sampleWidth = value;
              });
            },
          ),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: <Widget>[
              _LineChip(
                label: '1 line',
                selected: _maxLines == 1,
                onSelected: () => _updateMaxLines(1),
              ),
              _LineChip(
                label: '2 lines',
                selected: _maxLines == 2,
                onSelected: () => _updateMaxLines(2),
              ),
              _LineChip(
                label: '3 lines',
                selected: _maxLines == 3,
                onSelected: () => _updateMaxLines(3),
              ),
              FilterChip(
                label: const Text('presetFontSizes'),
                selected: _usePresetFontSizes,
                onSelected: (bool value) {
                  setState(() {
                    _usePresetFontSizes = value;
                  });
                },
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            '注意：AutoSizeText 必须放在有边界约束的布局里，这里用固定宽度的 SizedBox 来模拟真实卡片。',
            style: theme.textTheme.bodySmall?.copyWith(
              color: Colors.black54,
              height: 1.4,
            ),
          ),
          const SizedBox(height: 16),
          Wrap(
            spacing: 12,
            runSpacing: 12,
            children: <Widget>[
              _DemoPanel(
                title: 'Text',
                subtitle: '直接截断',
                accentColor: const Color(0xFFDC2626),
                child: SizedBox(
                  width: _sampleWidth,
                  child: Text(
                    _headlineText,
                    style: headlineStyle,
                    maxLines: _maxLines,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ),
              _DemoPanel(
                title: 'AutoSizeText',
                subtitle: _usePresetFontSizes ? '按预设字号档位缩放' : '连续缩放直到适配',
                accentColor: const Color(0xFF2563EB),
                child: SizedBox(
                  width: _sampleWidth,
                  child: AutoSizeText(
                    _headlineText,
                    style: headlineStyle,
                    maxLines: _maxLines,
                    minFontSize: 12,
                    stepGranularity: 1,
                    overflow: TextOverflow.ellipsis,
                    presetFontSizes: _usePresetFontSizes
                        ? const <double>[28, 24, 20, 18, 16, 14, 12]
                        : null,
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildGroupSection(BuildContext context) {
    return _SectionCard(
      title: 'AutoSizeGroup',
      subtitle: '多个标题共享同一个 AutoSizeGroup 时，会统一收敛到同一字号，适合仪表盘、商品卡片和宫格布局。',
      child: Wrap(
        spacing: 12,
        runSpacing: 12,
        children: _groupTitles
            .map(
              (String title) =>
                  _GroupShowcaseCard(title: title, group: _headlineGroup),
            )
            .toList(),
      ),
    );
  }

  Widget _buildAdvancedSection(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final TextStyle titleStyle =
        theme.textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w700) ??
        const TextStyle(fontSize: 22, fontWeight: FontWeight.w700);

    return _SectionCard(
      title: '进阶参数',
      subtitle: '当最小字号仍然放不下时，可以用 overflowReplacement 给出更明确的回退 UI，而不是继续挤压阅读体验。',
      child: Wrap(
        spacing: 12,
        runSpacing: 12,
        children: <Widget>[
          _DemoPanel(
            title: 'overflowReplacement',
            subtitle: '极窄空间下直接替换内容',
            accentColor: const Color(0xFF7C3AED),
            child: SizedBox(
              width: 120,
              child: AutoSizeText(
                'Quarterly subscription revenue forecast',
                style: titleStyle,
                maxLines: 1,
                minFontSize: 18,
                overflowReplacement: Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 10,
                  ),
                  decoration: BoxDecoration(
                    color: const Color(0xFFF5F3FF),
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(color: const Color(0xFFD8B4FE)),
                  ),
                  child: const Text(
                    '内容过长，请进入详情页查看',
                    style: TextStyle(
                      color: Color(0xFF6D28D9),
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ),
            ),
          ),
          const _DemoPanel(
            title: 'Rich copy',
            subtitle: '和普通 Text 一样支持 textAlign / maxLines',
            accentColor: Color(0xFF0F766E),
            child: SizedBox(
              width: 220,
              child: AutoSizeText(
                'Build once, adapt everywhere.',
                minFontSize: 14,
                maxLines: 2,
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 34,
                  height: 1.1,
                  fontWeight: FontWeight.w800,
                  color: Color(0xFF0F172A),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  void _updateMaxLines(int value) {
    setState(() {
      _maxLines = value;
    });
  }
}

class _SectionCard extends StatelessWidget {
  const _SectionCard({
    required this.title,
    required this.subtitle,
    required this.child,
  });

  final String title;
  final String subtitle;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: <Color>[Color(0xFFF8FAFC), Color(0xFFF1F5F9)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFE2E8F0)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x120F172A),
            blurRadius: 16,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              title,
              style: theme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              subtitle,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: Colors.black87,
                height: 1.5,
              ),
            ),
            const SizedBox(height: 16),
            child,
          ],
        ),
      ),
    );
  }
}

class _DemoPanel extends StatelessWidget {
  const _DemoPanel({
    required this.title,
    required this.subtitle,
    required this.accentColor,
    required this.child,
  });

  final String title;
  final String subtitle;
  final Color accentColor;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Container(
      width: 280,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: accentColor.withValues(alpha: 0.18)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Row(
            children: <Widget>[
              Container(
                width: 10,
                height: 10,
                decoration: BoxDecoration(
                  color: accentColor,
                  shape: BoxShape.circle,
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  title,
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 6),
          Text(
            subtitle,
            style: theme.textTheme.bodySmall?.copyWith(
              color: Colors.black54,
              height: 1.4,
            ),
          ),
          const SizedBox(height: 18),
          child,
        ],
      ),
    );
  }
}

class _GroupShowcaseCard extends StatelessWidget {
  const _GroupShowcaseCard({required this.title, required this.group});

  final String title;
  final AutoSizeGroup group;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 220,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFD6E4FF)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: const Color(0xFFE0ECFF),
              borderRadius: BorderRadius.circular(14),
            ),
            alignment: Alignment.center,
            child: const Icon(
              Icons.dashboard_customize_outlined,
              color: Color(0xFF2563EB),
            ),
          ),
          const SizedBox(height: 14),
          SizedBox(
            height: 72,
            child: AutoSizeText(
              title,
              group: group,
              maxLines: 2,
              minFontSize: 14,
              style: const TextStyle(
                fontSize: 26,
                fontWeight: FontWeight.w800,
                height: 1.15,
                color: Color(0xFF0F172A),
              ),
            ),
          ),
          const SizedBox(height: 12),
          const Text(
            '共享字号后，多列卡片的视觉节奏会更整齐。',
            style: TextStyle(color: Colors.black54, height: 1.4),
          ),
        ],
      ),
    );
  }
}

class _LineChip extends StatelessWidget {
  const _LineChip({
    required this.label,
    required this.selected,
    required this.onSelected,
  });

  final String label;
  final bool selected;
  final VoidCallback onSelected;

  @override
  Widget build(BuildContext context) {
    return ChoiceChip(
      label: Text(label),
      selected: selected,
      onSelected: (_) => onSelected(),
    );
  }
}

class _TagChip extends StatelessWidget {
  const _TagChip({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFE2E8F0),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Text(
          label,
          style: const TextStyle(
            color: Color(0xFF0F172A),
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }
}
