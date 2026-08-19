package com.example.william.my.module.compose.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import com.example.william.my.module.compose.R

/**
 * ComposeView — 在传统 View 体系中使用 Compose
 *
 * ComposeView 允许在传统 View 体系中嵌入 Compose 组件。
 *
 * 核心特性：
 * 1. 混合使用：在传统 View 中嵌入 Compose 组件
 * 2. 渐进迁移：支持从传统 View 渐进迁移到 Compose
 * 3. 灵活配置：支持在 XML 布局中使用 ComposeView
 * 4. 兼容性好：兼容传统 View 体系
 *
 * 基本用法：
 * ```kotlin
 * // 在 XML 布局中
 * <androidx.compose.ui.platform.ComposeView
 *     android:id="@+id/compose_view"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content" />
 *
 * // 在代码中设置
 * findViewById<ComposeView>(R.id.compose_view).setContent {
 *     MyTheme {
 *         Greeting("Android")
 *     }
 * }
 * ```
 *
 * 适用场景：
 * - 传统 View 项目渐进迁移到 Compose
 * - 需要在传统 View 中使用 Compose 组件
 * - 混合使用 View 和 Compose
 *
 * https://developer.android.google.cn/jetpack/compose/migrate/interoperability-apis/views-in-compose
 */
class ComposeViewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_compose_view)

        findViewById<ComposeView>(R.id.compose_view).setContent {

        }
    }
}