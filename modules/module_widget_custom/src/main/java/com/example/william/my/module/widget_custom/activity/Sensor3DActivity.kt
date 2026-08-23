package com.example.william.my.module.widget_custom.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.widget_custom.databinding.DemoActivitySensor3dBinding

/**
 * 3D 重力感应控件 — Sensor3DView 演示
 *
 * 通过 XML 属性配置前景、中景、背景三层 drawable，并设置各层的加速度比率，
 * 实现重力感应驱动的视差滚动效果。用户倾斜设备时，不同层级以不同速度移动，
 * 形成 3D 纵深感。
 *
 * 核心原理：
 * 1. 监听加速度传感器数据，计算设备倾斜方向和角度
 * 2. 根据各层设置的 accelerationX / accelerationY 比率，以不同速度偏移前景、中景、背景
 * 3. 使用 Matrix 平移变换实现多层视差效果
 *
 * XML 属性：
 * - foreground / midground / background — 三层 drawable
 * - foregroundAccelerationX / foregroundAccelerationY — 前景加速度比率
 * - midgroundAccelerationX / midgroundAccelerationY — 中景加速度比率
 * - backgroundAccelerationX / backgroundAccelerationY — 背景加速度比率
 *
 * 适用场景：
 * - 重力感应交互的视差滚动效果
 * - 多层背景视差动画（如游戏、壁纸）
 * - 增强 UI 的立体感和沉浸感
 */
@Route(path = RouterPath.WidgetCustom.Sensor3DView)
class Sensor3DActivity : BaseVBActivity<DemoActivitySensor3dBinding>() {

    override fun getViewBinding(): DemoActivitySensor3dBinding {
        return DemoActivitySensor3dBinding.inflate(layoutInflater)
    }
}