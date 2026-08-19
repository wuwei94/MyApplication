package com.example.william.my.module.opensource.activity.widget

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.opensource.databinding.OpenActivityCountdownBinding

/**
 * CountdownView — 倒计时控件
 *
 * CountdownView 是一个功能强大的倒计时控件，支持多种自定义样式。
 *
 * 核心特性：
 * 1. 精确计时：毫秒级精度，支持倒计时和正计时
 * 2. 丰富的自定义：支持自定义数字样式、分隔符、背景
 * 3. 多种格式：支持天、时、分、秒、毫秒的自由组合
 * 4. 动态更新：支持动态更新显示时间
 *
 * 基本用法：
 * ```kotlin
 * // XML 中使用
 * <com.iwgang.countdownview.CountdownView
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     app:cvTimeTextColor="#000000"
 *     app:cvTimeTextSize="20sp" />
 *
 * // 代码中启动倒计时
 * countdownView.start(60000)  // 60 秒倒计时
 * ```
 *
 * 适用场景：
 * - 活动倒计时、秒杀倒计时
 * - 验证码倒计时、重新发送倒计时
 * - 游戏计时、运动计时
 *
 * https://github.com/iwgang/CountdownView
 */
@Route(path = RouterPath.OpenSource.CountdownView)
class CountdownActivity : BaseVBActivity<OpenActivityCountdownBinding>() {

    override fun getViewBinding(): OpenActivityCountdownBinding {
        return OpenActivityCountdownBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initCountdownView()
    }

    private fun initCountdownView() {
        mBinding.countdownView.start(995550000) // Millisecond
        for (time in 0..999) {
            mBinding.countdownView.updateShow(time.toLong())
        }
    }
}