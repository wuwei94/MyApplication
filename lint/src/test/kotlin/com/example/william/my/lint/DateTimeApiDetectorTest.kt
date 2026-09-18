package com.example.william.my.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

/**
 * [DateTimeApiDetector] 的规则单测。
 */
class DateTimeApiDetectorTest {

    @Test
    fun model_legacyDateField_reportsWarning() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    import java.util.Date

                    class Article {
                        val publishedAt: Date = Date()
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectContains("java.util.Date")
    }

    @Test
    fun model_simpleDateFormatCall_reportsWarning() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    import java.text.SimpleDateFormat
                    import java.util.Locale

                    fun formatNow(): String =
                        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(System.currentTimeMillis())
                    """.trimIndent(),
                ),
            )
            .run()
            .expectContains("java.text.SimpleDateFormat")
    }

    @Test
    fun model_javaTime_reportsNothing() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    import java.time.Instant

                    data class Article(val publishedAt: Instant)
                    """.trimIndent(),
                ),
            )
            .run()
            .expectClean()
    }

    @Test
    fun legacyShowcase_deprecatedClass_reportsNothing() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    import java.util.Date

                    @Deprecated("废弃 API 教学对照")
                    data class LegacyArticle(val publishedAt: Date)
                    """.trimIndent(),
                ),
            )
            .run()
            .expectClean()
    }

    private fun lintTask() = TestLintTask.lint()
        .allowMissingSdk()
        .allowCompilationErrors()
        .issues(DateTimeApiDetector.ISSUE_DATE_TIME_API)
}
