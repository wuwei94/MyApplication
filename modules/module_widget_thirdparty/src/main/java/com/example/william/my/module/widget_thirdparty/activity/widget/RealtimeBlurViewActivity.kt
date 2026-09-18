package com.example.william.my.module.widget_thirdparty.activity.widget

import android.os.Bundle
import android.util.TypedValue
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivityBlurViewBinding

/**
 * RealtimeBlurView — 实时高斯模糊布局预览
 *
 * 核心机制与避坑点：
 * 1. 实时模糊：对下层内容做动态高斯模糊，适合毛玻璃浮层
 * 2. 模糊半径：setBlurRadius 以像素为单位，DPI 换算后设置
 * 3. 布局预览：本页仅配置半径并静态展示，无交互操作项
 *
 * 官方参考：
 * https://github.com/mmin18/RealtimeBlurView
 */
@Route(path = RouterPath.WidgetThirdparty.RealtimeBlurView)
class RealtimeBlurViewActivity : BaseVBActivity<WidgetThirdpartyActivityBlurViewBinding>() {

    override fun getViewBinding(): WidgetThirdpartyActivityBlurViewBinding = WidgetThirdpartyActivityBlurViewBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding.realtimeBlurView.setBlurRadius(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10f,
                resources.displayMetrics,
            ),
        )
    }
}
