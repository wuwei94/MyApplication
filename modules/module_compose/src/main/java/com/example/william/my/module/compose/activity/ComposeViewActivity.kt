package com.example.william.my.module.compose.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.R
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme

/**
 * ComposeView — 在传统 View 体系中使用 Compose
 *
 * 在传统 View 体系中嵌入 Compose 组件，支持渐进迁移。
 *
 * https://developer.android.google.cn/jetpack/compose/migrate/interoperability-apis/views-in-compose
 */
@Route(path = RouterPath.Compose.ComposeViewActivity)
class ComposeViewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_compose_view)

        // 在传统 View 体系中，通过 ComposeView 嵌入 Compose 内容
        findViewById<ComposeView>(R.id.compose_view).setContent {
            MyApplicationTheme {
                Text(text = "这是 Compose 内容")
            }
        }
    }
}
