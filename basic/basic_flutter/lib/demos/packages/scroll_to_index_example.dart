import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:scroll_to_index/scroll_to_index.dart';

/// scroll_to_index
/// https://pub.dev/packages/scroll_to_index
class ScrollToIndexDemoPage extends StatelessWidget {
  const ScrollToIndexDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ScrollToIndexDemoView(title: title);
  }
}

class ScrollToIndexDemoView extends StatefulWidget {
  const ScrollToIndexDemoView({super.key, required this.title});

  final String title;

  @override
  State<ScrollToIndexDemoView> createState() => _ScrollToIndexDemoViewState();
}

class _ScrollToIndexDemoViewState extends State<ScrollToIndexDemoView> {
  static const int _itemCount = 24;
  static const List<int> _quickJumpIndexes = <int>[0, 3, 7, 12, 18, 23];
  static const List<AutoScrollPosition> _scrollPositions = <AutoScrollPosition>[
    AutoScrollPosition.begin,
    AutoScrollPosition.middle,
    AutoScrollPosition.end,
  ];
  static const List<String> _sectionTitles = <String>[
    '目录导航',
    '章节锚点',
    '商品楼层',
    'FAQ 定位',
    '搜索命中',
    '长表单校验',
    '消息回看',
    '配置面板',
  ];
  static const List<String> _sectionSummaries = <String>[
    '适合文档型页面，通过目录项把对应内容区块滚动到可视区域。',
    '点击顶部目录后快速定位到正文小节，减少重复滑动。',
    '电商首页常见的楼层跳转场景，列表高度不固定时也能稳定定位。',
    '从问题清单直达答案正文，适合帮助中心和文档 FAQ 页面。',
    '在结果列表中定位命中项，并用高亮提示用户当前关注的位置。',
    '提交失败后跳转到第一个错误字段，长表单的体验会更自然。',
    '在长消息流里回到某个时间点、楼层或被引用的位置。',
    '设置项很多时，从概览区直接跳到目标模块，减少查找成本。',
  ];
  static const List<IconData> _sectionIcons = <IconData>[
    Icons.menu_book_rounded,
    Icons.bookmarks_rounded,
    Icons.storefront_rounded,
    Icons.live_help_rounded,
    Icons.search_rounded,
    Icons.assignment_turned_in_rounded,
    Icons.forum_rounded,
    Icons.tune_rounded,
  ];
  static const List<Color> _accentColors = <Color>[
    Color(0xFF2563EB),
    Color(0xFF7C3AED),
    Color(0xFFEA580C),
    Color(0xFF0891B2),
    Color(0xFF15803D),
    Color(0xFFDC2626),
    Color(0xFFD97706),
    Color(0xFF4F46E5),
  ];
  static const List<Color> _surfaceColors = <Color>[
    Color(0xFFEFF6FF),
    Color(0xFFF5F3FF),
    Color(0xFFFFF7ED),
    Color(0xFFECFEFF),
    Color(0xFFF0FDF4),
    Color(0xFFFEF2F2),
    Color(0xFFFFFBEB),
    Color(0xFFEEF2FF),
  ];

  late final AutoScrollController _controller;
  late final TextEditingController _indexController;
  late final List<_ScrollSection> _sections;

  AutoScrollPosition _preferPosition = AutoScrollPosition.begin;
  int _currentIndex = 7;
  String _statusMessage = '输入索引或点击快捷按钮，体验定点滚动和高亮反馈。';

  @override
  void initState() {
    super.initState();
    _sections = _buildSections();
    _controller = AutoScrollController(suggestedRowHeight: 196);
    _indexController = TextEditingController(text: '7');
  }

