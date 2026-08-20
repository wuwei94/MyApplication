package com.example.william.my.module.sample.activity

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.sample.hook.HookHelper

/**
 * 反射 Hook OnClickListener — 动态代理机制演示
 *
 * 通过反射和动态代理技术，拦截 View 的点击事件。
 *
 * 核心原理：
 * 1. Android 中 View 的点击事件监听器保存在私有内部类 View.ListenerInfo.mOnClickListener
 * 2. 通过反射调用私有方法 View.getListenerInfo() 获取 ListenerInfo 实例
 * 3. 获取 mOnClickListener 字段并取出原始的 View.OnClickListener 对象
 * 4. 使用 Proxy.newProxyInstance 动态代理包装原始 Listener，插入埋点/拦截逻辑
 * 5. 将代理对象反射写回 ListenerInfo.mOnClickListener
 *
 * 适用场景：
 * - 埋点统计：统一拦截点击事件，记录用户行为
 * - 防重放：防止按钮重复点击
 * - AOP 编程：面向切面编程，解耦业务逻辑
 *
 * 注意事项：
 * - 反射调用有性能损耗，不适合高频调用
 * - Android 高版本可能限制反射访问私有 API
 * - 仅用于学习和研究，生产环境慎用
 */
@Route(path = RouterPath.Sample.Hook)
class HookActivity : BasicResponseActivity() {

    private lateinit var targetDemoView: View
    private var originalClickListener: View.OnClickListener? = null
    private var isHooked = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示基于反射与动态代理 Hook View 的 OnClickListener")
        setupDemoTarget()
    }

    private fun setupDemoTarget() {
        targetDemoView = View(this)
        originalClickListener = View.OnClickListener {
            appendLog("【原始事件】TargetView.OnClickListener.onClick() 执行成功")
        }
        targetDemoView.setOnClickListener(originalClickListener)
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 触发原始点击事件（performClick）",
            "2. 执行反射 Hook（动态代理拦截点击）",
            "3. 再次触发点击事件（验证 Hook 拦截）",
            "4. 还原 Hook（恢复原始 Listener）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                appendLog("【触发点击】准备调用 targetDemoView.performClick()...")
                targetDemoView.performClick()
            }

            1 -> {
                applyHook(targetDemoView)
            }

            2 -> {
                appendLog("【触发点击】准备调用已 Hook 的 targetDemoView.performClick()...")
                targetDemoView.performClick()
            }

            3 -> {
                restoreHook(targetDemoView)
            }
        }
    }

    private fun applyHook(view: View) {
        val success = HookHelper.hookOnClickListener(view) {
            appendLog("【Hook 拦截】>>> 成功拦截到 onClick() 调用！可在此处做埋点或防重放校验 <<<")
        }
        if (success) {
            isHooked = true
            appendLog("【Hook 成功】已使用 Proxy 代理对象替换 ListenerInfo.mOnClickListener")
        } else {
            appendLog("【Hook 失败】未找到原始 OnClickListener 或反射失败")
        }
    }

    private fun restoreHook(view: View) {
        if (!isHooked) {
            appendLog("【还原提示】当前未处于 Hook 状态")
            return
        }
        HookHelper.restoreOnClickListener(view, originalClickListener)
        isHooked = false
        appendLog("【Hook 还原】已恢复原始 OnClickListener")
    }
}
