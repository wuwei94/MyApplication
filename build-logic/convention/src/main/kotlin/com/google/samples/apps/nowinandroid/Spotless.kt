/*
 * Copyright 2026 The Android Open Source Project
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

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/**
 * 为 Android Library 与 Application 模块配置 Spotless 代码风格检查
 */
internal fun Project.configureSpotlessForAndroid() {
    configureSpotlessCommon()
}

/**
 * 为纯 JVM 模块（如 basic_model）配置 Spotless 代码风格检查
 */
internal fun Project.configureSpotlessForJvm() {
    configureSpotlessCommon()
}

/**
 * 为根工程配置 Spotless 检查，纳管 build-logic convention 源码与根目录 KTS 脚本
 */
internal fun Project.configureSpotlessForRootProject() {
    apply(plugin = "com.diffplug.spotless")
    extensions.configure<SpotlessExtension> {
        lineEndings = LineEnding.UNIX
        kotlin {
            target("build-logic/convention/src/**/*.kt")
            targetExclude("**/build/**")
            ktlint(libs.findVersion("ktlint").get().requiredVersion).editorConfigOverride(
                mapOf(
                    "android" to "true",
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                ),
            )
            trimTrailingWhitespace()
            endWithNewline()
        }
        format("kts") {
            target("*.kts")
            target("build-logic/*.kts")
            target("build-logic/convention/*.kts")
            targetExclude("**/build/**")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}

/**
 * 通用 Spotless 规则配置：统一换行符为 LF、配置 ktlint 规则并抑制包名下划线校验
 */
private fun Project.configureSpotlessCommon() {
    apply(plugin = "com.diffplug.spotless")
    extensions.configure<SpotlessExtension> {
        lineEndings = LineEnding.UNIX
        kotlin {
            target("src/**/*.kt")
            targetExclude("**/build/**")
            ktlint(libs.findVersion("ktlint").get().requiredVersion).editorConfigOverride(
                mapOf(
                    "android" to "true",
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                    // 本项目允许 module 名包含下划线（如 :basic:basic_repo），package 路径随之带下划线；
                    // ktlint 默认禁用的 package-name 规则会因此拦截全工程校验，故显式禁用。
                    "ktlint_standard_package-name" to "disabled",
                ),
            )
            trimTrailingWhitespace()
            endWithNewline()
        }
        format("kts") {
            target("*.kts")
            targetExclude("**/build/**")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
