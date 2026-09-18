package com.google.samples.apps.nowinandroid

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import java.io.File
import java.util.Properties

/**
 * APK Badging 权限与产物基线卫士。
 *
 * 通过 AAPT2 dump APK 的 package / uses-permission / uses-feature 元数据，
 * 与入库基线（`app/badging/prodRelease.txt`）比对。依赖隐式引入危险权限或
 * 产物元数据漂移时构建失败并输出 diff。
 *
 * - 校验：`./gradlew checkBadging -PenableFlutter=false`
 * - 刷新基线：`./gradlew updateBadgingBaseline -PenableFlutter=false`
 */
abstract class CheckBadgingTask : org.gradle.api.DefaultTask() {

    @get:InputDirectory
    abstract val apkDir: DirectoryProperty

    @get:OutputFile
    abstract val baselineFile: RegularFileProperty

    @get:Input
    abstract val updateBaseline: Property<Boolean>

    @get:Input
    abstract val aapt2Path: Property<String>

    @TaskAction
    fun check() {
        val apk = apkDir.get().asFile.listFiles()
            ?.firstOrNull { it.isFile && it.extension.equals("apk", ignoreCase = true) }
            ?: throw GradleException("未找到 prodRelease APK，请先执行 assembleProdRelease")

        val actual = dumpBadging(apk)
        val baseline = baselineFile.get().asFile

        if (updateBaseline.get() || !baseline.exists()) {
            baseline.parentFile?.mkdirs()
            baseline.writeText(actual)
            logger.lifecycle("Badging 基线已写入：${baseline.absolutePath}")
            return
        }

        val expected = baseline.readText()
        if (expected != actual) {
            throw GradleException(
                buildString {
                    appendLine("APK Badging 与基线不一致：${baseline.name}")
                    appendLine("--- baseline ---")
                    appendLine(expected)
                    appendLine("--- actual ---")
                    appendLine(actual)
                    appendLine("确认变更合法后执行：./gradlew updateBadgingBaseline -PenableFlutter=false")
                },
            )
        }
        logger.lifecycle("Badging 校验通过：${baseline.name}")
    }

    private fun dumpBadging(apk: File): String {
        val aapt2 = File(aapt2Path.get())
        require(aapt2.exists()) { "未找到 aapt2：$aapt2" }
        val process = ProcessBuilder(aapt2.absolutePath, "dump", "badging", apk.absolutePath)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val code = process.waitFor()
        if (code != 0) {
            throw GradleException("aapt2 dump badging 失败（exit=$code）：\n$output")
        }
        return normalizeBadging(output)
    }

    /** 只保留稳定元数据行，剔除时间戳等易变噪声 */
    private fun normalizeBadging(raw: String): String {
        val keepPrefixes = listOf(
            "package:",
            "uses-permission:",
            "uses-feature:",
            "uses-feature-not-required:",
            "application-label:",
        )
        return raw.lineSequence()
            .map { it.trim() }
            .filter { line -> keepPrefixes.any { line.startsWith(it) } }
            .distinct()
            .sorted()
            .joinToString(separator = "\n")
            .plus("\n")
    }
}

/** 定位 SDK 内的 aapt2 可执行文件 */
internal fun Project.resolveAapt2(): String {
    val localProps = rootProject.file("local.properties")
    val sdkDir = sequence {
        if (localProps.exists()) {
            val props = Properties()
            localProps.inputStream().use { props.load(it) }
            props.getProperty("sdk.dir")?.let { yield(it.replace("\\\\", "\\")) }
        }
        System.getenv("ANDROID_HOME")?.let { yield(it) }
        System.getenv("ANDROID_SDK_ROOT")?.let { yield(it) }
    }.firstOrNull() ?: throw GradleException("未配置 sdk.dir / ANDROID_HOME，无法定位 aapt2")

    val buildToolsRoot = File(sdkDir, "build-tools")
    val buildToolsDir = buildToolsRoot.listFiles()
        ?.filter { it.isDirectory }
        ?.maxByOrNull { it.name }
        ?: throw GradleException("SDK build-tools 目录为空：$buildToolsRoot")

    val exeName = if (System.getProperty("os.name").orEmpty().contains("Windows", ignoreCase = true)) {
        "aapt2.exe"
    } else {
        "aapt2"
    }
    val aapt2 = File(buildToolsDir, exeName)
    require(aapt2.exists()) { "build-tools 中未找到 $exeName：${buildToolsDir.absolutePath}" }
    return aapt2.absolutePath
}

/**
 * 为壳工程注册 Badging 校验与基线刷新任务（仅 Application 模块）。
 */
internal fun Project.configureBadging() {
    if (!pluginManager.hasPlugin("com.android.application")) return

    val baselineDir = layout.projectDirectory.dir("badging")
    val apkDir = layout.buildDirectory.dir("outputs/apk/prod/release")

    tasks.register<CheckBadgingTask>("checkBadging") {
        group = "verification"
        description = "校验 APK Badging 与入库基线一致"
        dependsOn("assembleProdRelease")
        updateBaseline.set(false)
        aapt2Path.set(resolveAapt2())
        this.apkDir.set(apkDir)
        baselineFile.set(baselineDir.file("prodRelease.txt"))
    }

    tasks.register<CheckBadgingTask>("updateBadgingBaseline") {
        group = "verification"
        description = "刷新 APK Badging 基线"
        dependsOn("assembleProdRelease")
        updateBaseline.set(true)
        aapt2Path.set(resolveAapt2())
        this.apkDir.set(apkDir)
        baselineFile.set(baselineDir.file("prodRelease.txt"))
    }
}

/**
 * 为 Android 模块声明 Gradle Managed Device（GMD）。
 * 命令示例：`./gradlew pixel6api31aospDemoDebugAndroidTest -PenableFlutter=false`
 */
internal fun configureManagedDevices(testOptions: com.android.build.api.dsl.TestOptions) {
    testOptions.managedDevices.localDevices.create("pixel6api31aosp") {
        device = "Pixel 6"
        apiLevel = 31
        systemImageSource = "aosp"
    }
}
