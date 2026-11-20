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
import com.google.samples.apps.nowinandroid.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Feature 实现层（Impl）约定插件
 *
 * 组合 library 与 Hilt，自动注入 basic_lib、basic_shared 与生命周期依赖，
 * 不再默认绑定 ARouter，配合 Hilt 多重绑定与 API-Impl 契约隔离使用。
 */
class AndroidFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "nowinandroid.android.library")
            apply(plugin = "nowinandroid.android.hilt")

            extensions.configure<LibraryExtension> {
                testOptions.animationsDisabled = true
            }

            dependencies {
                "implementation"(project(":basic:basic_lib"))
                "implementation"(project(":basic:basic_shared"))

                "implementation"(libs.findLibrary("androidx-lifecycle-runtime").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel").get())
            }
        }
    }
}
