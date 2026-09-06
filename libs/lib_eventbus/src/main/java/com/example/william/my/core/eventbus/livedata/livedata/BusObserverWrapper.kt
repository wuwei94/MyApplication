package com.example.william.my.core.eventbus.livedata.livedata

import androidx.lifecycle.Observer

/**
 * LiveData 观察者包装类（用于 observeForever 时拦截重复回调）
 */
class BusObserverWrapper<T>(private val observer: Observer<in T>) : Observer<T> {

    override fun onChanged(t: T) {
        if (isCallOnObserve) {
            return
        }
        observer.onChanged(t)
    }

    private val isCallOnObserve: Boolean
        get() {
            val stackTrace = Thread.currentThread().stackTrace
            if (stackTrace.isNotEmpty()) {
                for (element in stackTrace) {
                    if ("android.arch.lifecycle.LiveData" == element.className && "observeForever" == element.methodName) {
                        return true
                    }
                }
            }
            return false
        }
}
