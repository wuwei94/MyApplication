import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/chart/bar_chart_demo.dart';
import 'package:flutter_demo/demos/chart/chart_linkage_demo.dart';
import 'package:flutter_demo/demos/chart/line_chart_demo.dart';
import 'package:flutter_demo/demos/chart/pie_chart_demo.dart';
import 'package:flutter_demo/demos/chart/radar_chart_demo.dart';

/// 复杂图表模块（fl_chart 实战演练）
///
/// 官方文档: https://pub.dev/packages/fl_chart
class ChartCatalog extends CatalogSection {
  const ChartCatalog._();

  @override
  String get path => 'chart';

  @override
  String get title => 'Charts';

  @override
  String get subtitle => '折线图、柱状图、饼图、雷达图与触摸 Tooltip 深度联动';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = <CatalogEntry>[
    CatalogEntry.page(
      path: 'line_chart',
      title: '折线图与面积渐变联动',
      subtitle: '多曲线对比、平滑贝塞尔、渐变填充与触摸十字线联动指标卡',
      pageBuilder: (BuildContext context) => const LineChartDemoPage(
        title: '折线图与面积渐变联动',
      ),
    ),
    CatalogEntry.page(
      path: 'bar_chart',
      title: '分组与正负收益柱状图',
      subtitle: '多维对比、正负收支柱状图与触摸高亮联动面板',
      pageBuilder: (BuildContext context) => const BarChartDemoPage(
        title: '分组与正负收益柱状图',
      ),
    ),
    CatalogEntry.page(
      path: 'pie_chart',
      title: '动态环形饼图与扇区放大',
      subtitle: '甜甜圈/实心切换、触摸扇区凸起动画与双向图例联动',
      pageBuilder: (BuildContext context) => const PieChartDemoPage(
        title: '动态环形饼图与扇区放大',
      ),
    ),
    CatalogEntry.page(
      path: 'radar_chart',
      title: '多维能力雷达图与评级',
      subtitle: '六维技术能力模型、双数据集对比与触摸维度评定联动',
      pageBuilder: (BuildContext context) => const RadarChartDemoPage(
        title: '多维能力雷达图与评级',
      ),
    ),
    CatalogEntry.page(
      path: 'chart_linkage',
      title: '商业看板多图表深度联动',
      subtitle: '折线 + 柱状 + 饼图联合联动：主控时间轴驱动部门与渠道数据实时刷新',
      pageBuilder: (BuildContext context) => const ChartLinkageDemoPage(
        title: '商业看板多图表深度联动',
      ),
    ),
  ];
}

/// 单例实例
const ChartCatalog chartCatalog = ChartCatalog._();
