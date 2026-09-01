import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

/// 动态环形饼图与扇区触摸放大联动示例
///
/// 演示环形甜甜圈图与实心饼图切换、扇区触摸动画凸起放大、中心动态数据展示与双向图例联动。
class PieChartDemoPage extends StatefulWidget {
  const PieChartDemoPage({super.key, required this.title});

  final String title;

  @override
  State<PieChartDemoPage> createState() => _PieChartDemoPageState();
}

class _CategoryItem {
  const _CategoryItem({
    required this.name,
    required this.amount,
    required this.color,
    required this.icon,
  });

  final String name;
  final double amount; // 金额（万元）
  final Color color;
  final IconData icon;
}

class _PieChartDemoPageState extends State<PieChartDemoPage> {
  int _touchedIndex = 0; // 默认选中第一项
  bool _isDonut = true; // 甜甜圈环形 vs 实心饼图

  static const List<_CategoryItem> _categories = <_CategoryItem>[
    _CategoryItem(
      name: '云计算与研发',
      amount: 45.8,
      color: Color(0xFF3B82F6), // Blue
      icon: Icons.cloud_queue_rounded,
    ),
    _CategoryItem(
      name: '市场营销推广',
      amount: 32.5,
      color: Color(0xFF10B981), // Emerald
      icon: Icons.campaign_rounded,
    ),
    _CategoryItem(
      name: '人力与薪酬',
      amount: 68.2,
      color: Color(0xFFF59E0B), // Amber
      icon: Icons.people_outline_rounded,
    ),
    _CategoryItem(
      name: '办公与行政',
      amount: 18.0,
      color: Color(0xFFEC4899), // Pink
      icon: Icons.business_rounded,
    ),
    _CategoryItem(
      name: '储备流动资金',
      amount: 25.5,
      color: Color(0xFF8B5CF6), // Purple
      icon: Icons.savings_outlined,
    ),
  ];

  double get _totalAmount =>
      _categories.fold<double>(0.0, (double sum, _CategoryItem item) => sum + item.amount);

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final ColorScheme colorScheme = theme.colorScheme;

    final _CategoryItem touchedCategory = _categories[_touchedIndex];
    final double touchedPercentage =
        (touchedCategory.amount / _totalAmount) * 100;

    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            // 控制卡片
            _buildControlCard(colorScheme),
            const SizedBox(height: 16),

            // 饼图核心卡片
            _buildPieCard(
              colorScheme,
              touchedCategory,
              touchedPercentage,
            ),
            const SizedBox(height: 16),

