package com.google.samples.apps.nowinandroid

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPluginExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/**
 * Dependency Guard 依赖漂移防护接入。
 *
 * Android Library / Feature 模块锁定 prod 变体运行时类路径；
 * 纯 JVM 模块锁定 `runtimeClasspath`。基线文件落在各模块 `dependencies/` 目录，
 * 由 `dependencyGuard` 任务比对，`dependencyGuardBaseline` 任务刷新。
 */
fun Project.configureAndroidDependencyGuard() {
    pluginManager.apply("com.dropbox.dependency-guard")
    extensions.configure<DependencyGuardPluginExtension> {
        configuration("prodDebugRuntimeClasspath")
        configuration("prodReleaseRuntimeClasspath")
    }
}

/**
 * 纯 JVM 模块的 Dependency Guard 接入（`runtimeClasspath`）。
 */
fun Project.configureJvmDependencyGuard() {
    pluginManager.apply("com.dropbox.dependency-guard")
    extensions.configure<DependencyGuardPluginExtension> {
        configuration("runtimeClasspath")
    }
}
