package com.example.william.my.core.base.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference

/**
 * Handler 弱引用工具类
 *
 * 演示 Java 时代使用静态内部类 + 弱引用（WeakReference）防止 Handler 引起 Activity 内存泄漏的经典设计模式。
 */
@Deprecated(
    message = "仅作为弱引用防内存泄漏示例保留。现代开发推荐使用 Kotlin 协程生命周期调度，或使用 Blankj 的 ThreadUtils.getMainHandler()",
    replaceWith = ReplaceWith("ThreadUtils.getMainHandler()", "com.blankj.utilcode.util.ThreadUtils"),
)
object HandlerUtils {

    class HandlerHolder : Handler {

        private val weakReference: WeakReference<OnReceiveMessageHandler>

        /**
         * 使用必读：推荐在Activity或者Activity内部持有类中实现该接口，不要使用匿名类，可能会被GC
         *
         * @param handler 收到消息回调接口
         */
        @Suppress("DEPRECATION")
        constructor(handler: OnReceiveMessageHandler) {
            weakReference = WeakReference(handler)
        }

        constructor(looper: Looper, handler: OnReceiveMessageHandler) : super(looper) {
            weakReference = WeakReference(handler)
        }

        override fun handleMessage(msg: Message) {
            weakReference.get()?.handlerMessage(msg)
        }
    }

    /**
     * 收到消息回调接口
     */
    interface OnReceiveMessageHandler {
        /**
         * 处理消息
         *
         * @param msg
         */
        fun handlerMessage(msg: Message)
    }
}
