package com.example.william.my.module.widget_custom.sensor3d.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget_custom.databinding.WidgetActivitySensor3dBinding

/**
 * Sensor3DView — 3D 重力感应视差控件
 *
 * 通过 XML 属性配置前景、中景、背景三层 drawable，并设置各层的加速度比率，
 * 实现重力感应驱动的视差滚动效果。用户倾斜设备时，不同层级以不同速度移动，
 * 形成 3D 纵深感。
 *
 * 核心机制与避坑点：
 * 1. 多层视差：前景 / 中景 / 背景三层各自独立配置 accelerationX / accelerationY 比率
 * 2. 传感器驱动：监听加速度传感器数据，实时计算设备倾斜方向和角度
 * 3. Matrix 变换：使用 Matrix 平移变换实现多层偏移，不重建布局
 * 4. XML 声明式：三层 drawable 与加速度比率均可通过 XML 属性直接配置
 *
 * XML 属性：
 * - foreground / midground / background — 三层 drawable
 * - foregroundAccelerationX / foregroundAccelerationY — 前景加速度比率
 * - midgroundAccelerationX / midgroundAccelerationY — 中景加速度比率
 * - backgroundAccelerationX / backgroundAccelerationY — 背景加速度比率
 */
@Route(path = RouterPath.WidgetCustom.Sensor3DView)
class Sensor3DActivity : BaseVBActivity<WidgetActivitySensor3dBinding>() {

    override fun getViewBinding(): WidgetActivitySensor3dBinding = WidgetActivitySensor3dBinding.inflate(layoutInflater)
}
