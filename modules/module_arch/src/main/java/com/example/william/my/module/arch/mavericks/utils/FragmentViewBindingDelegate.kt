package com.example.william.my.module.arch.mavericks.utils

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Fragment ViewBinding 属性委托类
 *
 * 自动管理 Fragment ViewBinding 的生命周期，在视图销毁时自动解绑释放。
 */
class FragmentViewBindingDelegate<T : ViewBinding>(
    bindingClass: Class<T>,
    private val fragment: Fragment
) : ReadOnlyProperty<Fragment, T> {
    private val clearBindingHandler by lazy(LazyThreadSafetyMode.NONE) { Handler(Looper.getMainLooper()) }
    private var binding: T? = null

    private val bindMethod = bindingClass.getMethod("bind", View::class.java)

    init {
        fragment.lifecycleScope.launch {
            fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onDestroy(owner: LifecycleOwner) {
                        // 生命周期监听器在 Fragment 的 onDestroyView 之前被调用。
                        // 为了让视图在 onDestroyView 中仍能访问 binding 执行清理操作，延迟一帧置空引用。
                        clearBindingHandler.post { binding = null }
                    }
                })
            }
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        // onCreateView 可能在 onDestroyView 与下一个主线程循环之间被调用。
        // 此时 binding 可能指向前一个视图，需检查 root view 是否与当前一致。
        if (binding != null && binding?.root !== thisRef.view) {
            binding = null
        }
        binding?.let { return it }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            error("无法访问 ViewBinding，当前视图生命周期状态为 ${lifecycle.currentState}！")
        }

        @Suppress("UNCHECKED_CAST")
        binding = bindMethod.invoke(null, thisRef.requireView()) as T
        return binding!!
    }
}

/**
 * Fragment 的 ViewBinding 委托扩展函数
 *
 * 用法：
 * ```kotlin
 * private val binding: FragmentLayoutBinding by viewBinding()
 * ```
 */
inline fun <reified T : ViewBinding> Fragment.viewBinding() =
    FragmentViewBindingDelegate(T::class.java, this)