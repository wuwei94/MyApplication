package com.example.william.my.module.widget_thirdparty.widget.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivityCountdownBinding

/**
 * CountdownView — 倒计时控件
 *
 * CountdownView 是一个功能强大的倒计时控件，支持多种自定义样式。
 *
 * 核心机制与避坑点：
 * 1. 精确计时：毫秒级精度，支持倒计时和正计时
 * 2. 丰富的自定义：支持自定义数字样式、分隔符、背景
 * 3. 多种格式：支持天、时、分、秒、毫秒的自由组合
 * 4. 动态更新：支持动态更新显示时间
 *
 * https://github.com/iwgang/CountdownView
 */
@Route(path = RouterPath.WidgetThirdparty.CountdownView)
class CountdownActivity : BaseVBActivity<WidgetThirdpartyActivityCountdownBinding>() {

    override fun getViewBinding(): WidgetThirdpartyActivityCountdownBinding = WidgetThirdpartyActivityCountdownBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initCountdownView()
    }

    private fun initCountdownView() {
        binding.countdownView.start(995550000) // Millisecond
        for (time in 0..999) {
            binding.countdownView.updateShow(time.toLong())
        }
    }
}
