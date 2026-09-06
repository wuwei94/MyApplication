package com.example.william.my.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

/**
 * Lint 自定义规则的注册入口。
 *
 * 全限定名登记在 `src/main/resources/META-INF/services/` 下的
 * `com.android.tools.lint.client.api.IssueRegistry` 文件中，AGP 通过 SPI 加载本注册表的规则。
 *
 * [api] 必须与编译期使用的 lint-api 版本一致；lint-api 版本需跟随 AGP 内置的 Lint 版本升级
 * （当前 AGP 9.1.0 对应 Lint 32.1.x，版本定义在 gradle/libs.versions.toml 的 `androidLint`）。
 */
@Suppress("UnstableApiUsage")
class TestNamingIssueRegistry : IssueRegistry() {

    override val api: Int = CURRENT_API

    override val minApi: Int = CURRENT_API

    override val vendor: Vendor = Vendor(
        vendorName = "MyApplication",
        feedbackUrl = "https://github.com/wuwei/MyApplication/issues",
    )

    override val issues: List<Issue> = listOf(
        TestNamingDetector.ISSUE_TEST_CLASS_NAME,
        TestNamingDetector.ISSUE_TEST_METHOD_NAME,
    )
}
