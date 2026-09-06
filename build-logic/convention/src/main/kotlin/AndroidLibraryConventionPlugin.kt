/*
 * Copyright 2022 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.google.samples.apps.nowinandroid.configureDepsAndroid
import com.google.samples.apps.nowinandroid.configureFlavors
import com.google.samples.apps.nowinandroid.configureKotlinAndroid
import com.google.samples.apps.nowinandroid.configurePrintApksTask
import com.google.samples.apps.nowinandroid.configureSpotlessForAndroid
import com.google.samples.apps.nowinandroid.disableUnnecessaryAndroidTests
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/**
 * Android 库模块约定插件
 *
 * 统一配置编译 SDK、Kotlin、flavor 与基础依赖。
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            apply(plugin = "kotlin-android")
            apply(plugin = "kotlin-parcelize")
            apply(plugin = "nowinandroid.android.lint")
            configureSpotlessForAndroid()
            extensions.configure<LibraryExtension> {
                compileSdk = 37
                defaultConfig.minSdk = 24
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                if (file("consumer-rules.pro").exists()) {
                    defaultConfig.consumerProguardFiles("consumer-rules.pro")
                }
                testOptions.animationsDisabled = true
                configureKotlinAndroid(this)
                configureFlavors(this)
                configureDepsAndroid(this)
                // 资源前缀由模块名派生，
                // 因此 ":core:module1" 内的资源必须以 "core_module1_" 为前缀
                // resourcePrefix =
                //    path.split("""\W""".toRegex()).drop(1).distinct().joinToString(separator = "_")
                //        .lowercase() + "_"
            }
            extensions.configure<LibraryAndroidComponentsExtension> {
                configurePrintApksTask(this)
                disableUnnecessaryAndroidTests(target)
            }
        }
    }
}
