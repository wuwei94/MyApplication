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
 * Configure base Dependencies with Android options
 */
internal fun Project.configureDepsAndroid(commonExtension: CommonExtension) {
    commonExtension.apply {
        dependencies {
            "implementation"(libs.findLibrary("kotlinx-coroutines-android").get())

            "implementation"(libs.findLibrary("google-gson").get())

            "implementation"(libs.findLibrary("androidx-core-ktx").get())
            "implementation"(libs.findLibrary("androidx-activity-ktx").get())
            "implementation"(libs.findLibrary("androidx-fragment-ktx").get())

            "implementation"(libs.findLibrary("androidx-appCompat").get())
            "implementation"(libs.findLibrary("androidx-appCompat").get())
            "implementation"(libs.findLibrary("androidx-constraintLayout").get())
            "implementation"(libs.findLibrary("androidx-recyclerView").get())
            "implementation"(libs.findLibrary("androidx-viewPager2").get())

            "implementation"(libs.findLibrary("brvah").get())
            "implementation"(libs.findLibrary("smartrefresh-layout").get())
            "implementation"(libs.findLibrary("smartrefresh-header").get())
            "implementation"(libs.findLibrary("smartrefresh-footer").get())

            "testImplementation"(libs.findLibrary("junit").get())
            "androidTestImplementation"(libs.findLibrary("androidx-test-ext").get())
            "androidTestImplementation"(libs.findLibrary("androidx-test-espresso").get())
        }
    }
}

internal fun Project.configureFeatureAndroid(commonExtension: CommonExtension) {
    commonExtension.apply {
        dependencies {
            "implementation"(project(":modules:module_demo"))
            "implementation"(project(":modules:module_widget"))
            "implementation"(project(":modules:module_libraries"))

            "implementation"(project(":modules:module_opensource"))
            //"implementation"(project(":modules:module_database"))
            "implementation"(project(":modules:module_utils"))

            "implementation"(project(":modules:module_network"))
            "implementation"(project(":modules:module_sample"))

            "implementation"(project(":modules:module_room"))
            "implementation"(project(":modules:module_arch"))

            "implementation"(project(":modules:module_compose"))

            "implementation"(project(":modules:module_flutter"))
        }
    }
}
