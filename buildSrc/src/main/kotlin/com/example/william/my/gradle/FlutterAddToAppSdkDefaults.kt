package com.example.william.my.gradle

/**
 * Mirrors the SDK defaults exposed by Flutter's Gradle plugin.
 *
 * We keep this in buildSrc because some add-to-app plugin subprojects use
 * Kotlin DSL and still reference `flutter.compileSdkVersion` style properties.
 */
data class FlutterAddToAppSdkDefaults(
    val compileSdkVersion: Int = 36,
    val minSdkVersion: Int = 24,
    val targetSdkVersion: Int = 36,
    val ndkVersion: String = "28.2.13676358",
)
