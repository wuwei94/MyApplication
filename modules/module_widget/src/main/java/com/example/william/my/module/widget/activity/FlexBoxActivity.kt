package com.example.william.my.module.widget.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget.adapter.RecyclerAdapter
import com.example.william.my.module.widget.databinding.UiActivityFlexBoxBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager

/**
 * FlexBox — 弹性布局管理器
 *
 * FlexboxLayoutManager 是 Google 出品的弹性布局管理器，用于 RecyclerView。
 *
 * 核心特性：
 * 1. 弹性布局：支持类似 CSS Flexbox 的弹性布局
 * 2. 自动换行：支持自动换行，适合标签、流式布局
 * 3. 灵活对齐：支持多种对齐方式
 * 4. 响应式：支持不同屏幕尺寸的自适应布局
 *
 * 核心属性：
 * 1. FlexDirection：主轴方向
 *    - ROW：水平方向
 *    - COLUMN：垂直方向
 *    - ROW_REVERSE：水平反向
 *    - COLUMN_REVERSE：垂直反向
 *
 * 2. FlexWrap：是否换行
 *    - WRAP：换行
 *    - NOWRAP：不换行
 *    - WRAP_REVERSE：反向换行
 *
 * 3. justifyContent：主轴对齐方式
 *    - FLEX_START：起始对齐
 *    - CENTER：居中对齐
 *    - FLEX_END：结束对齐
 *    - SPACE_BETWEEN：两端对齐
 *
 * 4. alignItems：交叉轴对齐方式
 *    - FLEX_START：起始对齐
 *    - CENTER：居中对齐
 *    - FLEX_END：结束对齐
 *    - STRETCH：拉伸对齐
 *
 * 基本用法：
 * ```kotlin
 * val manager = FlexboxLayoutManager(context)
 * manager.flexDirection = FlexDirection.ROW
 * manager.flexWrap = FlexWrap.WRAP
 * recyclerView.layoutManager = manager
 * ```
 *
 * 适用场景：
 * - 标签云、流式布局
 * - 自适应布局
 * - 响应式设计
 *
 * https://github.com/google/flexbox-layout
 */
@Route(path = RouterPath.Widget.FlexBox)
class FlexBoxActivity : BaseVBActivity<UiActivityFlexBoxBinding>() {

    override fun getViewBinding(): UiActivityFlexBoxBinding = UiActivityFlexBoxBinding.inflate(layoutInflater)

    private val mData = arrayListOf(
        "FlexBox_1234",
        "FlexBox_5678",
        "FlexBox_90",
        "FlexBox_123456",
        "FlexBox_567890",
        "FlexBox_12345678",
        "FlexBox_1234567890",
        "FlexBox_123",
        "FlexBox_456",
    )

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initFlexBox()
    }

    private fun initFlexBox() {
        val manager = FlexboxLayoutManager(this)
        manager.flexDirection = FlexDirection.ROW // 主轴方向
        manager.flexWrap = FlexWrap.WRAP // 是否换行

        mBinding.flexboxRecycleView.layoutManager = manager
        mBinding.flexboxRecycleView.adapter = RecyclerAdapter(mData)
    }
}