            // 图例与明细列表卡片（可点击图例双向联动）
            _buildLegendCard(colorScheme),
          ],
        ),
      ),
    );
  }

  Widget _buildControlCard(ColorScheme colorScheme) {
    return Card(
      elevation: 0,
      color: colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        child: Row(
          children: <Widget>[
            Icon(Icons.pie_chart_rounded, color: colorScheme.primary, size: 20),
            const SizedBox(width: 8),
            const Text(
              '图表样式：',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
            ),
            const Spacer(),
            SegmentedButton<bool>(
              segments: const <ButtonSegment<bool>>[
                ButtonSegment<bool>(
                  value: true,
                  label: Text('甜甜圈环形'),
                  icon: Icon(Icons.donut_large_rounded, size: 16),
                ),
                ButtonSegment<bool>(
                  value: false,
                  label: Text('实心饼图'),
                  icon: Icon(Icons.pie_chart_outline_rounded, size: 16),
                ),
              ],
              selected: <bool>{_isDonut},
              onSelectionChanged: (Set<bool> val) {
                setState(() {
                  _isDonut = val.first;
                });
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPieCard(
    ColorScheme colorScheme,
    _CategoryItem touchedCategory,
    double touchedPercentage,
  ) {
    return Card(
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: <Widget>[
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: <Widget>[
                Text(
                  '年度各项成本预算占比',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: colorScheme.onSurface,
                  ),
                ),
                Text(
                  '总额: ${_totalAmount.toStringAsFixed(1)} 万',
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                    color: colorScheme.primary,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),

            // 饼图与中心信息层叠
            SizedBox(
              height: 240,
              child: Stack(
                alignment: Alignment.center,
                children: <Widget>[
                  PieChart(
                    PieChartData(
                      pieTouchData: PieTouchData(
                        touchCallback:
                            (FlTouchEvent event, PieTouchResponse? res) {
                          if (res != null &&
                              res.touchedSection != null &&
                              event is! FlPointerExitEvent) {
                            final int index =
                                res.touchedSection!.touchedSectionIndex;
                            if (index >= 0 &&
                                index < _categories.length &&
                                index != _touchedIndex) {
                              setState(() {
                                _touchedIndex = index;
                              });
                            }
                          }
                        },
                      ),
                      startDegreeOffset: 180,
                      borderData: FlBorderData(show: false),
                      sectionsSpace: 3,
                      centerSpaceRadius: _isDonut ? 65 : 0,
                      sections: _buildPieSections(),
                    ),
                    duration: const Duration(milliseconds: 300),
                    curve: Curves.easeInOut,
                  ),

                  // 仅在环形图时在中心展示当前选中分类与占比
                  if (_isDonut)
                    Column(
                      mainAxisSize: MainAxisSize.min,
                      children: <Widget>[
                        Icon(
                          touchedCategory.icon,
                          color: touchedCategory.color,
                          size: 24,
                        ),
                        const SizedBox(height: 4),
                        Text(
                          '${touchedPercentage.toStringAsFixed(1)}%',
                          style: TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                            color: colorScheme.onSurface,
                          ),
                        ),
                        Text(
                          touchedCategory.name,
                          style: TextStyle(
                            fontSize: 11,
                            color: colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ],
                    ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  List<PieChartSectionData> _buildPieSections() {
    return _categories.asMap().entries.map((MapEntry<int, _CategoryItem> entry) {
      final int index = entry.key;
      final _CategoryItem item = entry.value;
      final bool isTouched = index == _touchedIndex;
      final double percentage = (item.amount / _totalAmount) * 100;

      final double radius = isTouched ? 65.0 : 50.0;
      final double fontSize = isTouched ? 14.0 : 11.0;

      return PieChartSectionData(
        color: item.color,
        value: item.amount,
        title: _isDonut
            ? (isTouched ? '${percentage.toStringAsFixed(1)}%' : '')
            : '${percentage.toStringAsFixed(0)}%',
        radius: radius,
        titleStyle: TextStyle(
          fontSize: fontSize,
          fontWeight: FontWeight.bold,
          color: Colors.white,
          shadows: const <Shadow>[
            Shadow(color: Colors.black38, blurRadius: 2),
          ],
        ),
        badgeWidget: isTouched
            ? _buildBadge(item.icon, item.color)
            : null,
        badgePositionPercentageOffset: 0.98,
      );
    }).toList();
  }

  Widget _buildBadge(IconData icon, Color color) {
    return AnimatedContainer(
      duration: const Duration(milliseconds: 250),
      width: 32,
      height: 32,
      decoration: BoxDecoration(
        color: Colors.white,
        shape: BoxShape.circle,
        boxShadow: <BoxShadow>[
          BoxShadow(
            color: color.withValues(alpha: 0.4),
            blurRadius: 6,
            offset: const Offset(0, 2),
          ),
        ],
        border: Border.all(color: color, width: 2),
      ),
      child: Center(
        child: Icon(icon, size: 16, color: color),
      ),
    );
  }

  Widget _buildLegendCard(ColorScheme colorScheme) {
    return Card(
      elevation: 0,
      color: colorScheme.surfaceContainerLow,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(color: colorScheme.outlineVariant.withValues(alpha: 0.5)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              children: <Widget>[
                Icon(Icons.touch_app_rounded, size: 16, color: colorScheme.primary),
                const SizedBox(width: 6),
                Text(
                  '点击图例可快速联动高亮对应扇区',
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.bold,
                    color: colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            ..._categories.asMap().entries.map((MapEntry<int, _CategoryItem> entry) {
              final int index = entry.key;
              final _CategoryItem item = entry.value;
              final bool isSelected = index == _touchedIndex;
              final double percentage = (item.amount / _totalAmount) * 100;

              return InkWell(
                onTap: () {
                  setState(() {
                    _touchedIndex = index;
                  });
                },
                borderRadius: BorderRadius.circular(10),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                  margin: const EdgeInsets.only(bottom: 6),
                  decoration: BoxDecoration(
                    color: isSelected
                        ? item.color.withValues(alpha: 0.12)
                        : Colors.transparent,
                    borderRadius: BorderRadius.circular(10),
                    border: isSelected
                        ? Border.all(color: item.color.withValues(alpha: 0.4))
                        : null,
                  ),
                  child: Row(
                    children: <Widget>[
                      Container(
                        width: 12,
                        height: 12,
                        decoration: BoxDecoration(
                          color: item.color,
                          shape: BoxShape.circle,
                        ),
                      ),
                      const SizedBox(width: 10),
                      Icon(item.icon, size: 16, color: item.color),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          item.name,
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                            color: colorScheme.onSurface,
                          ),
                        ),
                      ),
                      Text(
                        '${percentage.toStringAsFixed(1)}%',
                        style: TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w600,
                          color: isSelected ? item.color : colorScheme.onSurfaceVariant,
                        ),
                      ),
                      const SizedBox(width: 12),
                      Text(
                        '${item.amount.toStringAsFixed(1)} 万',
                        style: TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.bold,
                          color: colorScheme.onSurface,
                        ),
                      ),
                    ],
                  ),
                ),
              );
            }),
          ],
        ),
      ),
    );
  }
}
