import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

/// 商业看板多图表深度联动示例 (Dashboard Linkage)
///
/// 演示顶部月度折线图与中部部门开销柱状图、底部渠道占比饼图的双向实时数据联动。
class ChartLinkageDemoPage extends StatefulWidget {
  const ChartLinkageDemoPage({super.key, required this.title});

  final String title;

  @override
  State<ChartLinkageDemoPage> createState() => _ChartLinkageDemoPageState();
}

class _MonthlyOverview {
  const _MonthlyOverview({
    required this.monthName,
    required this.totalRevenue,
    required this.deptExpenses, // [研发, 运营, 市场, 行政] (万元)
    required this.channelShares, // [直接访问, 社交广告, 渠道代理, 搜索引擎] (万元)
  });

  final String monthName;
  final double totalRevenue;
  final List<double> deptExpenses;
  final List<double> channelShares;
}

class _ChartLinkageDemoPageState extends State<ChartLinkageDemoPage> {
  int _activeMonthIndex = 2; // 默认选中 3月

  static const List<String> _deptNames = <String>['研发', '运营', '市场', '行政'];
  static const List<Color> _deptColors = <Color>[
    Colors.teal,
    Colors.indigo,
    Colors.amber,
    Colors.pinkAccent,
  ];

  static const List<String> _channelNames = <String>['直接访问', '社交广告', '渠道代理', '搜索引擎'];
  static const List<Color> _channelColors = <Color>[
    Color(0xFF3B82F6),
    Color(0xFF10B981),
    Color(0xFFF59E0B),
    Color(0xFF8B5CF6),
  ];

  static const List<_MonthlyOverview> _records = <_MonthlyOverview>[
    _MonthlyOverview(
      monthName: '1月',
      totalRevenue: 68.0,
      deptExpenses: <double>[28.0, 14.0, 18.0, 8.0],
      channelShares: <double>[22.0, 18.0, 16.0, 12.0],
    ),
    _MonthlyOverview(
      monthName: '2月',
      totalRevenue: 75.5,
      deptExpenses: <double>[30.0, 16.0, 20.0, 9.5],
      channelShares: <double>[25.0, 22.0, 16.5, 12.0],
    ),
    _MonthlyOverview(
      monthName: '3月',
      totalRevenue: 92.0,
      deptExpenses: <double>[36.0, 18.0, 26.0, 12.0],
      channelShares: <double>[32.0, 26.0, 20.0, 14.0],
    ),
    _MonthlyOverview(
      monthName: '4月',
      totalRevenue: 86.4,
      deptExpenses: <double>[34.0, 17.5, 23.0, 11.9],
      channelShares: <double>[28.0, 24.0, 21.0, 13.4],
    ),
    _MonthlyOverview(
      monthName: '5月',
      totalRevenue: 108.0,
      deptExpenses: <double>[42.0, 22.0, 31.0, 13.0],
      channelShares: <double>[38.0, 32.0, 22.0, 16.0],
    ),
    _MonthlyOverview(
      monthName: '6月',
      totalRevenue: 125.0,
      deptExpenses: <double>[48.0, 25.0, 36.0, 16.0],
      channelShares: <double>[45.0, 38.0, 24.0, 18.0],
    ),
  ];

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final ColorScheme colorScheme = theme.colorScheme;

    final _MonthlyOverview currentRecord = _records[_activeMonthIndex];
    final double totalDeptExpense = currentRecord.deptExpenses.fold(
      0.0,
      (double sum, double val) => sum + val,
    );

    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            // 联动说明
            _buildNoticeBanner(colorScheme, currentRecord),
            const SizedBox(height: 16),

            // 图表 1：顶部时间轴折线图（主控图）
            _buildTimelineLineChart(colorScheme),
            const SizedBox(height: 16),

            // 图表 2：中部部门支出柱状图（联动从属）
            _buildDeptBarChart(colorScheme, currentRecord, totalDeptExpense),
            const SizedBox(height: 16),

