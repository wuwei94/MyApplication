plugins {
    // 纯 JVM 库：Lint 自定义规则以 jar 形式产出，通过 lintChecks 注入各模块
    alias(libs.plugins.nowinandroid.jvm.library)
}

dependencies {
    // Lint API 由 AGP 在运行时提供，只参与编译不打进产物
    compileOnly(libs.bundles.lint.rules)
    testImplementation(libs.bundles.lint.rules)
    testImplementation(libs.lint.tests)
}
