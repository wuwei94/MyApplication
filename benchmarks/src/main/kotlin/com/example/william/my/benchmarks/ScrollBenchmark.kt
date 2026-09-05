package com.example.william.my.benchmarks

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 列表滚动帧耗时基准测试
 *
 * 评估比较：
 * 1. 无优化状态 ([CompilationMode.None]) 下的帧耗时分布；
 * 2. 基线配置文件优化状态 ([CompilationMode.Partial]) 下的帧耗时分布。
 *
 * 与 [StartupBenchmark] 的关系：StartupBenchmark 衡量「首次到达第一帧可交互」（timeToInitialDisplayMs），
 * ScrollBenchmark 衡量「页面真正流畅起来」（帧耗时 P50/P90/P95/P99）。两者共用同一套 compilation mode，
 * 在 PR 中同时给出即可定位「冷启动是否变慢」「滑动是否更卡」两类回归。
 *
 * 运行命令（需连接真机或已启动模拟器）：
 * ./gradlew :benchmarks:connectedCheck -Pandroid.testInstrumentationRunnerArguments.class=com.example.william.my.benchmarks.ScrollBenchmark
 *
 * 生成可读报告：
 * ./tools/benchmark-report.py benchmarks/build/outputs/connected_check/.../benchmarkData.json
 */
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollCompilationNone() = scroll(CompilationMode.None())

    @Test
    fun scrollCompilationBaselineProfiles() = scroll(CompilationMode.Partial())

    private fun scroll(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = "com.example.william.my.application",
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            iterations = 5,
            startupMode = StartupMode.COLD,
        ) {
            pressHome()
            startActivityAndWait()
            flingDirectory(SWIPE_TIMES)
        }
    }

    /** 对首页 DirectoryActivity 内的可滚动列表连续上滑，触发足够多的真实帧 */
    private fun flingDirectory(times: Int) {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val width = device.displayWidth
        val height = device.displayHeight
        val downX = width / 2
        val downY = (height * 4) / 5
        val upY = height / 5
        repeat(times) {
            device.swipe(downX, downY, downX, upY, SWIPE_DURATION_MS)
        }
    }

    private companion object {
        const val SWIPE_TIMES = 8
        const val SWIPE_DURATION_MS = 8
    }
}
