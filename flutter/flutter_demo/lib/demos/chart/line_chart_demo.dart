import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

/// 折线图与触摸 Tooltip 联动示例
///
/// 演示多曲线趋势对比（收入 vs 支出）、面积渐变填充、平滑曲线切换与触摸十字线联动指标卡。
class LineChartDemoPage extends StatefulWidget {
  const LineChartDemoPage({super.key, required this.title});

  final String title;

  @override
  State<LineChartDemoPage> createState() => _LineChartDemoPageState();
}

class _LineChartDemoPageState extends State<LineChartDemoPage> {
  bool _isCurved = true;
  bool _showDots = true;
  bool _showFill = true;
  int _selectedMonthIndex = 5; // 默认 6 月

  static const List<String> _months = <String>[
    '1月',
    '2月',
    '3月',
    '4月',
    '5月',
    '6月',
    '7月',
    '8月',
    '9月',
    '10月',
    '11月',
    '12月',
  ];

  static const List<double> _incomeData = <double>[
    28.5,
    35.2,
    31.0,
    42.8,
    48.6,
    55.0,
    51.2,
    63.4,
    59.8,
    72.0,
    68.5,
    84.2,
  ];

  static const List<double> _expenseData = <double>[
    18.0,
    22.4,
    26.8,
    28.0,
    34.5,
    38.2,
    35.0,
    41.5,
    39.0,
    46.2,
    44.0,
    52.6,
  ];

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final ColorScheme colorScheme = theme.colorScheme;

    final double currentIncome = _incomeData[_selectedMonthIndex];
    final double currentExpense = _expenseData[_selectedMonthIndex];
    final double currentProfit = currentIncome - currentExpense;
    final double profitMargin = (currentProfit / currentIncome) * 100;

    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            // 顶部说明与控制开关
            _buildControlCard(colorScheme),
            const SizedBox(height: 16),

            // 折线图主卡片
            _buildChartCard(colorScheme),
            const SizedBox(height: 16),

