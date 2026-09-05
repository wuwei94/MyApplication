plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.example.william.my.module.markdown"
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    // Markwon 核心与常用扩展
    implementation(libs.markwon.core)
    implementation(libs.markwon.ext.tables)
    implementation(libs.markwon.ext.tasklist)
    implementation(libs.markwon.ext.strikethrough)
    implementation(libs.markwon.html)
    implementation(libs.markwon.image.glide)

    // 代码语法高亮（自带传递 prism4j，排除其过时的 annotations-java5）
    implementation(libs.markwon.syntax.highlight) {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
}
