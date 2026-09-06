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

import androidx.room.gradle.RoomExtension
import com.google.devtools.ksp.gradle.KspExtension
import com.google.samples.apps.nowinandroid.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import java.io.File

/**
 * Room 数据库约定插件
 *
 * 配置 schema 目录与 KAPT 参数并注入 Room 依赖。
 */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "androidx.room")
            apply(plugin = "nowinandroid.android.ksp")
            extensions.configure<RoomExtension> {
                // schemas 目录包含每个版本 Room 数据库的 schema 文件。
                // 这是启用 Room 自动迁移所必需的。
                // 见 https://developer.android.com/reference/kotlin/androidx/room/AutoMigration
                schemaDirectory("$projectDir/schemas")
            }
            extensions.configure<KspExtension> {
                // 生成 Kotlin 代码（替代 kapt 的 Java 存根）
                arg("room.generateKotlin", "true")
                // schemas 目录包含每个版本 Room 数据库的 schema 文件。
                arg("room.schemaLocation", File(projectDir, "schemas").absolutePath)
            }
            dependencies {
                "implementation"(libs.findLibrary("androidx.room").get())
                "implementation"(libs.findLibrary("androidx.room.ktx").get())
                "implementation"(libs.findLibrary("androidx.room.rxjava3").get())
                "implementation"(libs.findLibrary("androidx.room.paging").get())
                "ksp"(libs.findLibrary("androidx.room.compiler").get())
            }
        }
    }
}
