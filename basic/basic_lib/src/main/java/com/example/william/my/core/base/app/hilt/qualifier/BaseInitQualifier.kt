package com.example.william.my.core.base.app.hilt.qualifier

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseInit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppInit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EventInit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MavericksInit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LoadSirInit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FlutterInit
