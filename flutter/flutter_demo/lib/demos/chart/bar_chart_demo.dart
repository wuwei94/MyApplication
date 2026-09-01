import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

/// 柱状图与触摸联动示例
///
/// 演示多维度分组柱状图（目标值 vs 实际值）、正负收支堆叠柱状图与触摸高亮联动面板。
class BarChartDemoPage extends StatefulWidget {
  const BarChartDemoPage({super.key, required this.title});

  final String title;

  @override
  State<BarChartDemoPage> createState() => _BarChartDemoPageState();
}

enum _BarMode {
  grouped, // 多维分组
  stackedProfit, // 正负堆叠收益
}

class _BarChartDemoPageState extends State<BarChartDemoPage> {
  _BarMode _mode = _BarMode.grouped;
  int _touchedGroupIndex = 2; // 默认选中 Q3

  static const List<String> _quarters = <String>['Q1 第一季度', 'Q2 第二季度', 'Q3 第三季度', 'Q4 第四季度'];

  // 分组数据：[目标, 实际] (万元)
  static const List<List<double>> _groupedData = <List<double>>[
    <double>[120.0, 135.5],
    <double>[150.0, 142.0],
    <double>[180.0, 210.8],
    <double>[220.0, 245.0],
  ];

  // 正负收益数据：[主营盈利, 运营亏损/额外支出] (万元)
  static const List<List<double>> _profitData = <List<double>>[
    <double>[65.0, -18.0],
    <double>[82.0, -25.0],
    <double>[110.0, -15.0],
    <double>[135.0, -32.0],
  ];

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final ColorScheme colorScheme = theme.colorScheme;

    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            // 模式切换卡片
            _buildModeSwitchCard(colorScheme),
            const SizedBox(height: 16),

            // 柱状图展示卡片
            _buildChartCard(colorScheme),
            const SizedBox(height: 16),

