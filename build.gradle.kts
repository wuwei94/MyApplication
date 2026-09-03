// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        // 传统 Maven 构件插件依赖
        classpath(libs.gradlePlugin.protobuf)
        classpath(libs.gradlePlugin.arouter)
        classpath(libs.gradlePlugin.objectbox)
    }
}

// 统一声明全工程使用的 Gradle 插件版本（apply false 仅声明不应用）
plugins {
    // Android & Kotlin 核心构建插件
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false

    // 架构与注解处理
    alias(libs.plugins.android.ksp) apply false
    alias(libs.plugins.android.hilt) apply false
    alias(libs.plugins.android.room) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // 质量保证、代码规范与依赖保护
    alias(libs.plugins.dependency.guard) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.baselineprofile) apply false

    // 根工程全局管理约定插件（生成模块拓扑图与 Spotless 规范）
    alias(libs.plugins.nowinandroid.root)
}

// 全局子模块编译目标统一
subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}