  @override
  void dispose() {
    _indexController.dispose();
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    return Column(
      children: <Widget>[
        _buildOverviewCard(context),
        Expanded(
          child: Scrollbar(
            controller: _controller,
            child: ListView.separated(
              controller: _controller,
              padding: const EdgeInsets.fromLTRB(16, 4, 16, 24),
              itemCount: _sections.length,
              separatorBuilder: (BuildContext context, int index) {
                return const SizedBox(height: 12);
              },
              itemBuilder: (BuildContext context, int index) {
                final _ScrollSection section = _sections[index];
                return AutoScrollTag(
                  key: ValueKey<int>(index),
                  controller: _controller,
                  index: index,
                  highlightColor: section.highlightColor,
                  child: _ScrollSectionCard(
                    index: index,
                    isCurrent: index == _currentIndex,
                    section: section,
                  ),
                );
              },
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildOverviewCard(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 12),
      child: DecoratedBox(
        decoration: BoxDecoration(
          color: const Color(0xFFF8FAFC),
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: const Color(0xFFDCE3F0)),
          boxShadow: const <BoxShadow>[
            BoxShadow(
              color: Color(0x120F172A),
              blurRadius: 20,
              offset: Offset(0, 10),
            ),
          ],
        ),
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  DecoratedBox(
                    decoration: BoxDecoration(
                      color: const Color(0xFF1D4ED8),
                      borderRadius: BorderRadius.circular(16),
                    ),
                    child: const Padding(
                      padding: EdgeInsets.all(12),
                      child: Icon(
                        Icons.my_location_rounded,
                        color: Colors.white,
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: <Widget>[
                        Text(
                          'scroll_to_index 3.0.1',
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          '通过 AutoScrollController 和 AutoScrollTag，给任意滚动列表补上按索引定位能力。',
                          style: theme.textTheme.bodyMedium?.copyWith(
                            color: Colors.black54,
                            height: 1.45,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              Wrap(
                spacing: 10,
                runSpacing: 10,
                children: <Widget>[
                  _MetricChip(label: '列表项', value: '${_sections.length}'),
                  _MetricChip(label: '当前定位', value: '#${_currentIndex + 1}'),
                  _MetricChip(
                    label: '对齐方式',
                    value: _positionLabel(_preferPosition),
                  ),
                ],
              ),
              const SizedBox(height: 14),
              Text(
                _statusMessage,
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: const Color(0xFF1E3A8A),
                  height: 1.4,
                ),
              ),
              const SizedBox(height: 16),
              Row(
                children: <Widget>[
                  Expanded(
                    child: TextField(
                      controller: _indexController,
                      keyboardType: TextInputType.number,
                      inputFormatters: <TextInputFormatter>[
                        FilteringTextInputFormatter.digitsOnly,
                      ],
                      decoration: const InputDecoration(
                        labelText: '目标索引',
                        hintText: '例如 7',
                        border: OutlineInputBorder(),
                        isDense: true,
                        helperText: '从 0 开始，越界会自动夹到首尾。',
                      ),
                      onSubmitted: (String value) {
                        _handleJumpPressed();
                      },
                    ),
                  ),
                  const SizedBox(width: 12),
                  FilledButton.icon(
                    onPressed: _handleJumpPressed,
                    icon: const Icon(Icons.arrow_downward_rounded),
                    label: const Text('跳转'),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: <Widget>[
                  for (final int index in _quickJumpIndexes)
                    ActionChip(
                      label: Text('跳到 #${index + 1}'),
                      onPressed: () {
                        _handleQuickJump(index);
                      },
                    ),
                ],
              ),
              const SizedBox(height: 12),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: <Widget>[
                  for (final AutoScrollPosition position in _scrollPositions)
                    ChoiceChip(
                      label: Text(_positionLabel(position)),
                      selected: _preferPosition == position,
                      onSelected: (bool selected) {
                        if (!selected) {
                          return;
                        }

                        setState(() {
                          _preferPosition = position;
                          _statusMessage =
                              '对齐方式已切换为 ${_positionLabel(position)}。';
                        });
                      },
                    ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _handleJumpPressed() {
    final int? parsedIndex = int.tryParse(_indexController.text);
    if (parsedIndex == null) {
      setState(() {
        _statusMessage = '请输入有效数字，例如 0、7 或 12。';
      });
      return;
    }

    _handleQuickJump(parsedIndex);
  }

  void _handleQuickJump(int index) {
    unawaited(_scrollToIndex(index));
  }

  Future<void> _scrollToIndex(int rawIndex) async {
    final int targetIndex = rawIndex.clamp(0, _sections.length - 1);
    FocusManager.instance.primaryFocus?.unfocus();
    _controller.cancelAllHighlights();

    setState(() {
      _statusMessage = '正在定位到第 ${targetIndex + 1} 项...';
    });

    await _controller.scrollToIndex(
      targetIndex,
      preferPosition: _preferPosition,
      duration: const Duration(milliseconds: 450),
    );
    await _controller.highlight(targetIndex);

    if (!mounted) {
      return;
    }

    setState(() {
      _currentIndex = targetIndex;
      _indexController.text = '$targetIndex';
      _statusMessage = '已定位到第 ${targetIndex + 1} 项，卡片会短暂高亮方便确认目标位置。';
    });
  }

  List<_ScrollSection> _buildSections() {
    return List<_ScrollSection>.generate(_itemCount, (int index) {
      final int templateIndex = index % _sectionTitles.length;
      final int bulletCount = 2 + index % 4;

      return _ScrollSection(
        title: 'Section ${index + 1} · ${_sectionTitles[templateIndex]}',
        summary: _sectionSummaries[templateIndex],
        accentColor: _accentColors[templateIndex],
        surfaceColor: _surfaceColors[templateIndex],
        highlightColor: _surfaceColors[templateIndex],
        icon: _sectionIcons[templateIndex],
        bullets: List<String>.generate(bulletCount, (int bulletIndex) {
          return '要点 ${bulletIndex + 1}：第 ${index + 1} 项演示 '
              '${_sectionTitles[templateIndex]} 场景下的定点滚动反馈。';
        }),
      );
    });
  }

  String _positionLabel(AutoScrollPosition position) {
    switch (position) {
      case AutoScrollPosition.begin:
        return '顶部对齐';
      case AutoScrollPosition.middle:
        return '居中对齐';
      case AutoScrollPosition.end:
        return '底部对齐';
    }
  }
}

class _ScrollSectionCard extends StatelessWidget {
  const _ScrollSectionCard({
    required this.index,
    required this.isCurrent,
    required this.section,
  });

  final int index;
  final bool isCurrent;
  final _ScrollSection section;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: section.surfaceColor,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: isCurrent ? section.accentColor : const Color(0xFFD8E2EC),
          width: isCurrent ? 2 : 1,
        ),
      ),
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                DecoratedBox(
                  decoration: BoxDecoration(
                    color: section.accentColor,
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.all(10),
                    child: Icon(section.icon, color: Colors.white),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Text(
                        section.title,
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        'Index: $index',
                        style: theme.textTheme.labelLarge?.copyWith(
                          color: section.accentColor,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 14),
            Text(
              section.summary,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: Colors.black87,
                height: 1.45,
              ),
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: <Widget>[
                for (final String bullet in section.bullets)
                  DecoratedBox(
                    decoration: BoxDecoration(
                      color: Colors.white.withValues(alpha: 0.78),
                      borderRadius: BorderRadius.circular(999),
                      border: Border.all(
                        color: section.accentColor.withValues(alpha: 0.24),
                      ),
                    ),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 12,
                        vertical: 8,
                      ),
                      child: Text(
                        bullet,
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: Colors.black87,
                          height: 1.35,
                        ),
                      ),
                    ),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _MetricChip extends StatelessWidget {
  const _MetricChip({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: const Color(0xFFDCE3F0)),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: RichText(
          text: TextSpan(
            style: theme.textTheme.bodySmall?.copyWith(color: Colors.black87),
            children: <InlineSpan>[
              TextSpan(text: '$label: '),
              TextSpan(
                text: value,
                style: const TextStyle(fontWeight: FontWeight.w700),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ScrollSection {
  const _ScrollSection({
    required this.title,
    required this.summary,
    required this.accentColor,
    required this.surfaceColor,
    required this.highlightColor,
    required this.icon,
    required this.bullets,
  });

  final String title;
  final String summary;
  final Color accentColor;
  final Color surfaceColor;
  final Color highlightColor;
  final IconData icon;
  final List<String> bullets;
}
