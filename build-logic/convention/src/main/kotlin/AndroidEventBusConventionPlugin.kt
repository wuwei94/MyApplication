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

import com.google.samples.apps.nowinandroid.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import java.util.Locale

/**
 * EventBus 事件总线约定插件
 *
 * 按模块名生成 EventBus 索引并注入依赖。
 *
 * 注意：EventBus 3.3.1 已停更，`eventbus-annotation-processor` 无 KSP 处理器，
 * 故此处仍使用 kapt。AGP 10.0（builtInKotlin 强制）迁移前需处理——
 * 可选方案：换 FlowEventBus / LiveEventBus，或去掉编译期索引退化为运行时反射。
 */
class AndroidEventBusConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // EventBus 3.3.1 无官方 KSP，仅 kapt（详见类注释）
            apply(plugin = "nowinandroid.android.kapt")
            extensions.configure<KaptExtension> {
                val eventBusClassPath = "com.example.william.my" +
                    project.path.replace(":", ".")
                val eventBusClassName = "My" +
                    project.name.substringAfter("_", project.name)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + "EventBusIndex"
                println("eventBusClassPath : $eventBusClassPath")
                println("eventBusClassName : $eventBusClassName")
                arguments {
                    arg("eventBusIndex", "$eventBusClassPath.$eventBusClassName")
                }
            }
            dependencies {
                "implementation"(libs.findLibrary("eventbus").get())
                "kapt"(libs.findLibrary("eventbus.processor").get())
            }
        }
    }
}
