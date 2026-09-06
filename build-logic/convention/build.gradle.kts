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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    //alias(libs.plugins.android.lint)
}

group = "com.google.samples.apps.nowinandroid.buildlogic"

// 将 build-logic 插件编译目标设为 JDK 17
// 与构建项目所用的 JDK 一致，与设备上运行的 JDK 无关。
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.gradlePlugin.android)
    compileOnly(libs.gradlePlugin.kotlin)
    compileOnly(libs.gradlePlugin.compose)
    compileOnly(libs.gradlePlugin.protobuf)

    compileOnly(libs.gradlePlugin.objectbox)

    compileOnly(libs.gradlePlugin.ksp)
    compileOnly(libs.gradlePlugin.hilt)
    compileOnly(libs.gradlePlugin.room)
    compileOnly(libs.gradlePlugin.spotless)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        // =========================================================================
        // 1. 基础模块插件 (Base Module Plugins)
        // =========================================================================
        register("androidApplication") {
            id = libs.plugins.nowinandroid.android.application.asProvider().get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = libs.plugins.nowinandroid.android.application.compose.get().pluginId
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.nowinandroid.android.library.asProvider().get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = libs.plugins.nowinandroid.android.library.compose.get().pluginId
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.nowinandroid.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }

        // =========================================================================
        // 2. 业务功能模块插件 (Feature Module Plugins)
        // =========================================================================
        register("androidFeature") {
            id = libs.plugins.nowinandroid.android.feature.asProvider().get().pluginId
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidFeatureCompose") {
            id = libs.plugins.nowinandroid.android.feature.compose.get().pluginId
            implementationClass = "AndroidFeatureComposeConventionPlugin"
        }

        // =========================================================================
        // 3. 架构与中间件插件 (Architecture & Framework Plugins)
        // =========================================================================
        register("androidHilt") {
            id = libs.plugins.nowinandroid.android.hilt.get().pluginId
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidARouter") {
            id = libs.plugins.nowinandroid.android.arouter.get().pluginId
            implementationClass = "AndroidARouterConventionPlugin"
        }
        register("androidEventBus") {
            id = libs.plugins.nowinandroid.android.eventbus.get().pluginId
            implementationClass = "AndroidEventBusConventionPlugin"
        }
        register("androidKsp") {
            id = libs.plugins.nowinandroid.android.ksp.get().pluginId
            implementationClass = "AndroidKspConventionPlugin"
        }

        // =========================================================================
        // 4. 数据存储与持久化插件 (Data & Persistence Plugins)
        // =========================================================================
        register("androidRoom") {
            id = libs.plugins.nowinandroid.android.room.get().pluginId
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidProtobuf") {
            id = libs.plugins.nowinandroid.android.protobuf.get().pluginId
            implementationClass = "AndroidProtobufConventionPlugin"
        }
        register("androidObjectBox") {
            id = libs.plugins.nowinandroid.android.objectbox.get().pluginId
            implementationClass = "AndroidObjectBoxConventionPlugin"
        }
        register("androidGreenDao") {
            id = libs.plugins.nowinandroid.android.greendao.get().pluginId
            implementationClass = "AndroidGreenDaoConventionPlugin"
        }

        // =========================================================================
        // 5. 质量分析与测试插件 (Quality & Testing Plugins)
        // =========================================================================
        register("androidLint") {
            id = libs.plugins.nowinandroid.android.lint.get().pluginId
            implementationClass = "AndroidLintConventionPlugin"
        }
        register("androidTest") {
            id = libs.plugins.nowinandroid.android.test.get().pluginId
            implementationClass = "AndroidTestConventionPlugin"
        }

        // =========================================================================
        // 6. 根工程管理插件 (Root Management Plugin: Graph & Spotless)
        // =========================================================================
        register("root") {
            id = libs.plugins.nowinandroid.root.get().pluginId
            implementationClass = "RootPlugin"
        }
    }
}
