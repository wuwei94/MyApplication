package com.example.william.my.module.compose.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme

/**
 * Jetpack Compose — 现代化 UI 工具包
 *
 * Jetpack Compose 是 Android 推荐的现代化 UI 工具包，使用声明式编程。
 *
 * 核心特性：
 * 1. 声明式 UI：使用 Kotlin 代码描述 UI，无需 XML
 * 2. 状态驱动：UI 自动响应状态变化
 * 3. 组件化：支持组件复用和组合
 * 4. 预览支持：支持实时预览
 *
 * 核心组件：
 * 1. @Composable：标记可组合函数
 * 2. ComposeView：在传统 View 体系中使用 Compose
 * 3. setContent：设置 Compose 内容
 * 4. Modifier：修饰符，用于配置组件
 *
 * 基本用法：
 * ```kotlin
 * // 在 Activity 中使用
 * class MyActivity : ComponentActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContent {
 *             MyTheme {
 *                 Greeting("Android")
 *             }
 *         }
 *     }
 * }
 *
 * // 可组合函数
 * @Composable
 * fun Greeting(name: String) {
 *     Text(text = "Hello $name!")
 * }
 *
 * // 预览
 * @Preview
 * @Composable
 * fun GreetingPreview() {
 *     Greeting("Android")
 * }
 * ```
 *
 * 适用场景：
 * - 新项目开发
 * - 现代化 UI 开发
 * - 声明式编程
 *
 * https://developer.android.google.cn/jetpack/compose
 */
//@Route(path = RouterPath.Compose.Main)
class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        MyApplicationTheme {
            Greeting("Android")
        }
    }
}