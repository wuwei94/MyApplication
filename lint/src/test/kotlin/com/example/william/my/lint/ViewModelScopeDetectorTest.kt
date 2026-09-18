package com.example.william.my.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

/**
 * [ViewModelScopeDetector] 的规则单测。
 */
class ViewModelScopeDetectorTest {

    @Test
    fun childComposable_viewModelCall_reportsWarning() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    import androidx.compose.runtime.Composable
                    import androidx.lifecycle.viewmodel.compose.viewModel

                    @Composable
                    fun ArticleCard() {
                        val viewModel = viewModel<SampleViewModel>()
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectContains("顶层 Screen/Route")
    }

    @Test
    fun screenComposable_viewModelCall_reportsNothing() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    import androidx.compose.runtime.Composable
                    import androidx.lifecycle.viewmodel.compose.viewModel

                    @Composable
                    fun ArticleScreen() {
                        val viewModel = viewModel<SampleViewModel>()
                    }
                    """.trimIndent(),
                ),
            )
            .run()
            .expectClean()
    }

    @Test
    fun nonComposable_viewModelDelegate_reportsNothing() {
        lintTask()
            .files(
                kotlin(
                    """
                    package test

                    class SampleActivity {
                        private val sampleViewModel by viewModel()
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
        .issues(ViewModelScopeDetector.ISSUE_VIEW_MODEL_SCOPE)
}
