package com.example.william.my.module.eventbus.event

data class GlobalEvent(val message: String)

data class ActivityEvent(val message: String)

data class StickyEvent(val message: String)