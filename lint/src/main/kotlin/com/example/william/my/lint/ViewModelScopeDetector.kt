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
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getContainingUMethod
import java.util.EnumSet

/**
 * ViewModel 作用域检查：`viewModel()` 仅允许在顶层 Screen/Route Composable 中获取。
 *
 * 在叶子子 Composable 内直接调用 `viewModel()` 会破坏状态提升与组件可测性，
 * 使 Preview/单测被全局状态拖累，也容易在列表复用与多返回栈场景下出现实例错乱。
 * 顶层容器获取 ViewModel 后，子组件只接收 `UiState` 与事件回调。
 */
class ViewModelScopeDetector :
    Detector(),
    SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = ViewModelScopeHandler(context)

    private class ViewModelScopeHandler(private val context: JavaContext) : UElementHandler() {

        override fun visitCallExpression(node: UCallExpression) {
            val sourceText = node.sourcePsi?.text.orEmpty()
            val methodName = node.methodName?.substringBefore('(')?.trim().orEmpty()
            val isTargetCall = methodName in TARGET_METHODS ||
                Regex("\\b(viewModel|hiltViewModel)\\s*(<[^>]*>)?\\s*\\(").containsMatchIn(sourceText)
            if (!isTargetCall) return

            val method = node.getContainingUMethod() ?: return
            if (!method.isComposable()) return
            if (method.isTopLevelScreenComposable()) return

            val usedName = methodName.takeIf { it in TARGET_METHODS }
                ?: if (sourceText.contains("hiltViewModel")) "hiltViewModel" else "viewModel"
            context.report(
                issue = ISSUE_VIEW_MODEL_SCOPE,
                location = context.getLocation(node),
                message = "子 Composable `${method.name}` 内不要直接调用 `$usedName()`；" +
                    "请在顶层 Screen/Route 中获取 ViewModel，并向子组件传递 UiState 与事件回调。",
            )
        }

        private fun UMethod.isComposable(): Boolean {
            if (uAnnotations.any { annotation ->
                    val qName = annotation.qualifiedName ?: ""
                    val text = annotation.sourcePsi?.text.orEmpty()
                    qName.substringAfterLast('.') == "Composable" || text.contains("Composable")
                }
            ) {
                return true
            }
            return sourcePsi?.text?.contains("@Composable") == true
        }

        private fun UMethod.isTopLevelScreenComposable(): Boolean {
            val name = name ?: return false
            return ALLOWED_NAME_SUFFIXES.any { name.endsWith(it) }
        }
    }

    companion object {

        const val ISSUE_ID = "ViewModelScope"

        /** 顶层页面容器惯用命名后缀；命中则允许获取 ViewModel */
        private val ALLOWED_NAME_SUFFIXES = setOf("Screen", "Route", "Page", "Destination")

        private val TARGET_METHODS = setOf("viewModel", "hiltViewModel")

        @JvmField
        val ISSUE_VIEW_MODEL_SCOPE: Issue = Issue.create(
            id = ISSUE_ID,
            briefDescription = "viewModel() 仅允许在顶层 Screen/Route Composable 获取",
            explanation = """
                在可复用的叶子 Composable 中调用 `viewModel()` / `hiltViewModel()` 会：
                1. 破坏 State Hoisting，组件无法独立 Preview 与单测；
                2. 在 LazyColumn 复用、嵌套导航或多返回栈下绑定错误的作用域实例。

                正确做法：在 `*Screen` / `*Route` / `*Page` 等顶层容器获取 ViewModel，
                子组件只接收不可变 `UiState` 与事件 Lambda。
                需要保留的对照实现可 `@Suppress("ViewModelScope")`。
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                ViewModelScopeDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE),
            ),
        )
    }
}
