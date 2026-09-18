package com.example.william.my.module.widget_custom.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget_custom.databinding.WidgetActivityVerifyCodeBinding

/**
 * VerifyCode — 验证码输入控件
 *
 * 验证码输入控件，支持自定义验证码长度和样式。
 *
 * 核心机制与避坑点：
 * 1. 自定义长度：支持 4 位、6 位等验证码长度
 * 2. 自定义样式：支持自定义输入框样式、颜色、字体
 * 3. 输入监听：支持输入完成回调
 * 4. 自动聚焦：自动聚焦到第一个输入框
 */
@Route(path = RouterPath.WidgetCustom.VerifyCode)
class VerifyCodeActivity : BaseVBActivity<WidgetActivityVerifyCodeBinding>() {

    override fun getViewBinding(): WidgetActivityVerifyCodeBinding = WidgetActivityVerifyCodeBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initVerifyCode()
    }

    private fun initVerifyCode() {
        binding.widgetVerifyCodeView.editContent = "0731"
    }
}
