import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

/// 多维能力雷达图与触摸联动示例
///
/// 演示六维技术能力评估模型、双数据集对比（自我评定 vs 团队期望）与触摸维度联动评级。
class RadarChartDemoPage extends StatefulWidget {
  const RadarChartDemoPage({super.key, required this.title});

  final String title;

  @override
  State<RadarChartDemoPage> createState() => _RadarChartDemoPageState();
}

class _RadarDim {
  const _RadarDim({
    required this.name,
    required this.selfScore,
    required this.targetScore,
    required this.description,
  });

  final String name;
  final double selfScore;
  final double targetScore;
  final String description;
}

class _RadarChartDemoPageState extends State<RadarChartDemoPage> {
  int _selectedDimIndex = 0; // 默认选中第一个维度
  bool _showTargetSet = true;

  static const List<_RadarDim> _dimensions = <_RadarDim>[
    _RadarDim(
      name: '架构设计',
      selfScore: 92.0,
      targetScore: 85.0,
      description: '掌握 Clean Architecture、模块化治理、跨端分层设计与高内聚低耦合边界划分。',
    ),
    _RadarDim(
      name: '性能调优',
      selfScore: 88.0,
      targetScore: 80.0,
      description: '精通启动优化、Baseline Profiles、重绘重组消除、FPS 帧率治理与内存泄露排查。',
    ),
    _RadarDim(
      name: '源码机制',
      selfScore: 85.0,
      targetScore: 90.0,
      description: '深入理解 Android Binder / Handler / View 绘制体系与 Flutter Engine 渲染流水线。',
    ),
    _RadarDim(
      name: '跨端实战',
      selfScore: 95.0,
      targetScore: 85.0,
      description: '具备深厚的 Android 原生 + Flutter 双端混合开发与 Platform Channel 架构实战经验。',
    ),
    _RadarDim(
      name: '工程运维',
      selfScore: 78.0,
      targetScore: 75.0,
      description: '熟悉 Gradle Convention Plugins、CI/CD 自动化流水线构建、混淆与静态扫描规范。',
    ),
    _RadarDim(
      name: '团队协同',
      selfScore: 89.0,
      targetScore: 80.0,
      description: '优秀的跨团队技术沟通、技术方案评审把控、新人指导与技术文档沉淀习惯。',
    ),
  ];

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final ColorScheme colorScheme = theme.colorScheme;

    final _RadarDim currentDim = _dimensions[_selectedDimIndex];
    final double scoreDiff = currentDim.selfScore - currentDim.targetScore;

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

            // 雷达图卡片
            _buildRadarCard(colorScheme),
            const SizedBox(height: 16),

