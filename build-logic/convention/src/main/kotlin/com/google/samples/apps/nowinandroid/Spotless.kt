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

internal fun Project.configureSpotlessForAndroid() {
    configureSpotlessCommon()
}

internal fun Project.configureSpotlessForJvm() {
    configureSpotlessCommon()
}

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