            // 触摸联动详细面板
            _buildDetailCard(colorScheme),
          ],
        ),
      ),
    );
  }

  Widget _buildModeSwitchCard(ColorScheme colorScheme) {
    return Card(
      elevation: 0,
      color: colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          children: <Widget>[
            Icon(Icons.bar_chart_rounded, color: colorScheme.primary, size: 20),
            const SizedBox(width: 8),
            const Text(
              '图表模式：',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
            ),
            const Spacer(),
            SegmentedButton<_BarMode>(
              segments: const <ButtonSegment<_BarMode>>[
                ButtonSegment<_BarMode>(
                  value: _BarMode.grouped,
                  label: Text('分组对比'),
                  icon: Icon(Icons.compare_arrows_rounded, size: 16),
                ),
                ButtonSegment<_BarMode>(
                  value: _BarMode.stackedProfit,
                  label: Text('正负收支'),
                  icon: Icon(Icons.stacked_bar_chart_rounded, size: 16),
                ),
              ],
              selected: <_BarMode>{_mode},
              onSelectionChanged: (Set<_BarMode> newSelection) {
                setState(() {
                  _mode = newSelection.first;
                });
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildChartCard(ColorScheme colorScheme) {
    const Color targetColor = Colors.indigo;
    const Color actualColor = Colors.teal;
    const Color profitColor = Color(0xFF10B981);
    const Color lossColor = Color(0xFFF43F5E);

    return Card(
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 20, 20, 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: <Widget>[
                Text(
                  _mode == _BarMode.grouped ? '季度目标 vs 实际销售额' : '季度净收支分布',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: colorScheme.onSurface,
                  ),
                ),
                // 图例
                if (_mode == _BarMode.grouped)
                  Row(
                    children: <Widget>[
                      _buildLegend('目标额', targetColor),
                      const SizedBox(width: 12),
                      _buildLegend('实际额', actualColor),
                    ],
                  )
                else
                  Row(
                    children: <Widget>[
                      _buildLegend('盈利', profitColor),
                      const SizedBox(width: 12),
                      _buildLegend('亏损', lossColor),
                    ],
                  ),
              ],
            ),
            const SizedBox(height: 24),
            SizedBox(
              height: 270,
              child: BarChart(
                _mode == _BarMode.grouped
                    ? _createGroupedData(colorScheme, targetColor, actualColor)
                    : _createStackedData(colorScheme, profitColor, lossColor),
                duration: const Duration(milliseconds: 300),
                curve: Curves.easeInOut,
              ),
            ),
          ],
        ),
      ),
    );
  }

  BarChartData _createGroupedData(
    ColorScheme colorScheme,
    Color targetColor,
    Color actualColor,
  ) {
    return BarChartData(
      maxY: 300,
      minY: 0,
      gridData: FlGridData(
        show: true,
        drawVerticalLine: false,
        horizontalInterval: 60,
        getDrawingHorizontalLine: (double val) => FlLine(
          color: colorScheme.outlineVariant.withValues(alpha: 0.4),
          strokeWidth: 1,
          dashArray: const <int>[4, 4],
        ),
      ),
      titlesData: FlTitlesData(
        leftTitles: AxisTitles(
          sideTitles: SideTitles(
            showTitles: true,
            interval: 60,
            reservedSize: 42,
            getTitlesWidget: (double val, TitleMeta meta) => Text(
              '${val.toInt()}w',
              style: TextStyle(
                fontSize: 11,
                color: colorScheme.onSurfaceVariant,
              ),
            ),
          ),
        ),
        bottomTitles: AxisTitles(
          sideTitles: SideTitles(
            showTitles: true,
            reservedSize: 32,
            getTitlesWidget: (double val, TitleMeta meta) {
              final int index = val.toInt();
              if (index < 0 || index >= _quarters.length) {
                return const SizedBox.shrink();
              }
              final bool isSelected = index == _touchedGroupIndex;
              return Padding(
                padding: const EdgeInsets.only(top: 8),
                child: Text(
                  'Q${index + 1}',
                  style: TextStyle(
                    fontSize: isSelected ? 13 : 11,
                    fontWeight: isSelected
                        ? FontWeight.bold
                        : FontWeight.normal,
                    color: isSelected
                        ? colorScheme.primary
                        : colorScheme.onSurfaceVariant,
                  ),
                ),
              );
            },
          ),
        ),
        rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
        topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
      ),
      borderData: FlBorderData(show: false),
      barTouchData: BarTouchData(
        handleBuiltInTouches: true,
        touchCallback: (FlTouchEvent event, BarTouchResponse? res) {
          if (res != null && res.spot != null) {
            final int index = res.spot!.touchedBarGroupIndex;
            if (index >= 0 &&
                index < _quarters.length &&
                index != _touchedGroupIndex) {
              setState(() {
                _touchedGroupIndex = index;
              });
            }
          }
        },
        touchTooltipData: BarTouchTooltipData(
          getTooltipColor: (BarChartGroupData group) =>
              colorScheme.inverseSurface.withValues(alpha: 0.9),
          tooltipBorder: BorderSide(
            color: colorScheme.outlineVariant,
            width: 1,
          ),
          tooltipPadding: const EdgeInsets.symmetric(
            horizontal: 10,
            vertical: 6,
          ),
          getTooltipItem: (
            BarChartGroupData group,
            int groupIndex,
            BarChartRodData rod,
            int rodIndex,
          ) {
            final bool isTarget = rodIndex == 0;
            return BarTooltipItem(
              '${isTarget ? "目标额" : "实际额"}: ${rod.toY.toStringAsFixed(1)} 万元',
              TextStyle(
                color: isTarget ? Colors.lightBlueAccent : Colors.tealAccent,
                fontWeight: FontWeight.bold,
                fontSize: 12,
              ),
            );
          },
        ),
      ),
      barGroups: _groupedData.asMap().entries.map((MapEntry<int, List<double>> entry) {
        final int index = entry.key;
        final List<double> vals = entry.value;
        final bool isSelected = index == _touchedGroupIndex;

        return BarChartGroupData(
          x: index,
          barRods: <BarChartRodData>[
            BarChartRodData(
              toY: vals[0],
              color: targetColor.withValues(
                alpha: isSelected ? 1.0 : 0.7,
              ),
              width: isSelected ? 18 : 14,
              borderRadius: const BorderRadius.vertical(
                top: Radius.circular(6),
              ),
            ),
            BarChartRodData(
              toY: vals[1],
              color: actualColor.withValues(
                alpha: isSelected ? 1.0 : 0.7,
              ),
              width: isSelected ? 18 : 14,
              borderRadius: const BorderRadius.vertical(
                top: Radius.circular(6),
              ),
            ),
          ],
        );
      }).toList(),
    );
  }

  BarChartData _createStackedData(
    ColorScheme colorScheme,
    Color profitColor,
    Color lossColor,
  ) {
    return BarChartData(
      maxY: 160,
      minY: -50,
      gridData: FlGridData(
        show: true,
        drawVerticalLine: false,
        horizontalInterval: 40,
        getDrawingHorizontalLine: (double val) => FlLine(
          color: val == 0
              ? colorScheme.outline
              : colorScheme.outlineVariant.withValues(alpha: 0.3),
          strokeWidth: val == 0 ? 1.5 : 1,
          dashArray: val == 0 ? null : const <int>[4, 4],
        ),
      ),
      titlesData: FlTitlesData(
        leftTitles: AxisTitles(
          sideTitles: SideTitles(
            showTitles: true,
            interval: 40,
            reservedSize: 42,
            getTitlesWidget: (double val, TitleMeta meta) => Text(
              '${val.toInt()}w',
              style: TextStyle(
                fontSize: 11,
                color: colorScheme.onSurfaceVariant,
              ),
            ),
          ),
        ),
        bottomTitles: AxisTitles(
          sideTitles: SideTitles(
            showTitles: true,
            reservedSize: 32,
            getTitlesWidget: (double val, TitleMeta meta) {
              final int index = val.toInt();
              if (index < 0 || index >= _quarters.length) {
                return const SizedBox.shrink();
              }
              final bool isSelected = index == _touchedGroupIndex;
              return Padding(
                padding: const EdgeInsets.only(top: 8),
                child: Text(
                  'Q${index + 1}',
                  style: TextStyle(
                    fontSize: isSelected ? 13 : 11,
                    fontWeight: isSelected
                        ? FontWeight.bold
                        : FontWeight.normal,
                    color: isSelected
                        ? colorScheme.primary
                        : colorScheme.onSurfaceVariant,
                  ),
                ),
              );
            },
          ),
        ),
        rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
        topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
      ),
      borderData: FlBorderData(show: false),
      barTouchData: BarTouchData(
        handleBuiltInTouches: true,
        touchCallback: (FlTouchEvent event, BarTouchResponse? res) {
          if (res != null && res.spot != null) {
            final int index = res.spot!.touchedBarGroupIndex;
            if (index >= 0 &&
                index < _quarters.length &&
                index != _touchedGroupIndex) {
              setState(() {
                _touchedGroupIndex = index;
              });
            }
          }
        },
        touchTooltipData: BarTouchTooltipData(
          getTooltipColor: (BarChartGroupData group) =>
              colorScheme.inverseSurface.withValues(alpha: 0.9),
          getTooltipItem: (
            BarChartGroupData group,
            int groupIndex,
            BarChartRodData rod,
            int rodIndex,
          ) {
            final double profit = _profitData[groupIndex][0];
            final double loss = _profitData[groupIndex][1];
            final double net = profit + loss;
            return BarTooltipItem(
              '${_quarters[groupIndex]}\n盈利: +$profit 万\n亏损: $loss 万\n净收益: ${net > 0 ? "+" : ""}${net.toStringAsFixed(1)} 万',
              const TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.bold,
                fontSize: 11,
              ),
            );
          },
        ),
      ),
      barGroups: _profitData.asMap().entries.map((MapEntry<int, List<double>> entry) {
        final int index = entry.key;
        final List<double> vals = entry.value;
        final bool isSelected = index == _touchedGroupIndex;

        return BarChartGroupData(
          x: index,
          barRods: <BarChartRodData>[
            BarChartRodData(
              toY: vals[0],
              color: profitColor.withValues(
                alpha: isSelected ? 1.0 : 0.75,
              ),
              width: isSelected ? 24 : 18,
              borderRadius: const BorderRadius.vertical(
                top: Radius.circular(6),
              ),
            ),
            BarChartRodData(
              toY: vals[1],
              color: lossColor.withValues(
                alpha: isSelected ? 1.0 : 0.75,
              ),
              width: isSelected ? 24 : 18,
              borderRadius: const BorderRadius.vertical(
                bottom: Radius.circular(6),
              ),
            ),
          ],
        );
      }).toList(),
    );
  }

  Widget _buildDetailCard(ColorScheme colorScheme) {
    if (_mode == _BarMode.grouped) {
      final double target = _groupedData[_touchedGroupIndex][0];
      final double actual = _groupedData[_touchedGroupIndex][1];
      final double diff = actual - target;
      final double achievementRate = (actual / target) * 100;

      return Card(
        elevation: 0,
        color: colorScheme.secondaryContainer.withValues(alpha: 0.4),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: BorderSide(
            color: colorScheme.secondary.withValues(alpha: 0.2),
          ),
        ),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: <Widget>[
                  Text(
                    '${_quarters[_touchedGroupIndex]} 业绩达成分析',
                    style: TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.bold,
                      color: colorScheme.onSecondaryContainer,
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: achievementRate >= 100
                          ? Colors.green
                          : Colors.orange,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      achievementRate >= 100 ? '超额完成' : '未达预期',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 11,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: <Widget>[
                  Expanded(
                    child: _buildTile(
                      '目标额',
                      '${target.toStringAsFixed(1)} 万',
                      Colors.indigo,
                    ),
                  ),
                  Expanded(
                    child: _buildTile(
                      '实际额',
                      '${actual.toStringAsFixed(1)} 万',
                      Colors.teal,
                    ),
                  ),
                  Expanded(
                    child: _buildTile(
                      '差额',
                      '${diff > 0 ? "+" : ""}${diff.toStringAsFixed(1)} 万',
                      diff >= 0 ? Colors.green : Colors.red,
                    ),
                  ),
                  Expanded(
                    child: _buildTile(
                      '达成率',
                      '${achievementRate.toStringAsFixed(1)}%',
                      colorScheme.primary,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      );
    } else {
      final double profit = _profitData[_touchedGroupIndex][0];
      final double loss = _profitData[_touchedGroupIndex][1];
      final double net = profit + loss;

      return Card(
        elevation: 0,
        color: colorScheme.tertiaryContainer.withValues(alpha: 0.4),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: BorderSide(color: colorScheme.tertiary.withValues(alpha: 0.2)),
        ),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Text(
                '${_quarters[_touchedGroupIndex]} 收支拆解',
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.bold,
                  color: colorScheme.onTertiaryContainer,
                ),
              ),
              const SizedBox(height: 12),
              Row(
                children: <Widget>[
                  Expanded(
                    child: _buildTile(
                      '总收益',
                      '+${profit.toStringAsFixed(1)} 万',
                      Colors.green,
                    ),
                  ),
                  Expanded(
                    child: _buildTile(
                      '总支出/亏损',
                      '${loss.toStringAsFixed(1)} 万',
                      Colors.redAccent,
                    ),
                  ),
                  Expanded(
                    child: _buildTile(
                      '实际净盈余',
                      '${net > 0 ? "+" : ""}${net.toStringAsFixed(1)} 万',
                      colorScheme.primary,
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

  Widget _buildTile(String title, String val, Color color) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          title,
          style: const TextStyle(fontSize: 11, color: Colors.black54),
        ),
        const SizedBox(height: 4),
        Text(
          val,
          style: TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
      ],
    );
  }

  Widget _buildLegend(String label, Color color) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        Container(
          width: 10,
          height: 10,
          decoration: BoxDecoration(
            color: color,
            borderRadius: BorderRadius.circular(2),
          ),
        ),
        const SizedBox(width: 4),
        Text(
          label,
          style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w500),
        ),
      ],
    );
  }
}
