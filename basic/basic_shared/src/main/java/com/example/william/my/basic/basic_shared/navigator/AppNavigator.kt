package com.example.william.my.basic.basic_shared.navigator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.app.ActivityOptionsCompat
import com.blankj.utilcode.util.ActivityUtils

/**
 * 自研轻量级页面导航器
 *
 * 替代第三方字节码路由框架，通过 [AppPageRegistry] 解析目标 Activity，
 * 经由 [PageInterceptor] 责任链处理后发起原生 Intent 跳转。
 */
class AppNavigator private constructor(private val request: PageRequest) {

    fun withString(key: String, value: String?): AppNavigator {
        request.extras.putString(key, value)
        return this
    }

    fun withInt(key: String, value: Int): AppNavigator {
        request.extras.putInt(key, value)
        return this
    }

    fun withBoolean(key: String, value: Boolean): AppNavigator {
        request.extras.putBoolean(key, value)
        return this
    }

    fun withParcelable(key: String, value: Parcelable?): AppNavigator {
        request.extras.putParcelable(key, value)
        return this
    }

    fun addFlags(flags: Int): AppNavigator {
        request.flags = request.flags or flags
        return this
    }

    fun withOptions(options: ActivityOptionsCompat?): AppNavigator {
        request.options = options
        return this
    }

    fun navigation(context: Context? = null): Boolean {
        val targetContext = context ?: ActivityUtils.getTopActivity() ?: return false
        val interceptors = AppPageRegistry.getInterceptors()
        var navigated = false

        val chain = RealInterceptorChain(
            context = targetContext,
            interceptors = interceptors,
            index = 0,
            request = request,
            onFinalProceed = { finalRequest ->
                navigated = launchActivity(targetContext, finalRequest)
            },
        )
        chain.proceed(request)
        return navigated
    }

    private fun launchActivity(context: Context, finalRequest: PageRequest): Boolean {
        val targetClass = AppPageRegistry.resolveActivity(finalRequest.path) ?: return false
        val intent = Intent(context, targetClass).apply {
            putExtras(finalRequest.extras)
            if (finalRequest.flags != 0) {
                addFlags(finalRequest.flags)
            }
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent, finalRequest.options?.toBundle())
        return true
    }

    private class RealInterceptorChain(
        override val context: Context,
        private val interceptors: List<PageInterceptor>,
        private val index: Int,
        override val request: PageRequest,
        private val onFinalProceed: (PageRequest) -> Unit,
    ) : PageInterceptor.Chain {

        override fun proceed(request: PageRequest) {
            if (index < interceptors.size) {
                val next = RealInterceptorChain(
                    context = context,
                    interceptors = interceptors,
                    index = index + 1,
                    request = request,
                    onFinalProceed = onFinalProceed,
                )
                interceptors[index].intercept(next)
            } else {
                onFinalProceed(request)
            }
        }
    }

    companion object {
        fun build(path: String): AppNavigator = AppNavigator(PageRequest(path = path))
    }
}
