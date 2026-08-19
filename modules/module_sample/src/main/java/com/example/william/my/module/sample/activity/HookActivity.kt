package com.example.william.my.module.sample.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.lang.reflect.Proxy

/**
 * 反射 Hook OnClickListener 机制演示
 *
 * 原理：
 * 1. Android 中 View 的点击事件监听器保存在私有内部类 `View.ListenerInfo.mOnClickListener`。
 * 2. 通过反射调用私有方法 `View.getListenerInfo()` 获取 `ListenerInfo` 实例。
 * 3. 获取 `mOnClickListener` 字段并取出原始的 `View.OnClickListener` 对象。
 * 4. 使用 `Proxy.newProxyInstance` 动态代理包装原始 Listener，插入埋点/拦截逻辑。
 * 5. 将代理对象反射写回 `ListenerInfo.mOnClickListener`。
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

    @SuppressLint("PrivateApi,DiscouragedPrivateApi")
    private fun applyHook(view: View) {
        try {
            // 1. 反射获取 ListenerInfo
            val getListenerInfo = View::class.java.getDeclaredMethod("getListenerInfo").apply {
                isAccessible = true
            }
            val listenerInfo = getListenerInfo.invoke(view)

            // 2. 获取 ListenerInfo 中的 mOnClickListener 字段
            val listenerInfoClz = Class.forName("android.view.View\$ListenerInfo")
            val field = listenerInfoClz.getDeclaredField("mOnClickListener").apply {
                isAccessible = true
            }
            val currentListener = field.get(listenerInfo) as? View.OnClickListener

            if (currentListener == null) {
                appendLog("【Hook 失败】未找到原始 OnClickListener")
                return
            }

            // 3. 动态代理包装
            val proxy = Proxy.newProxyInstance(
                classLoader,
                arrayOf(View.OnClickListener::class.java)
            ) { _, method, args ->
                appendLog("【Hook 拦截】>>> 成功拦截到 onClick() 调用！可在此处做埋点或防重放校验 <<<")
                if (args != null) {
                    method.invoke(currentListener, *args)
                } else {
                    method.invoke(currentListener)
                }
            } as View.OnClickListener

            // 4. 写回代理对象
            field.set(listenerInfo, proxy)
            isHooked = true
            appendLog("【Hook 成功】已使用 Proxy 代理对象替换 ListenerInfo.mOnClickListener")
        } catch (e: Exception) {
            appendLog("【Hook 异常】${e.message}")
        }
    }

    @SuppressLint("PrivateApi,DiscouragedPrivateApi")
    private fun restoreHook(view: View) {
        if (!isHooked) {
            appendLog("【还原提示】当前未处于 Hook 状态")
            return
        }
        view.setOnClickListener(originalClickListener)
        isHooked = false
        appendLog("【Hook 还原】已恢复原始 OnClickListener")
    }
}
