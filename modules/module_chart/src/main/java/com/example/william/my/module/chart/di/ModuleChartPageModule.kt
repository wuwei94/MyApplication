package com.example.william.my.module.chart.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.chart.ChartMainActivity
import com.example.william.my.module.chart.activity.MPBarChartActivity
import com.example.william.my.module.chart.activity.MPChartLinkageActivity
import com.example.william.my.module.chart.activity.MPLineChartActivity
import com.example.william.my.module.chart.activity.MPPieChartActivity
import com.example.william.my.module.chart.activity.MPRadarChartActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_chart 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleChartPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Chart.MPBarChart)
    fun provideMPBarChartActivity(): Class<out Activity> = MPBarChartActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Chart.MPChartLinkage)
    fun provideMPChartLinkageActivity(): Class<out Activity> = MPChartLinkageActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Chart.MPLineChart)
    fun provideMPLineChartActivity(): Class<out Activity> = MPLineChartActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Chart.MPPieChart)
    fun provideMPPieChartActivity(): Class<out Activity> = MPPieChartActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Chart.MPRadarChart)
    fun provideMPRadarChartActivity(): Class<out Activity> = MPRadarChartActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Chart.Main)
    fun provideChartMainActivity(): Class<out Activity> = ChartMainActivity::class.java
}
