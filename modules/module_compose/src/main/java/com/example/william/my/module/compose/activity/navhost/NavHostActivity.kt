package com.example.william.my.module.compose.activity.navhost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlinx.serialization.Serializable

/**
 * 强类型导航路由模型契约 (Type-Safe Navigation Routes)
 *
 * 遵循 AndroidX Navigation 2.8+ 官方规范：
 * 采用 Kotlin Serialization 声明路由目的地，彻底弃用字符串 URL 拼接，
 * 提供编译期强类型安全、无痛复杂参数反序列化与安全返回栈跳转。
 */
sealed interface NavRoute {
    /**
     * 首页无参路由
     */
    @Serializable
    data object Home : NavRoute

    /**
     * 详情页强类型参数路由
     *
     * @param articleId 文章 ID
     * @param title 文章标题
     * @param author 发布作者（带默认值可选参数）
     */
    @Serializable
    data class Detail(
        val articleId: Int,
        val title: String,
        val author: String = "Google Android 官方",
    ) : NavRoute

    /**
     * 设置页可选参数路由
     *
     * @param debugMode 调试开关
     * @param theme 界面主题
     */
    @Serializable
    data class Settings(
        val debugMode: Boolean = true,
        val theme: String = "Material 3",
    ) : NavRoute
}

/**
 * NavHost — 官方类型安全导航（Type-Safe Navigation）
 *
 * 演示 Jetpack Navigation 2.8+ 官方强类型目的地声明、参数传递、转场动效与返回栈控制。
 */
@Route(path = RouterPath.Compose.NavHost)
class NavHostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    TypeSafeNavigationApp()
                }
            }
        }
    }

    @Composable
    fun TypeSafeNavigationApp() {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = NavRoute.Home,
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { -it } + fadeOut() },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
            popExitTransition = { slideOutHorizontally { it } + fadeOut() },
        ) {
            composable<NavRoute.Home> {
                HomeScreen(navController)
            }

            composable<NavRoute.Detail> { backStackEntry ->
                val detail = backStackEntry.toRoute<NavRoute.Detail>()
                DetailScreen(detail, navController)
            }

            composable<NavRoute.Settings> { backStackEntry ->
                val settings = backStackEntry.toRoute<NavRoute.Settings>()
                SettingsScreen(settings, navController)
            }
        }
    }

    @Composable
    fun HomeScreen(navController: NavController) {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "官方类型安全导航 (Type-Safe Navigation)",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "弃用旧版字符串路由（\"home\" / \"second?id={id}\"），采用 Kotlinx Serialization 强类型安全对象作为唯一目的地契约。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "【特性 1】强类型对象路由声明",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "composable<NavRoute.Detail> 代替字符串匹配，参数在编译期静态校验。",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                Button(
                    onClick = {
                        navController.navigate(
                            NavRoute.Detail(
                                articleId = 1024,
                                title = "Jetpack Compose 现代化架构实战",
                                author = "Antigravity Team",
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("携带强类型参数跳转至 Detail 详情页")
                }

                Button(
                    onClick = {
                        navController.navigate(NavRoute.Settings())
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("携带默认参数跳转至 Settings 设置页")
                }
            }
        }
    }

    @Composable
    fun DetailScreen(detail: NavRoute.Detail, navController: NavController) {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Detail 详情页（反序列化结果）",
                    style = MaterialTheme.typography.titleLarge,
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = "文章 ID: ${detail.articleId}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "文章标题: ${detail.title}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "作者来源: ${detail.author}", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "底层通过 backStackEntry.toRoute<NavRoute.Detail>() 自动还原为实体类，零 Bundle 强转！",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { navController.navigate(NavRoute.Settings(debugMode = false)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("继续跳转至 Settings 设置页")
                }

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("返回上一页 (popBackStack)")
                }
            }
        }
    }

    @Composable
    fun SettingsScreen(settings: NavRoute.Settings, navController: NavController) {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Settings 设置页",
                    style = MaterialTheme.typography.titleLarge,
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = "调试模式 (debugMode): ${settings.debugMode}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "当前主题 (theme): ${settings.theme}", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        // 弹出回到 Home，并保持 Home 单例
                        navController.popBackStack<NavRoute.Home>(inclusive = false)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("弹出所有中间页面，直接回退至 Home (popBackStack<NavRoute.Home>)")
                }

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("返回上一页")
                }
            }
        }
    }
}
