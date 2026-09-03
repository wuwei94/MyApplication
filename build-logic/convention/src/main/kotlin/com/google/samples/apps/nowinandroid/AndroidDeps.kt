/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * 为所有 Android 模块配置基础依赖（核心协程与基础单元测试）
 */
internal fun Project.configureDepsAndroid(
    commonExtension: CommonExtension,
) {
    commonExtension.apply {
        dependencies {
            "implementation"(libs.findLibrary("kotlinx-coroutines-core").get())
            "implementation"(libs.findLibrary("kotlinx-coroutines-android").get())

            "testImplementation"(libs.findLibrary("junit").get())

            if (projectDir.resolve("src/androidTest").exists()) {
                "androidTestImplementation"(libs.findLibrary("androidx-test-ext").get())
                "androidTestImplementation"(libs.findLibrary("androidx-test-espresso").get())
            }
        }
    }
}

/**
 * 动态为 App 入口模块注入所有 :modules: 功能模块
 */
internal fun Project.configureFeatureAndroid(
    commonExtension: CommonExtension,
) {
    commonExtension.apply {
        dependencies {
            rootProject.subprojects.forEach { subproject ->
                if (subproject.path.startsWith(":modules:module_")) {
                    "implementation"(project(subproject.path))
                }
            }
        }
    }
}
