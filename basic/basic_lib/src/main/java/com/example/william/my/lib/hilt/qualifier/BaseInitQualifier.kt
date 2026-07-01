package com.example.william.my.lib.hilt.qualifier

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseInit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppInit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ArchInit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EventInit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenSourceInit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FlutterInit