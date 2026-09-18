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
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UDeclaration
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UField
import org.jetbrains.uast.getContainingUClass
import java.util.EnumSet

/**
 * 现代时间 API 检查：数据实体与网络链路禁止依赖 `java.util.Date` / `Calendar` / `SimpleDateFormat`。
 *
 * 这些 API 非线程安全、时区处理易错，且不利于 Kotlin Multiplatform 与类型化序列化。
 * 推荐迁移至 `java.time.*` 或 `kotlinx.datetime.*`。
 *
 * 废弃 API 教学对照页可在类上标注 `@Deprecated` 放行。
 */
class DateTimeApiDetector :
    Detector(),
    SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(
        UClass::class.java,
        UCallExpression::class.java,
    )

    override fun createUastHandler(context: JavaContext): UElementHandler = DateTimeHandler(context)

    private class DateTimeHandler(private val context: JavaContext) : UElementHandler() {

        override fun visitClass(node: UClass) {
            if (node.isDeprecated()) return
            val typeAliasMap = node.collectTypeAliasesInFile()
            node.fields
                .filterIsInstance<UField>()
                .forEach { field ->
                    val rawType = field.type.canonicalText.substringBefore('<')
                    val resolvedFq = context.evaluator.getTypeClass(field.type)?.qualifiedName
                        ?: typeAliasMap[rawType.substringAfterLast('.')]
                        ?: rawType
                    val fqName = when {
                        resolvedFq in BANNED_IMPORTS -> resolvedFq
                        else -> BANNED_SIMPLE_TO_FQ[resolvedFq.substringAfterLast('.')]
                    } ?: return@forEach
                    context.report(
                        issue = ISSUE_DATE_TIME_API,
                        location = context.getNameLocation(field),
                        message = "模型字段类型避免使用 `$fqName`，建议改用 ${BANNED_IMPORTS[fqName]}。",
                    )
                }
        }

        /** 收集当前编译单元中的 typealias 映射：简单名 → 目标类型简单名/全名 */
        private fun UClass.collectTypeAliasesInFile(): Map<String, String> {
            val parent = uastParent ?: return emptyMap()
            val parentText = parent.asRenderString()
            val result = mutableMapOf<String, String>()
            Regex("typealias\\s+(\\w+)\\s*=\\s*([\\w.]+)").findAll(parentText).forEach { match ->
                result[match.groupValues[1]] = match.groupValues[2]
            }
            return result
        }

        override fun visitCallExpression(node: UCallExpression) {
            if (node.getContainingUClass()?.isDeprecated() == true) return
            val methodName = node.methodName ?: ""
            val rendered = node.asRenderString()
            val sourceText = node.sourcePsi?.text.orEmpty()
            val fqName = when {
                methodName == "SimpleDateFormat" ||
                    rendered.startsWith("SimpleDateFormat") ||
                    sourceText.startsWith("SimpleDateFormat") ->
                    "java.text.SimpleDateFormat"
                methodName == "Calendar" || sourceText.contains("Calendar.getInstance") ->
                    "java.util.Calendar"
                methodName == "Date" ||
                    rendered.startsWith("Date(") ||
                    Regex("\\bDate\\s*\\(").containsMatchIn(sourceText) ->
                    "java.util.Date"
                else -> return
            }
            if (fqName !in BANNED_IMPORTS) return
            context.report(
                issue = ISSUE_DATE_TIME_API,
                location = context.getLocation(node),
                message = "避免使用 `$fqName`，建议改用 ${BANNED_IMPORTS[fqName]}。",
            )
        }

        private fun UDeclaration.isDeprecated(): Boolean {
            if (uAnnotations.any { annotation ->
                    val qName = annotation.qualifiedName ?: ""
                    val simpleName = qName.substringAfterLast('.')
                    val text = annotation.sourcePsi?.text.orEmpty()
                    simpleName == "Deprecated" ||
                        qName == "kotlin.Deprecated" ||
                        qName == "java.lang.Deprecated" ||
                        text.contains("Deprecated")
                }
            ) {
                return true
            }
            return sourcePsi?.text?.contains("@Deprecated") == true
        }
    }

    companion object {

        const val ISSUE_ID = "DateTimeApi"

        private val BANNED_IMPORTS = mapOf(
            "java.util.Date" to "`java.time.Instant` / `kotlinx.datetime.Instant`",
            "java.util.Calendar" to "`java.time.ZonedDateTime` / `java.time.LocalDateTime`",
            "java.text.SimpleDateFormat" to "`java.time.format.DateTimeFormatter`",
        )

        private val BANNED_SIMPLE_TO_FQ = mapOf(
            "Date" to "java.util.Date",
            "Calendar" to "java.util.Calendar",
            "SimpleDateFormat" to "java.text.SimpleDateFormat",
        )

        @JvmField
        val ISSUE_DATE_TIME_API: Issue = Issue.create(
            id = ISSUE_ID,
            briefDescription = "数据与网络层应使用现代时间 API",
            explanation = """
                `java.util.Date`、`java.util.Calendar` 与 `java.text.SimpleDateFormat` 非线程安全，
                时区与格式化行为易错，也不利于跨平台序列化。

                数据实体模型、网络请求/响应字段与仓储层请优先使用：
                * `java.time.Instant` / `java.time.LocalDateTime` / `java.time.format.DateTimeFormatter`
                * 或 KMP 友好的 `kotlinx.datetime.Instant`

                废弃 API 教学对照页可对类标注 `@Deprecated` 放行。
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                DateTimeApiDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE),
            ),
        )
    }
}
