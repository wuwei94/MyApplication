package com.example.william.my.module.arch.mavericks.counter.viewmodel

import com.airbnb.mvrx.MavericksViewModel
import com.example.william.my.module.arch.mavericks.counter.data.CounterState

/**
 * 计数器 ViewModel
 *
 * 承载计数器的业务逻辑与状态变更。
 */
class CounterViewModel(initialState: CounterState) :
    MavericksViewModel<CounterState>(initialState) {
    fun incrementCount() = setState { copy(count = count + 1) }
}
