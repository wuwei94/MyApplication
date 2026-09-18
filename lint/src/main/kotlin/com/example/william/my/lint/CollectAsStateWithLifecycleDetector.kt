package com.example.william.my.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import java.util.EnumSet

/**
 * 生命周期安全流收集检查：Compose UI 中禁止直接使用 `Flow.collectAsState()`。
 *
 * `collectAsState()` 在页面进入后台（STOPPED）后仍持续收集上游冷流，
 * 既浪费计算资源，也容易在后台触发不必要的重组与副作用。
 * 应改用 `androidx.lifecycle.compose.collectAsStateWithLifecycle()`，
 * 使收集范围自动对齐 `Lifecycle.State.STARTED`。
 */
class CollectAsStateWithLifecycleDetector :
    Detector(),
    SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = CollectAsStateHandler(context)

    private class CollectAsStateHandler(private val context: JavaContext) : UElementHandler() {

        override fun visitCallExpression(node: UCallExpression) {
            val methodName = node.methodName
            if (methodName == "collectAsStateWithLifecycle") return
            val sourceText = node.sourcePsi?.text.orEmpty()
            val isBareCollectAsState = methodName == "collectAsState" ||
                COLLECT_AS_STATE_CALL.containsMatchIn(sourceText)
            if (!isBareCollectAsState) return
            if (sourceText.contains("collectAsStateWithLifecycle")) return
            context.report(
                issue = ISSUE_COLLECT_AS_STATE_WITH_LIFECYCLE,
                location = context.getLocation(node),
                message = "Compose UI 中请使用 `collectAsStateWithLifecycle()` 替代 `collectAsState()`，" +
                    "以便在生命周期低于 STARTED 时自动停止收集。",
            )
        }
    }

    companion object {

        /** 匹配 `collectAsState(`，排除 `collectAsStateWithLifecycle(` */
        private val COLLECT_AS_STATE_CALL = Regex("(?<!WithLifecycle)\\bcollectAsState\\s*\\(")

        const val ISSUE_ID = "CollectAsStateWithLifecycle"

        @JvmField
        val ISSUE_COLLECT_AS_STATE_WITH_LIFECYCLE: Issue = Issue.create(
            id = ISSUE_ID,
            briefDescription = "Compose 中应使用 collectAsStateWithLifecycle",
            explanation = """
                `Flow.collectAsState()` 不感知宿主生命周期，页面退到后台后仍会持续收集上游流。
                请使用 `androidx.lifecycle.compose.collectAsStateWithLifecycle()`，
                在 `Lifecycle.State.STARTED` 以下自动取消收集，避免后台无效计算与潜在泄漏。
                教学对照场景可用 `@Suppress("CollectAsStateWithLifecycle")` 局部放行。
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                CollectAsStateWithLifecycleDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE),
            ),
        )
    }
}
