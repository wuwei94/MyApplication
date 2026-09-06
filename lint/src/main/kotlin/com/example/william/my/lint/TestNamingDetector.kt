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
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import java.util.EnumSet

/**
 * 测试命名规范检查：约束测试类名与测试方法名的写法。
 *
 * 判定「是不是测试代码」不依赖目录名，而是看有没有 `@Test` 方法：
 * - 含 `@Test` 方法的类视为测试类，类名必须以 `Test` 结尾；
 * - 带 `@Test` 的方法，方法名必须是 `被测对象_场景_预期结果` 的下划线式。
 *
 * 这样测试辅助类（Fake / 工具类）不会被误报，规则也能同时覆盖 `src/test` 与 `src/androidTest`。
 */
class TestNamingDetector :
    Detector(),
    SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UClass::class.java, UMethod::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = TestNamingHandler(context)

    private class TestNamingHandler(private val context: JavaContext) : UElementHandler() {

        override fun visitClass(node: UClass) {
            val name = node.name ?: return
            if (!node.hasTestMethod() || name.endsWith(TEST_CLASS_SUFFIX)) return
            context.report(
                issue = ISSUE_TEST_CLASS_NAME,
                location = context.getNameLocation(node),
                message = "测试类名必须以 `Test` 结尾，当前为 `$name`，建议改为 `${name}Test`。",
            )
        }

        override fun visitMethod(node: UMethod) {
            if (!node.isTestMethod()) return
            val name = node.name
            if (TEST_METHOD_NAME.containsMatchIn(name)) return
            context.report(
                issue = ISSUE_TEST_METHOD_NAME,
                location = context.getNameLocation(node),
                message = "测试方法名应为 `被测对象_场景_预期结果`（小写字母开头、下划线分段），" +
                    "当前为 `$name`；中文语义请写进 KDoc 或断言消息。",
            )
        }
    }

    companion object {

        /** JUnit 4 / JUnit 5 的测试方法注解，按全限定名匹配避免同名注解误判 */
        private val TEST_ANNOTATIONS = setOf("org.junit.Test", "org.junit.jupiter.api.Test")

        /**
         * 注解源文本的归一化匹配集合。
         *
         * 类型解析失败时（无 classpath 的测试环境）[UDeclaration.findAnnotation] 返回 null，
         * 回退到注解文本匹配：`@Test`、`@org.junit.Test` 等写法都能识别。
         */
        private val TEST_ANNOTATION_TEXTS = TEST_ANNOTATIONS +
            setOf("Test", "kotlin.test.Test")

        /** 测试类名的必需后缀 */
        private const val TEST_CLASS_SUFFIX = "Test"

        /**
         * 测试方法名格式：`被测对象_场景_预期结果`。
         *
         * 至少两段，每段以小写字母开头，段内允许驼峰，例如 `mapFlow_lowercaseInput_emitsUppercase`。
         * 采用英文而非反引号中文，是为了让规则可被机械校验，并与 Android 官方 / Now in Android 保持一致。
         */
        private val TEST_METHOD_NAME = Regex("^[a-z][a-zA-Z0-9]*(_[a-zA-Z0-9]+)+$")

        private fun UClass.hasTestMethod(): Boolean = methods.any { it.isTestMethod() }

        /** 优先按全限定名做类型解析，解析不到时回退注解源文本匹配（覆盖无 classpath 环境） */
        private fun UMethod.isTestMethod(): Boolean = TEST_ANNOTATIONS.any { findAnnotation(it) != null } ||
            uAnnotations.any { annotation ->
                annotation.sourcePsi?.text
                    ?.removePrefix("@")
                    ?.trim() in TEST_ANNOTATION_TEXTS
            }

        /** 测试类命名：`FlowOperatorSamples` → 应改为 `FlowOperatorSamplesTest` */
        @JvmField
        val ISSUE_TEST_CLASS_NAME: Issue = Issue.create(
            id = "TestClassName",
            briefDescription = "测试类名必须以 Test 结尾",
            explanation = """
                包含 `@Test` 方法的类即为测试类，类名必须以 `Test` 结尾。
                统一后缀后，Gradle 测试任务、IDE 运行配置与 CI 报告才能稳定识别测试类，
                并与同目录下的测试替身（Fake）、测试数据工厂等辅助类区分开。
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                TestNamingDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE),
            ),
        )

        /** 测试方法命名：`testMapFlow` → 应改为 `mapFlow_lowercaseInput_emitsUppercase` */
        @JvmField
        val ISSUE_TEST_METHOD_NAME: Issue = Issue.create(
            id = "TestMethodName",
            briefDescription = "测试方法名必须使用 subject_scenario_expected 下划线式",
            explanation = """
                测试方法名统一为 `被测对象_场景_预期结果`，小写字母开头、下划线分段（段内允许驼峰）。

                相比 `testXxx` 前缀式或反引号中文短句，这一写法的价值在于：
                1. 断言失败时在 CI 日志里即可读出「哪个对象的什么场景期望什么」，无需点开源码；
                2. 同一被测对象的用例在 IDE 中天然聚拢，便于对照覆盖情况；
                3. 规则可被 Lint 机械校验，避免命名风格随人漂移。

                中文语义请写进 KDoc 或断言消息，不要放进方法名。
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                TestNamingDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE),
            ),
        )
    }
}