            // 触摸联动详细面板
            _buildDetailDimCard(colorScheme, currentDim, scoreDiff),
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
            Icon(Icons.radar_rounded, color: colorScheme.primary, size: 20),
            const SizedBox(width: 8),
            const Text(
              '双数据集对照：',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
            ),
            const Spacer(),
            FilterChip(
              avatar: Icon(
                _showTargetSet ? Icons.check_rounded : Icons.add_rounded,
                size: 16,
              ),
              label: const Text('显示职级基准线'),
              selected: _showTargetSet,
              onSelected: (bool val) => setState(() => _showTargetSet = val),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildRadarCard(ColorScheme colorScheme) {
    const Color selfColor = Colors.teal;
    const Color targetColor = Colors.amber;

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
                  '综合技术栈能力雷达 (100分制)',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: colorScheme.onSurface,
                  ),
                ),
                // 图例
                Row(
                  children: <Widget>[
                    _buildLegend('自我评定', selfColor),
                    if (_showTargetSet) ...<Widget>[
                      const SizedBox(width: 12),
                      _buildLegend('团队基准', targetColor),
                    ],
                  ],
                ),
              ],
            ),
            const SizedBox(height: 24),
            SizedBox(
              height: 280,
              child: RadarChart(
                RadarChartData(
                  radarTouchData: RadarTouchData(
                    touchCallback:
                        (FlTouchEvent event, RadarTouchResponse? res) {
                      if (res != null &&
                          res.touchedSpot != null &&
                          event is! FlPointerExitEvent) {
                        final int index =
                            res.touchedSpot!.touchedRadarEntryIndex;
                        if (index >= 0 &&
                            index < _dimensions.length &&
                            index != _selectedDimIndex) {
                          setState(() {
                            _selectedDimIndex = index;
                          });
                        }
                      }
                    },
                  ),
                  dataSets: <RadarDataSet>[
                    // 自我评定数据集
                    RadarDataSet(
                      fillColor: selfColor.withValues(alpha: 0.35),
                      borderColor: selfColor,
                      entryRadius: 4,
                      dataEntries: _dimensions
                          .map(
                            (_RadarDim d) => RadarEntry(value: d.selfScore),
                          )
                          .toList(),
                      borderWidth: 2.5,
                    ),
                    // 团队基准线数据集
                    if (_showTargetSet)
                      RadarDataSet(
                        fillColor: targetColor.withValues(alpha: 0.2),
                        borderColor: targetColor,
                        entryRadius: 3,
                        dataEntries: _dimensions
                            .map(
                              (_RadarDim d) =>
                                  RadarEntry(value: d.targetScore),
                            )
                            .toList(),
                        borderWidth: 1.8,
                      ),
                  ],
                  radarBackgroundColor: Colors.transparent,
                  borderData: FlBorderData(show: false),
                  radarBorderData: BorderSide(
                    color: colorScheme.outlineVariant.withValues(alpha: 0.4),
                    width: 1,
                  ),
                  titlePositionPercentageOffset: 0.18,
                  titleTextStyle: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                    color: colorScheme.onSurface,
                  ),
                  getTitle: (int index, double angle) {
                    if (index < 0 || index >= _dimensions.length) {
                      return const RadarChartTitle(text: '');
                    }
                    return RadarChartTitle(
                      text: _dimensions[index].name,
                      positionPercentageOffset: 0.15,
                    );
                  },
                  tickCount: 4,
                  ticksTextStyle: const TextStyle(
                    color: Colors.transparent, // 隐藏多余的蛛网内部数字
                  ),
                  tickBorderData: BorderSide(
                    color: colorScheme.outlineVariant.withValues(alpha: 0.3),
                    width: 1,
                  ),
                  gridBorderData: BorderSide(
                    color: colorScheme.outlineVariant.withValues(alpha: 0.4),
                    width: 1,
                  ),
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

  Widget _buildDetailDimCard(
    ColorScheme colorScheme,
    _RadarDim currentDim,
    double diff,
  ) {
    return Card(
      elevation: 0,
      color: colorScheme.surfaceContainerLow,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(
          color: colorScheme.primary.withValues(alpha: 0.25),
          width: 1.5,
        ),
      ),
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: <Widget>[
                Row(
                  children: <Widget>[
                    Icon(
                      Icons.verified_rounded,
                      color: colorScheme.primary,
                      size: 20,
                    ),
                    const SizedBox(width: 8),
                    Text(
                      '维度评定：${currentDim.name}',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: colorScheme.onSurface,
                      ),
                    ),
                  ],
                ),
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 10,
                    vertical: 4,
                  ),
                  decoration: BoxDecoration(
                    color: diff >= 0 ? Colors.green : Colors.orange,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    diff >= 0 ? '超基准 +${diff.toStringAsFixed(0)}分' : '待提升 ${diff.toStringAsFixed(0)}分',
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
                  child: _buildScoreBox(
                    '当前评定',
                    '${currentDim.selfScore.toInt()} 分',
                    Colors.teal,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _buildScoreBox(
                    '基准期望',
                    '${currentDim.targetScore.toInt()} 分',
                    Colors.amber.shade800,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 14),
            Text(
              '能力概要与评审建议：',
              style: TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.bold,
                color: colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              currentDim.description,
              style: TextStyle(
                fontSize: 13,
                height: 1.45,
                color: colorScheme.onSurface,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildScoreBox(String label, String score, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 12),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: color.withValues(alpha: 0.3)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(label, style: const TextStyle(fontSize: 11, color: Colors.black54)),
          const SizedBox(height: 2),
          Text(
            score,
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLegend(String label, Color color) {
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
