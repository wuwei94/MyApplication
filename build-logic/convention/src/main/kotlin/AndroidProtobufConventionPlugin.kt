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

import com.google.protobuf.gradle.ProtobufExtension
import com.google.samples.apps.nowinandroid.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Protobuf 约定插件
 *
 * 配置 protoc 编译器并依赖 lite 运行时。
 */
class AndroidProtobufConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.google.protobuf")
            // Protobuf Gradle 插件，见 https://github.com/google/protobuf-gradle-plugin
            extensions.configure<ProtobufExtension> {
                protoc {
                    // Protobuf 编译器的构件坐标
                    artifact = "com.google.protobuf:protoc:3.24.0"
                }
                generateProtoTasks {
                    all().forEach { task ->
                        task.builtins {
                            register("java") {
                                option("lite")
                            }
                        }
                    }
                }
            }
            dependencies {
                // 需依赖 lite 运行时库，而非 protobuf-java
                "implementation"(libs.findLibrary("google.protobuf.javalite").get())
            }
        }
    }
}
