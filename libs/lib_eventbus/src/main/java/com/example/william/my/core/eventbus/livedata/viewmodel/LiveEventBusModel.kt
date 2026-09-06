package com.example.william.my.core.eventbus.livedata.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.william.my.core.eventbus.livedata.livedata.BusMutableLiveData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * LiveData 事件总线 ViewModel（维护普通事件与粘性事件）
 */
class LiveEventBusModel : ViewModel() {

    // 正常事件
    private val eventLiveData: MutableMap<String, BusMutableLiveData<Any>> = HashMap()

    // 粘性事件
    private val stickyEventLiveData: MutableMap<String, MutableLiveData<Any>> = HashMap()

    private fun getEventLiveData(eventName: String, isSticky: Boolean): MutableLiveData<Any> = if (isSticky) {
        stickyEventLiveData[eventName] ?: MutableLiveData<Any>().also {
            stickyEventLiveData[eventName] = it
        }
    } else {
        eventLiveData[eventName] ?: BusMutableLiveData<Any>().also {
            eventLiveData[eventName] = it
        }
    }

    fun <T : Any> observeEvent(
        owner: LifecycleOwner,
        eventName: String,
        isSticky: Boolean = false,
        onReceived: (T) -> Unit,
    ): Job = owner.lifecycleScope.launch {
        getEventLiveData(eventName, isSticky).observe(
            owner,
            Observer { value ->
                invokeReceived(value, onReceived)
            },
        )
    }

    fun postEvent(eventName: String, value: Any) {
        listOfNotNull(getEventLiveData(eventName, false), getEventLiveData(eventName, true))
            .forEach { livedata ->
                livedata.value = value
            }
    }

    fun removeStickEvent(eventName: String) {
        stickyEventLiveData.remove(eventName)
    }

    private fun <T : Any> invokeReceived(value: Any, onReceived: (T) -> Unit) {
        try {
            onReceived.invoke(value as T)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
