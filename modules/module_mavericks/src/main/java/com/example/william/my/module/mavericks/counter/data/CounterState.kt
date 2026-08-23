package com.example.william.my.module.mavericks.counter.data

import com.airbnb.mvrx.MavericksState

/**
 * 计数器不可变状态
 */
data class CounterState(val count: Int = 0) : MavericksState
