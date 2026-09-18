package com.example.william.my.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

/**
 * [CollectAsStateWithLifecycleDetector] 的规则单测。
 */
class CollectAsStateWithLifecycleDetectorTest {

    @Test
    fun composable_collectAsState_reportsWarning() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    fun sample() {
                        val uiState = viewModel.uiState.collectAsState()
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectContains("collectAsStateWithLifecycle")
    }

    @Test
    fun composable_collectAsStateWithLifecycle_reportsNothing() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    fun sample() {
                        val uiState = viewModel.uiState.collectAsStateWithLifecycle()
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectClean()
    }

    @Test
    fun composable_collectAsStateWithSuppress_reportsNothing() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    @Suppress("${CollectAsStateWithLifecycleDetector.ISSUE_ID}")
                    fun sample() {
                        val uiState = viewModel.uiState.collectAsState()
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectClean()
    }

    private fun lintTask() = TestLintTask.lint()
        .allowMissingSdk()
        .allowCompilationErrors()
        .issues(CollectAsStateWithLifecycleDetector.ISSUE_COLLECT_AS_STATE_WITH_LIFECYCLE)
}
