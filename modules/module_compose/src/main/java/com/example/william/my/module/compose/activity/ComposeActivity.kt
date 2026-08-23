package com.example.william.my.module.compose.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme

/**
 * Jetpack Compose — 现代化 UI 工具包
 *
 * 使用声明式编程，核心特性：
 * 1. 声明式 UI：使用 Kotlin 代码描述 UI，无需 XML
 * 2. 状态驱动：UI 自动响应状态变化
 * 3. 组件化：支持组件复用和组合
 *
 * https://developer.android.google.cn/jetpack/compose
 */
@Route(path = RouterPath.Compose.ComposeActivity)
class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // 组件复用：同一个 Greeting 组件被复用多次
                Column {
                    Greeting("Android")
                    Greeting("Compose")
                }
            }
        }
    }

    @Composable
    fun Greeting(name: String) {
        Text(text = "Hello $name!")
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        MyApplicationTheme {
            Greeting("Android")
        }
    }
}
