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

/**
 * GreenDAO 数据库约定插件（保留模板）
 *
 * 【设计说明】
 * 此插件及其依赖配置（包含 libs.versions.toml 中的依赖与插件声明）特意保留完整结构，
 * 作为现代 Gradle 多模块工程中集成 GreenDAO 的标准配置模板与最佳实践样例，
 * 方便后续在其他项目或需要接入 GreenDAO 的项目中直接参考挪用（包含 GreendaoOptions
 * 参数配置、代码生成目录指定、以及针对 Kotlin / kapt 编译任务的 dependsOn 依赖编排）。
 *
 * 在本工程中若需重新激活，取消内部集成代码注释并将插件应用到目标模块即可。
 */
class AndroidGreenDaoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
//            apply(plugin = "org.greenrobot.greendao")
//            extensions.configure<GreendaoOptions> {
//                schemaVersion = 1
//                daoPackage = "com.example.william.my.module.database.greendao.dao"
//                targetGenDir("src/main/java")
//            }
//            dependencies {
//                "implementation"(libs.findLibrary("greendao").get())
//            }
//            tasks.configureEach {
//                if (name.matches(Regex("\\w*compile\\w*Kotlin"))) {
//                    dependsOn("greendao")
//                }
//                if (name.matches(Regex("\\w*kaptGenerateStubs\\w*Kotlin"))) {
//                    dependsOn("greendao")
//                }
//                if (name.matches(Regex("\\w*kapt\\w*Kotlin"))) {
//                    dependsOn("greendao")
//                }
//            }
        }
    }
}
