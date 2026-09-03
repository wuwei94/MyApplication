package com.example.william.my.core.base.coroutine

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 在 [LifecycleOwner]（如 Activity / Fragment）处于指定生命周期状态 [state] 时启动并安全执行协程块。
 * 当生命周期低于该状态时会自动取消挂起，重新进入时会重新启动。
 */
inline fun LifecycleOwner.launchAndRepeatWithLifecycle(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit,
): Job = lifecycleScope.launch {
    repeatOnLifecycle(state) {
        block()
    }
}

/**
 * 安全收集 Flow 的扩展函数，生命周期低于 [state] 时自动停止收集，重新进入时恢复。
 */
inline fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend (value: T) -> Unit,
): Job = lifecycleOwner.launchAndRepeatWithLifecycle(state) {
    collect { action(it) }
}

/**
 * 安全收集 Flow 最新值的扩展函数（新值到来时取消前一个值的收集处理）
 */
inline fun <T> Flow<T>.collectLatestWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend (value: T) -> Unit,
): Job = lifecycleOwner.launchAndRepeatWithLifecycle(state) {
    collectLatest { action(it) }
}
