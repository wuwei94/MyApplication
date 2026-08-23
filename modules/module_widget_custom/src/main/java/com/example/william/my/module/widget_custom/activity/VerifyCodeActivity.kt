package com.example.william.my.module.widget_custom.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.widget_custom.databinding.DemoActivityVerifyCodeBinding

/**
 * VerifyCode — 验证码输入控件
 *
 * 验证码输入控件，支持自定义验证码长度和样式。
 *
 * 核心特性：
 * 1. 自定义长度：支持 4 位、6 位等验证码长度
 * 2. 自定义样式：支持自定义输入框样式、颜色、字体
 * 3. 输入监听：支持输入完成回调
 * 4. 自动聚焦：自动聚焦到第一个输入框
 *
 * 基本用法：
 * ```kotlin
 * // XML 中使用
 * <com.example.widget.VerifyCodeView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:codeLength="4" />
 *
 * // 代码中设置
 * verifyCodeView.editContent = "1234"
 * verifyCodeView.setOnCompleteListener { code ->
 *     // 处理验证码输入完成
 * }
 * ```
 *
 * 适用场景：
 * - 短信验证码输入
 * - 支付密码输入
 * - 任何需要分格输入的场景
 */
@Route(path = RouterPath.WidgetCustom.VerifyCode)
class VerifyCodeActivity : BaseVBActivity<DemoActivityVerifyCodeBinding>() {

    override fun getViewBinding(): DemoActivityVerifyCodeBinding {
        return DemoActivityVerifyCodeBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initVerifyCode()
    }

    private fun initVerifyCode() {
        mBinding.verifyCodeView.editContent = "0731"
    }
}