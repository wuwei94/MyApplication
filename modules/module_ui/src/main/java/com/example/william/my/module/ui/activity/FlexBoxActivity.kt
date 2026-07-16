package com.example.william.my.module.ui.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.ui.adapter.RecyclerAdapter
import com.example.william.my.module.ui.databinding.UiActivityFlexBoxBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager

/**
 * FlexboxLayoutManager 是 Google 出品的弹性布局管理器，可用于 RecyclerView
 * 核心属性：
 * - FlexDirection: 主轴方向（ROW 水平 / COLUMN 垂直 / ROW_REVERSE / COLUMN_REVERSE）
 * - FlexWrap: 是否换行（WRAP 换行 / NOWRAP 不换行 / WRAP_REVERSE 反向换行）
 * - justifyContent: 主轴对齐方式（FLEX_START / CENTER / FLEX_END / SPACE_BETWEEN 等）
 * - alignItems: 交叉轴对齐方式（FLEX_START / CENTER / FLEX_END / STRETCH 等）
 */
@Route(path = RouterPath.UI.FlexBox)
class FlexBoxActivity : BaseVBActivity<UiActivityFlexBoxBinding>() {

    override fun getViewBinding(): UiActivityFlexBoxBinding {
        return UiActivityFlexBoxBinding.inflate(layoutInflater)
    }

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
        manager.flexDirection = FlexDirection.ROW //主轴方向
        manager.flexWrap = FlexWrap.WRAP //是否换行

        mBinding.flexboxRecycleView.layoutManager = manager
        mBinding.flexboxRecycleView.adapter = RecyclerAdapter(mData)
    }
}
