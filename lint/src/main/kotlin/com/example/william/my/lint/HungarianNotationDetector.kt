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
import org.jetbrains.uast.UDeclaration
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UField
import org.jetbrains.uast.getContainingUClass
import java.util.EnumSet

/**
 * 匈牙利命名法检查：禁止成员变量与属性使用 `m` 前缀（如 `mBinding`, `mAdapter`, `mLog`）。
 *
 * 遵循现代 Kotlin 与 Google 官方编码规范，属性统一使用语义清晰的小驼峰命名。
 * 放行条件：
 * - 带有 `@Deprecated` 注解的兼容过渡属性或其所属类带有 `@Deprecated`；
 * - 非 `m[A-Z]` 模式的常规命名。
 */
class HungarianNotationDetector :
    Detector(),
    SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UField::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = HungarianNotationHandler(context)

    private class HungarianNotationHandler(private val context: JavaContext) : UElementHandler() {

        override fun visitField(node: UField) {
            val name = node.name
            if (!HUNGARIAN_FIELD_PATTERN.matches(name)) return

            // 属性自身或所属类带有 @Deprecated 注解的兼容/历史属性予以放行
            if (node.isDeprecated() || node.getContainingUClass()?.isDeprecated() == true) return

            val suggested = name.substring(1).replaceFirstChar { it.lowercase() }
            context.report(
                issue = ISSUE_HUNGARIAN_NOTATION,
                location = context.getNameLocation(node),
                message = "避免使用以 `m` 开头的匈牙利命名法，当前字段为 `$name`，建议重命名为 `$suggested`。",
            )
        }

        private fun UDeclaration.isDeprecated(): Boolean = uAnnotations.any { annotation ->
            val qName = annotation.qualifiedName ?: ""
            val simpleName = qName.substringAfterLast('.')
            simpleName == "Deprecated" || qName == "kotlin.Deprecated" || qName == "java.lang.Deprecated"
        }
    }

    companion object {
        private val HUNGARIAN_FIELD_PATTERN = Regex("^m[A-Z].*")

        @JvmField
        val ISSUE_HUNGARIAN_NOTATION: Issue = Issue.create(
            id = "HungarianNotation",
            briefDescription = "禁止使用 m 前缀匈牙利命名法",
            explanation = """
                按照现代 Kotlin 与 Google Android 官方规范，禁止成员变量使用 `m` 前缀（如 `mBinding`、`mAdapter`）。
                请直接使用语义清晰的小驼峰命名（如 `binding`、`adapter`）。
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                HungarianNotationDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE),
            ),
        )
    }
}
