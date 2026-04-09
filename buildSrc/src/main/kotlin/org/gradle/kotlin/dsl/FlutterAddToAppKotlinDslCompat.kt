package org.gradle.kotlin.dsl

import com.example.william.my.gradle.FlutterAddToAppSdkDefaults

private val defaultFlutterAddToAppSdkDefaults = FlutterAddToAppSdkDefaults()

private fun Any?.toFlutterAddToAppSdkDefaults(): FlutterAddToAppSdkDefaults = when (this) {
    is FlutterAddToAppSdkDefaults -> this
    else -> defaultFlutterAddToAppSdkDefaults
}

/**
 * Flutter add-to-app source integration still pulls in some plugin
 * `build.gradle.kts` files that reference `flutter.compileSdkVersion`.
 *
 * In that situation the generated Kotlin DSL accessor is typed as `Any`, so we
 * expose the expected SDK properties here instead of patching pub-cache files.
 */
@Suppress("unused")
val Any.compileSdkVersion: Int
    get() = toFlutterAddToAppSdkDefaults().compileSdkVersion

@Suppress("unused")
val Any.minSdkVersion: Int
    get() = toFlutterAddToAppSdkDefaults().minSdkVersion

@Suppress("unused")
val Any.targetSdkVersion: Int
    get() = toFlutterAddToAppSdkDefaults().targetSdkVersion

@Suppress("unused")
val Any.ndkVersion: String
    get() = toFlutterAddToAppSdkDefaults().ndkVersion
