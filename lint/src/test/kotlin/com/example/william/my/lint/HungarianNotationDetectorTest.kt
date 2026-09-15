package com.example.william.my.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

/**
 * [HungarianNotationDetector] 的规则单测。
 *
 * 验证：
 * 1. 命中 `m[A-Z]` 命名时报告 WARNING 并提供首字母小写建议；
 * 2. 正常小驼峰字段或全大写常量正常放行；
 * 3. 标记 `@Deprecated` 的兼容属性正常放行。
 */
class HungarianNotationDetectorTest {

    @Test
    fun property_hungarianName_reportsWarningAndSuggestsName() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    class SampleActivity {
                        private var mBinding: String = ""
                        private var mIsScanning: Boolean = false
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectContains("避免使用以 m 开头的匈牙利命名法，当前字段为 mBinding，建议重命名为 binding。")
            .expectContains("避免使用以 m 开头的匈牙利命名法，当前字段为 mIsScanning，建议重命名为 isScanning。")
    }

    @Test
    fun property_standardCamelCase_reportsNothing() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    class SampleActivity {
                        private var binding: String = ""
                        val maxCount: Int = 10
                        private val isScanning: Boolean = false
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectClean()
    }

    @Test
    fun property_deprecatedHungarianName_reportsNothing() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    class SampleActivity {
                        @Deprecated("使用 binding 代替 mBinding")
                        private var mBinding: String = ""
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectClean()
    }

    @Test
    fun property_insideDeprecatedClass_reportsNothing() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    @Deprecated("历史示例演示类")
                    class LegacyShowcaseActivity {
                        private var mLegacyClient: String = ""
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectClean()
    }

    private fun lintTask() = TestLintTask.lint()
        .allowMissingSdk()
        .issues(HungarianNotationDetector.ISSUE_HUNGARIAN_NOTATION)
}
