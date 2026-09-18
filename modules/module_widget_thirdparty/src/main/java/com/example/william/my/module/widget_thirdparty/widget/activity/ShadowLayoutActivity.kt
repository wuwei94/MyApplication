package com.example.william.my.module.widget_thirdparty.widget.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivityShadowLayoutBinding

/**
 * ShadowLayout — 阴影与圆角布局预览
 *
 * 核心机制与避坑点：
 * 1. 阴影绘制：由库在 Canvas 上绘制阴影，可配置颜色 / 半径 / 偏移
 * 2. 布局预览：本页为静态 XML 展示，无交互操作项
 *
 * 官方参考：
 * https://github.com/lihangleo2/ShadowLayout
 */
@Route(path = RouterPath.WidgetThirdparty.ShadowLayout)
class ShadowLayoutActivity : BaseVBActivity<WidgetThirdpartyActivityShadowLayoutBinding>() {

    override fun getViewBinding(): WidgetThirdpartyActivityShadowLayoutBinding = WidgetThirdpartyActivityShadowLayoutBinding.inflate(layoutInflater)
}
