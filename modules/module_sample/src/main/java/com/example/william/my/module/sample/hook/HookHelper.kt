package com.example.william.my.module.sample.hook

import android.annotation.SuppressLint
import android.view.View
import java.lang.reflect.Proxy

/**
 * View 事件反射 Hook 辅助工具类
 *
 * 提供 View.OnClickListener 的动态代理 Hook 与还原能力：
 * 1. hookOnClickListener：反射读取 View.ListenerInfo.mOnClickListener 并使用 Proxy 动态代理拦截
 * 2. restoreOnClickListener：恢复原始 Listener
 */
object HookHelper {

    /**
     * Hook View 的 OnClickListener 点击事件
     *
     * @param view 目标 View
     * @param onIntercept 点击被拦截时的回调（可用于插入埋点、防重放等切面逻辑）
     * @return 是否 Hook 成功
     */
    @SuppressLint("PrivateApi,DiscouragedPrivateApi")
    fun hookOnClickListener(
        view: View,
        onIntercept: ((v: View) -> Unit)? = null
    ): Boolean {
        return try {
            // 1. 反射获取 ListenerInfo 实例
            val getListenerInfo = View::class.java.getDeclaredMethod("getListenerInfo").apply {
                isAccessible = true
            }
            val listenerInfo = getListenerInfo.invoke(view) ?: return false

            // 2. 获取 ListenerInfo 中的 mOnClickListener 字段与原始 Listener
            val listenerInfoClz = Class.forName("android.view.View\$ListenerInfo")
            val field = listenerInfoClz.getDeclaredField("mOnClickListener").apply {
                isAccessible = true
            }
            val currentListener = field.get(listenerInfo) as? View.OnClickListener ?: return false

            // 3. 动态代理包装原始 OnClickListener
            val proxy = Proxy.newProxyInstance(
                view.context.classLoader,
                arrayOf(View.OnClickListener::class.java)
            ) { _, method, args ->
                onIntercept?.invoke(view)
                if (args != null) {
                    method.invoke(currentListener, *args)
                } else {
                    method.invoke(currentListener)
                }
            } as View.OnClickListener

            // 4. 写回代理对象
            field.set(listenerInfo, proxy)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 还原 View 的原始 OnClickListener
     */
    fun restoreOnClickListener(view: View, originalListener: View.OnClickListener?) {
        view.setOnClickListener(originalListener)
    }
}
