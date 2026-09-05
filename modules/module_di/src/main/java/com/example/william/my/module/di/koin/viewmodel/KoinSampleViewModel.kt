package com.example.william.my.module.di.koin.viewmodel

import androidx.lifecycle.ViewModel
import com.example.william.my.module.di.koin.model.KoinAnalyticsTracker

/**
 * 演示 Koin 注入的 ViewModel
 */
class KoinSampleViewModel(
    private val tracker: KoinAnalyticsTracker,
) : ViewModel() {

    private var counter = 0

    fun incrementAndGet(): Int {
        counter++
        return counter
    }

    fun getViewModelInfo(): String = "KoinSampleViewModel: ${tracker.logEvent("vm_counter_updated")}"
}