            // 图表 3：底部渠道来源分布饼图（联动从属）
            _buildChannelPieChart(colorScheme, currentRecord),
          ],
        ),
      ),
    );
  }

  Widget _buildNoticeBanner(
    ColorScheme colorScheme,
    _MonthlyOverview currentRecord,
  ) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: colorScheme.primaryContainer.withValues(alpha: 0.5),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: colorScheme.primary.withValues(alpha: 0.3)),
      ),
      child: Row(
        children: <Widget>[
          Icon(
            Icons.hub_rounded,
            color: colorScheme.primary,
            size: 22,
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                const Text(
                  '多图表交互联动引擎',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                ),
                Text(
                  '滑动上方折线图切换月份，下方柱状图与饼图将实时动态响应。当前联动焦点：【${currentRecord.monthName}】',
                  style: TextStyle(
                    fontSize: 12,
                    color: colorScheme.onPrimaryContainer,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTimelineLineChart(ColorScheme colorScheme) {
    const Color lineColor = Colors.indigoAccent;

    return Card(
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 16, 20, 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: <Widget>[
                Row(
                  children: <Widget>[
                    Icon(
                      Icons.show_chart_rounded,
                      size: 18,
                      color: colorScheme.primary,
                    ),
                    const SizedBox(width: 6),
                    Text(
                      '① 月度营业额主控趋势',
                      style: TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.bold,
                        color: colorScheme.onSurface,
                      ),
                    ),
                  ],
                ),
                Text(
                  '点击/触摸对应点切换',
                  style: TextStyle(
                    fontSize: 11,
                    color: colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            SizedBox(
              height: 180,
              child: LineChart(
                LineChartData(
                  minX: 0,
                  maxX: (_records.length - 1).toDouble(),
                  minY: 40,
                  maxY: 140,
                  gridData: FlGridData(
                    show: true,
                    horizontalInterval: 30,
                    drawVerticalLine: false,
                    getDrawingHorizontalLine: (double v) => FlLine(
                      color: colorScheme.outlineVariant.withValues(alpha: 0.3),
                      strokeWidth: 1,
                      dashArray: const <int>[4, 4],
                    ),
                  ),
                  titlesData: FlTitlesData(
                    leftTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        reservedSize: 36,
                        interval: 30,
                        getTitlesWidget: (double v, TitleMeta m) => Text(
                          '${v.toInt()}w',
                          style: TextStyle(
                            fontSize: 10,
                            color: colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ),
                    ),
                    bottomTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        reservedSize: 26,
                        interval: 1,
                        getTitlesWidget: (double v, TitleMeta m) {
                          final int idx = v.toInt();
                          if (idx < 0 || idx >= _records.length) {
                            return const SizedBox.shrink();
                          }
                          final bool isSel = idx == _activeMonthIndex;
                          return Padding(
                            padding: const EdgeInsets.only(top: 4),
                            child: Text(
                              _records[idx].monthName,
                              style: TextStyle(
                                fontSize: isSel ? 12 : 10,
                                fontWeight: isSel
                                    ? FontWeight.bold
                                    : FontWeight.normal,
                                color: isSel
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
                    touchCallback:
                        (FlTouchEvent event, LineTouchResponse? res) {
                      if (res != null &&
                          res.lineBarSpots != null &&
                          res.lineBarSpots!.isNotEmpty) {
                        final int idx = res.lineBarSpots!.first.x.toInt();
                        if (idx >= 0 &&
                            idx < _records.length &&
                            idx != _activeMonthIndex) {
                          setState(() {
                            _activeMonthIndex = idx;
                          });
                        }
                      }
                    },
                    touchTooltipData: LineTouchTooltipData(
                      getTooltipColor: (LineBarSpot spot) =>
                          colorScheme.inverseSurface.withValues(alpha: 0.9),
                      getTooltipItems: (List<LineBarSpot> spots) {
                        return spots.map((LineBarSpot s) {
                          final _MonthlyOverview r = _records[s.x.toInt()];
                          return LineTooltipItem(
                            '${r.monthName} 总营收: ${r.totalRevenue} 万',
                            const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 11,
                            ),
                          );
                        }).toList();
                      },
                    ),
                  ),
                  lineBarsData: <LineChartBarData>[
                    LineChartBarData(
                      spots: _records.asMap().entries.map((MapEntry<int, _MonthlyOverview> e) {
                        return FlSpot(e.key.toDouble(), e.value.totalRevenue);
                      }).toList(),
                      isCurved: true,
                      curveSmoothness: 0.35,
                      color: lineColor,
                      barWidth: 3,
                      dotData: FlDotData(
                        show: true,
                        getDotPainter:
                            (FlSpot s, double p, LineChartBarData b, int i) {
                          final bool isSel = i == _activeMonthIndex;
                          return FlDotCirclePainter(
                            radius: isSel ? 6 : 3.5,
                            color: isSel ? Colors.white : lineColor,
                            strokeWidth: isSel ? 3 : 1.5,
                            strokeColor: lineColor,
                          );
                        },
                      ),
                      belowBarData: BarAreaData(
                        show: true,
                        gradient: LinearGradient(
                          begin: Alignment.topCenter,
                          end: Alignment.bottomCenter,
                          colors: <Color>[
                            lineColor.withValues(alpha: 0.3),
                            lineColor.withValues(alpha: 0.0),
                          ],
                        ),
                      ),
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

  Widget _buildDeptBarChart(
    ColorScheme colorScheme,
    _MonthlyOverview record,
    double totalExpense,
  ) {
    return Card(
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
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
                    const Icon(
                      Icons.bar_chart_rounded,
                      size: 18,
                      color: Colors.teal,
                    ),
                    const SizedBox(width: 6),
                    Text(
                      '② ${record.monthName} 部门开销分布',
                      style: TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.bold,
                        color: colorScheme.onSurface,
                      ),
                    ),
                  ],
                ),
                Text(
                  '总支出: ${totalExpense.toStringAsFixed(1)} 万',
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: Colors.teal.shade800,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            SizedBox(
              height: 180,
              child: BarChart(
                BarChartData(
                  maxY: 60,
                  minY: 0,
                  gridData: FlGridData(
                    show: true,
                    horizontalInterval: 20,
                    drawVerticalLine: false,
                    getDrawingHorizontalLine: (double v) => FlLine(
                      color: colorScheme.outlineVariant.withValues(alpha: 0.3),
                      strokeWidth: 1,
                      dashArray: const <int>[4, 4],
                    ),
                  ),
                  titlesData: FlTitlesData(
                    leftTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        reservedSize: 34,
                        interval: 20,
                        getTitlesWidget: (double v, TitleMeta m) => Text(
                          '${v.toInt()}w',
                          style: TextStyle(
                            fontSize: 10,
                            color: colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ),
                    ),
                    bottomTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        reservedSize: 26,
                        getTitlesWidget: (double v, TitleMeta m) {
                          final int idx = v.toInt();
                          if (idx < 0 || idx >= _deptNames.length) {
                            return const SizedBox.shrink();
                          }
                          return Padding(
                            padding: const EdgeInsets.only(top: 4),
                            child: Text(
                              _deptNames[idx],
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.w500,
                                color: colorScheme.onSurface,
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
                  barTouchData: BarTouchData(
                    handleBuiltInTouches: true,
                    touchTooltipData: BarTouchTooltipData(
                      getTooltipColor: (BarChartGroupData group) =>
                          colorScheme.inverseSurface.withValues(alpha: 0.9),
                      getTooltipItem: (
                        BarChartGroupData group,
                        int groupIndex,
                        BarChartRodData rod,
                        int rodIndex,
                      ) {
                        return BarTooltipItem(
                          '${_deptNames[groupIndex]}: ${rod.toY} 万元',
                          const TextStyle(
                            color: Colors.white,
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                          ),
                        );
                      },
                    ),
                  ),
                  barGroups: record.deptExpenses
                      .asMap()
                      .entries
                      .map((MapEntry<int, double> e) {
                    final int idx = e.key;
                    final double val = e.value;
                    return BarChartGroupData(
                      x: idx,
                      barRods: <BarChartRodData>[
                        BarChartRodData(
                          toY: val,
                          color: _deptColors[idx],
                          width: 22,
                          borderRadius: const BorderRadius.vertical(
                            top: Radius.circular(6),
                          ),
                        ),
                      ],
                    );
                  }).toList(),
                ),
                duration: const Duration(milliseconds: 300),
                curve: Curves.easeInOut,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildChannelPieChart(
    ColorScheme colorScheme,
    _MonthlyOverview record,
  ) {
    final double totalChannel = record.channelShares.fold(
      0.0,
      (double sum, double val) => sum + val,
    );

    return Card(
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              children: <Widget>[
                const Icon(
                  Icons.pie_chart_rounded,
                  size: 18,
                  color: Colors.purple,
                ),
                const SizedBox(width: 6),
                Text(
                  '③ ${record.monthName} 获客渠道构成',
                  style: TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.bold,
                    color: colorScheme.onSurface,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Row(
              children: <Widget>[
                // 饼图
                SizedBox(
                  width: 140,
                  height: 140,
                  child: PieChart(
                    PieChartData(
                      sectionsSpace: 2,
                      centerSpaceRadius: 28,
                      borderData: FlBorderData(show: false),
                      sections: record.channelShares
                          .asMap()
                          .entries
                          .map((MapEntry<int, double> e) {
                        final int idx = e.key;
                        final double val = e.value;
                        final double pct = (val / totalChannel) * 100;
                        return PieChartSectionData(
                          color: _channelColors[idx],
                          value: val,
                          title: '${pct.toStringAsFixed(0)}%',
                          radius: 38,
                          titleStyle: const TextStyle(
                            fontSize: 10,
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                          ),
                        );
                      }).toList(),
                    ),
                    duration: const Duration(milliseconds: 300),
                    curve: Curves.easeInOut,
                  ),
                ),
                const SizedBox(width: 16),
                // 渠道明细 Legend
                Expanded(
                  child: Column(
                    children: record.channelShares
                        .asMap()
                        .entries
                        .map((MapEntry<int, double> e) {
                      final int idx = e.key;
                      final double val = e.value;
                      final double pct = (val / totalChannel) * 100;
                      return Padding(
                        padding: const EdgeInsets.symmetric(vertical: 3),
                        child: Row(
                          children: <Widget>[
                            Container(
                              width: 8,
                              height: 8,
                              decoration: BoxDecoration(
                                color: _channelColors[idx],
                                shape: BoxShape.circle,
                              ),
                            ),
                            const SizedBox(width: 6),
                            Expanded(
                              child: Text(
                                _channelNames[idx],
                                style: const TextStyle(fontSize: 11),
                              ),
                            ),
                            Text(
                              '${pct.toStringAsFixed(1)}% (${val.toStringAsFixed(1)}w)',
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.bold,
                                color: colorScheme.onSurface,
                              ),
                            ),
                          ],
                        ),
                      );
                    }).toList(),
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