            // 触摸联动详细指标卡
            _buildDetailMetricsCard(
              colorScheme,
              currentIncome,
              currentExpense,
              currentProfit,
              profitMargin,
            ),
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
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              children: <Widget>[
                Icon(Icons.tune_rounded, size: 18, color: colorScheme.primary),
                const SizedBox(width: 8),
                Text(
                  '图表参数控制与交互',
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    color: colorScheme.onSurface,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Wrap(
              spacing: 8,
              runSpacing: 6,
              children: <Widget>[
                FilterChip(
                  label: const Text('平滑贝塞尔曲线'),
                  selected: _isCurved,
                  onSelected: (bool val) => setState(() => _isCurved = val),
                ),
                FilterChip(
                  label: const Text('显示数据圆点'),
                  selected: _showDots,
                  onSelected: (bool val) => setState(() => _showDots = val),
                ),
                FilterChip(
                  label: const Text('面积渐变填充'),
                  selected: _showFill,
                  onSelected: (bool val) => setState(() => _showFill = val),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildChartCard(ColorScheme colorScheme) {
    const Color incomeColor = Colors.teal;
    const Color expenseColor = Colors.deepOrangeAccent;

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
                  '2026 年度收支趋势图 (万元)',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: colorScheme.onSurface,
                  ),
                ),
                // 图例
                Row(
                  children: <Widget>[
                    _buildLegendItem('收入', incomeColor),
                    const SizedBox(width: 12),
                    _buildLegendItem('支出', expenseColor),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 24),
            SizedBox(
              height: 260,
              child: LineChart(
                LineChartData(
                  minX: 0,
                  maxX: 11,
                  minY: 0,
                  maxY: 100,
                  gridData: FlGridData(
                    show: true,
                    drawVerticalLine: false,
                    horizontalInterval: 20,
                    getDrawingHorizontalLine: (double value) {
                      return FlLine(
                        color: colorScheme.outlineVariant.withValues(
                          alpha: 0.4,
                        ),
                        strokeWidth: 1,
                        dashArray: const <int>[4, 4],
                      );
                    },
                  ),
                  titlesData: FlTitlesData(
                    leftTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        interval: 20,
                        reservedSize: 36,
                        getTitlesWidget: (double value, TitleMeta meta) {
                          return Text(
                            '${value.toInt()}w',
                            style: TextStyle(
                              fontSize: 11,
                              color: colorScheme.onSurfaceVariant,
                            ),
                          );
                        },
                      ),
                    ),
                    bottomTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        interval: 1,
                        reservedSize: 28,
                        getTitlesWidget: (double value, TitleMeta meta) {
                          final int index = value.toInt();
                          if (index < 0 || index >= _months.length) {
                            return const SizedBox.shrink();
                          }
                          final bool isSelected = index == _selectedMonthIndex;
                          return Padding(
                            padding: const EdgeInsets.only(top: 6),
                            child: Text(
                              _months[index],
                              style: TextStyle(
                                fontSize: isSelected ? 12 : 10,
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
                    rightTitles: const AxisTitles(
                      sideTitles: SideTitles(showTitles: false),
                    ),
                    topTitles: const AxisTitles(
                      sideTitles: SideTitles(showTitles: false),
                    ),
                  ),
                  borderData: FlBorderData(show: false),
                  lineTouchData: LineTouchData(
                    handleBuiltInTouches: true,
                    touchCallback: (
                      FlTouchEvent event,
                      LineTouchResponse? touchResponse,
                    ) {
                      if (touchResponse != null &&
                          touchResponse.lineBarSpots != null &&
                          touchResponse.lineBarSpots!.isNotEmpty) {
                        final int index =
                            touchResponse.lineBarSpots!.first.x.toInt();
                        if (index >= 0 &&
                            index < _months.length &&
                            index != _selectedMonthIndex) {
                          setState(() {
                            _selectedMonthIndex = index;
                          });
                        }
                      }
                    },
                    touchTooltipData: LineTouchTooltipData(
                      getTooltipColor: (LineBarSpot spot) =>
                          colorScheme.inverseSurface.withValues(alpha: 0.9),
                      tooltipBorder: BorderSide(
                        color: colorScheme.outlineVariant,
                        width: 1,
                      ),
                      tooltipPadding: const EdgeInsets.symmetric(
                        horizontal: 10,
                        vertical: 6,
                      ),
                      getTooltipItems: (List<LineBarSpot> touchedSpots) {
                        return touchedSpots.map((LineBarSpot spot) {
                          final bool isIncome = spot.barIndex == 0;
                          return LineTooltipItem(
                            '${isIncome ? "收入" : "支出"}: ${spot.y.toStringAsFixed(1)} 万元\n',
                            TextStyle(
                              color: isIncome
                                  ? Colors.tealAccent
                                  : Colors.orangeAccent,
                              fontWeight: FontWeight.bold,
                              fontSize: 12,
                            ),
                            children: <TextSpan>[
                              TextSpan(
                                text: '${_months[spot.x.toInt()]} 数据',
                                style: const TextStyle(
                                  color: Colors.white70,
                                  fontSize: 10,
                                  fontWeight: FontWeight.normal,
                                ),
                              ),
                            ],
                          );
                        }).toList();
                      },
                    ),
                  ),
                  lineBarsData: <LineChartBarData>[
                    // 收入曲线
                    _buildLineSeries(
                      data: _incomeData,
                      color: incomeColor,
                      gradientColors: <Color>[
                        incomeColor,
                        incomeColor.withValues(alpha: 0.3),
                      ],
                    ),
                    // 支出曲线
                    _buildLineSeries(
                      data: _expenseData,
                      color: expenseColor,
                      gradientColors: <Color>[
                        expenseColor,
                        expenseColor.withValues(alpha: 0.3),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  LineChartBarData _buildLineSeries({
    required List<double> data,
    required Color color,
    required List<Color> gradientColors,
  }) {
    return LineChartBarData(
      spots: data.asMap().entries.map((MapEntry<int, double> entry) {
        return FlSpot(entry.key.toDouble(), entry.value);
      }).toList(),
      isCurved: _isCurved,
      curveSmoothness: 0.35,
      color: color,
      barWidth: 3,
      isStrokeCapRound: true,
      dotData: FlDotData(
        show: _showDots,
        getDotPainter: (FlSpot spot, double percent, LineChartBarData bar, int index) {
          final bool isSelected = index == _selectedMonthIndex;
          return FlDotCirclePainter(
            radius: isSelected ? 6 : 3.5,
            color: isSelected ? Colors.white : color,
            strokeWidth: isSelected ? 3 : 1.5,
            strokeColor: color,
          );
        },
      ),
      belowBarData: BarAreaData(
        show: _showFill,
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: <Color>[
            gradientColors[0].withValues(alpha: 0.35),
            gradientColors[1].withValues(alpha: 0.02),
          ],
        ),
      ),
    );
  }

  Widget _buildDetailMetricsCard(
    ColorScheme colorScheme,
    double income,
    double expense,
    double profit,
    double margin,
  ) {
    return Card(
      elevation: 0,
      color: colorScheme.primaryContainer.withValues(alpha: 0.4),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(
          color: colorScheme.primary.withValues(alpha: 0.2),
          width: 1,
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
                Row(
                  children: <Widget>[
                    Icon(
                      Icons.touch_app_rounded,
                      color: colorScheme.primary,
                      size: 20,
                    ),
                    const SizedBox(width: 8),
                    Text(
                      '选中月份联动看板 (${_months[_selectedMonthIndex]})',
                      style: TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.bold,
                        color: colorScheme.onPrimaryContainer,
                      ),
                    ),
                  ],
                ),
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 8,
                    vertical: 4,
                  ),
                  decoration: BoxDecoration(
                    color: profit >= 0 ? Colors.green : Colors.red,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    profit >= 0 ? '盈利良好' : '支出预警',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 14),
            Row(
              children: <Widget>[
                Expanded(
                  child: _buildMetricTile(
                    label: '月度收入',
                    value: '${income.toStringAsFixed(1)} 万',
                    color: Colors.teal,
                  ),
                ),
                Expanded(
                  child: _buildMetricTile(
                    label: '月度支出',
                    value: '${expense.toStringAsFixed(1)} 万',
                    color: Colors.deepOrangeAccent,
                  ),
                ),
                Expanded(
                  child: _buildMetricTile(
                    label: '净利润',
                    value: '${profit.toStringAsFixed(1)} 万',
                    color: colorScheme.primary,
                  ),
                ),
                Expanded(
                  child: _buildMetricTile(
                    label: '利润率',
                    value: '${margin.toStringAsFixed(1)}%',
                    color: Colors.purpleAccent,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMetricTile({
    required String label,
    required String value,
    required Color color,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          label,
          style: const TextStyle(fontSize: 11, color: Colors.black54),
        ),
        const SizedBox(height: 4),
        Text(
          value,
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
      ],
    );
  }

  Widget _buildLegendItem(String label, Color color) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        Container(
          width: 10,
          height: 10,
          decoration: BoxDecoration(color: color, shape: BoxShape.circle),
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
