package com.example.william.my.module.chart

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 数据可视化模块入口（MPAndroidChart）
 *
 * GitHub: https://github.com/PhilJay/MPAndroidChart
 *
 * MPAndroidChart 是 Android 生态最主流的原生图表库，以 `Chart` 基类衍生出
 * LineChart / BarChart / PieChart / RadarChart 等图表视图，配合 DataSet 数据模型、
 * MarkerView 悬浮提示与 Highlight 高亮机制完成图表渲染与交互。
 *
 * 本模块演示两类使用场景：
 * 1. 基础图表：折线图 / 柱状图 / 饼图 / 雷达图，覆盖数据模型、坐标轴、图例与
 *    MarkerView 自定义提示，并通过 `OnChartValueSelectedListener` 联动页面指标看板
 * 2. 全景联动看板：以折线图为控制器，驱动柱状图与饼图随所选时间轴同步重绘
 */
@Route(path = RouterPath.Chart.Main)
class ChartMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = arrayListOf<RouterItem>()
        routerItems.add(RouterItem("── 基础图表 ──", ""))
        routerItems.add(RouterItem("LineChart（折线图 & 触摸 Tooltip）", RouterPath.Chart.MPLineChart))
        routerItems.add(RouterItem("BarChart（柱状图 & 目标达成对比）", RouterPath.Chart.MPBarChart))
        routerItems.add(RouterItem("PieChart（饼图/环形图 & 触控外扩）", RouterPath.Chart.MPPieChart))
        routerItems.add(RouterItem("RadarChart（雷达图 & 能力评估模型）", RouterPath.Chart.MPRadarChart))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 综合看板 ──", ""))
        routerItems.add(RouterItem("ChartLinkage（多图表全景联动看板）", RouterPath.Chart.MPChartLinkage))
        return routerItems
    }
}
