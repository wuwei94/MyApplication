/*
 * Copyright 2023 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Project

/**
 * 若 [project] 没有 `androidTest` 目录，则禁用不必要的 Android 仪器化测试。
 * 否则这些项目会被编译、打包、安装并运行，最终只得到如下信息：
 *
 * > Starting 0 tests on AVD
 *
 * 注意：可通过检查基于 buildTypes 与 flavors 的其他潜在 sourceSets 来进一步改进。
 */
internal fun LibraryAndroidComponentsExtension.disableUnnecessaryAndroidTests(
    project: Project,
) = beforeVariants {
    it.enableAndroidTest = it.enableAndroidTest &&
        project.projectDir.resolve("src/androidTest").exists()
}
