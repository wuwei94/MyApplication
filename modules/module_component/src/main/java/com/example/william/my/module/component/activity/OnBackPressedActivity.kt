package com.example.william.my.module.component.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * OnBackPressedDispatcher — 返回键拦截
 *
 * OnBackPressedDispatcher 是 AndroidX 提供的返回键拦截 API，替代已废弃的 onBackPressed()。
 *
 * 核心特性：
 * 1. 生命周期感知：自动处理生命周期，避免内存泄漏
 * 2. 优先级控制：支持多个回调，按优先级执行
 * 3. 动态启用/禁用：支持动态启用或禁用回调
 * 4. 代码简洁：无需重写 onBackPressed，代码更简洁
 *
 * 基本用法：
 * ```kotlin
 * // 创建回调
 * val callback = object : OnBackPressedCallback(true) {
 *     override fun handleOnBackPressed() {
 *         // 处理返回键
 *     }
 * }
 *
 * // 添加回调
 * onBackPressedDispatcher.addCallback(this, callback)
 *
 * // 禁用回调
 * callback.isEnabled = false
 *
 * // 移除回调
 * callback.remove()
 * ```
 *
 * 页面逻辑：
 * - 点击「拦截返回键」：注册回调，按返回键时显示 Toast 并关闭页面
 * - 点击「取消拦截」：移除回调，按返回键正常返回
 *
 * 适用场景：
 * - 拦截返回键操作
 * - 自定义返回逻辑
 * - 多个返回键处理优先级
 */
@Route(path = RouterPath.Component.OnBackPressed)
class OnBackPressedActivity : BasicResponseActivity() {

    private var backCallback: OnBackPressedCallback? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("OnBackPressedDispatcher\n\n点击下方按钮拦截返回键")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "拦截返回键",
        "取消拦截",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> interceptBackPress()
            1 -> cancelIntercept()
        }
    }

    private fun interceptBackPress() {
        if (backCallback != null) {
            appendLog("返回键已处于拦截状态")
            return
        }

        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                appendLog("返回键被拦截")
            }
        }
        onBackPressedDispatcher.addCallback(this, backCallback!!)
        appendLog("已拦截返回键，按返回键试试")
    }

    private fun cancelIntercept() {
        backCallback?.let {
            it.isEnabled = false
            backCallback = null
            appendLog("已取消拦截")
        } ?: appendLog("当前未拦截返回键")
    }

    override fun onDestroy() {
        super.onDestroy()
        backCallback?.isEnabled = false
    }
}
