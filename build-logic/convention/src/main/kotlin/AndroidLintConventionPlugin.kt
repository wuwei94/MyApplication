/*
 * Copyright 2022 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/**
 * Lint 约定插件
 *
 * 注入自定义 Lint 规则并开启 XML 与 SARIF 报告。
 */
class AndroidLintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // 自定义 Lint 规则模块（:lint）以 lintChecks 依赖配置注入各 Android 模块，
            // 使 TestNamingDetector 等规则在 lint 任务中真实生效。
            // 注意：lintChecks 是 dependencies 的配置名，不是 Lint DSL 的方法，必须走 dependencies.add。
            val lintProject = project(":lint")
            val projectPath = path
            when {
                pluginManager.hasPlugin("com.android.application") -> {
                    configure<ApplicationExtension> { lint { configure() } }
                    if (projectPath != ":lint") dependencies.add("lintChecks", lintProject)
                }

                pluginManager.hasPlugin("com.android.library") -> {
                    configure<LibraryExtension> { lint { configure() } }
                    if (projectPath != ":lint") dependencies.add("lintChecks", lintProject)
                }

                else -> {
                    apply(plugin = "com.android.lint")
                    configure<Lint> { configure() }
                    if (projectPath != ":lint") dependencies.add("lintChecks", lintProject)
                }
            }
        }
    }
}

private fun Lint.configure() {
    xmlReport = true
    sarifReport = true
    // 多模块工程配合 pre-push 增量门禁，各模块独立负责自身的静态代码质量。
    // 关闭 checkDependencies（AGP 默认即为 false），避免：
    // 1. 各 feature 模块反复重复穿透扫描底层基础库，严重拖慢本地与 CI 构建效率；
    // 2. 将 Flutter Add-to-App 动态挂载的第三方插件源码子工程（如 geolocator_android）
    //    误当作第一方源码扫描，导致外部不可控的未抑制告警异常阻断构建。
    checkDependencies = false
    disable += "GradleDependency"
}
