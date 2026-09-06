package com.example.william.my.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

/**
 * [TestNamingDetector] 的规则单测。
 *
 * 用 Lint 官方的 `TestLintTask` 直接喂源码片段，既验证「违规要报」也验证「合规不报」，
 * 避免规则在真实工程里误伤测试替身等辅助类。
 */
class TestNamingDetectorTest {

    @Test
    fun testClass_missingTestSuffix_reportsClassNameIssue() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    class FlowOperatorSamples {
                        @Test
                        fun mapFlow_lowercaseInput_emitsUppercase() = Unit
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectContains("测试类名必须以 Test 结尾")
            .expectContains("FlowOperatorSamplesTest")
    }

    @Test
    fun testClass_withTestSuffix_reportsNothing() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    class FlowOperatorSamplesTest {
                        @Test
                        fun mapFlow_lowercaseInput_emitsUppercase() = Unit
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectClean()
    }

    @Test
    fun testMethod_camelCaseName_reportsMethodNameIssue() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    class FlowOperatorSamplesTest {
                        @Test
                        fun testMapFlow() = Unit
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectContains("被测对象_场景_预期结果")
            .expectContains("testMapFlow")
    }

    @Test
    fun testMethod_singleSegmentName_reportsMethodNameIssue() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    class FlowOperatorSamplesTest {
                        @Test
                        fun emitsUppercase() = Unit
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectContains("被测对象_场景_预期结果")
    }

    @Test
    fun testMethod_snakeCaseName_reportsNothing() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    class FlowOperatorSamplesTest {
                        @Test
                        fun mapFlow_lowercaseInput_emitsUppercase() = Unit
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectClean()
    }

    @Test
    fun helperClass_withoutTestMethod_reportsNothing() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    class FakeNumberSource {
                        fun numbers() = listOf(1, 2)
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectClean()
    }

    private fun lintTask() = TestLintTask.lint()
        // 纯 Kotlin 语法检查不涉及 Android 资源，允许无 SDK 环境（CI）运行
        .allowMissingSdk()
        .issues(
            TestNamingDetector.ISSUE_TEST_CLASS_NAME,
            TestNamingDetector.ISSUE_TEST_METHOD_NAME,
        )
}
