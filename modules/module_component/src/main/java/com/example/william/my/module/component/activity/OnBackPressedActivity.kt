package com.example.william.my.module.component.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * OnBackPressedDispatcher 返回键拦截
 *
 * 演示使用 OnBackPressedCallback 拦截返回键操作。
 * Android 13+ 推荐使用 OnBackPressedDispatcher 替代已废弃的 onBackPressed()。
 *
 * 页面逻辑：
 * - 点击「拦截返回键」：注册回调，按返回键时显示 Toast 并关闭页面
 * - 点击「取消拦截」：移除回调，按返回键正常返回
 */
@Route(path = RouterPath.Component.OnBackPressed)
class OnBackPressedActivity : BasicResponseActivity() {

    private var backCallback: OnBackPressedCallback? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showResponse("OnBackPressedDispatcher\n\n点击下方按钮拦截返回键")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "拦截返回键",
            "取消拦截"
        )
    }

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
