package com.example.william.my.module.event.event

/** 全局普通事件 */
data class GlobalEvent(val message: String)

/** Activity 作用域事件 */
data class ActivityEvent(val message: String)

/** 粘性事件 */
data class StickyEvent(val message: String)