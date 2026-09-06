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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

/**
 * KAPT 注解处理约定插件
 *
 * 统一应用 KAPT 插件并设置全局默认配置（correctErrorTypes=true，允许引用生成的代码），
 * 供 ARouter / EventBus / Hilt / Room 等需要注解处理的约定插件复用，
 * 避免在基础 Library / Application 插件中无条件引入 KAPT。
 */
class AndroidKaptConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "kotlin-kapt")
            extensions.configure<KaptExtension> {
                // 允许引用生成的代码
                correctErrorTypes = true
            }
        }
    }
}
