package com.example.william.my.benchmarks

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Baseline Profile 生成器
 *
 * 用于为 App 生成基线配置文件 (baseline-prof.txt)，提前将热点代码编译为机器码，提升冷启动与首屏渲染性能。
 *
 * 运行命令（需连接真机或已启动模拟器）：
 * ./gradlew :benchmarks:connectedCheck -Pandroid.testInstrumentationRunnerArguments.class=com.example.william.my.benchmarks.BaselineProfileGenerator
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        baselineProfileRule.collect(
            packageName = "com.example.william.my.application",
            includeInStartupProfile = true,
        ) {
            // 启动应用主界面
            pressHome()
            startActivityAndWait()
        }
    }
}
