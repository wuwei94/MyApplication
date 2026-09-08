package com.example.william.my.module.compose.activity.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * NavigationBar — 官方强类型底部导航与多返回栈（Multi-BackStack）
 *
 * 演示 Compose Material 3 与 Navigation 2.8+ 官方强类型声明、Multi-BackStack 状态保存与恢复。
 *
 * 核心特性：
 * 1. 强类型一级 Tab 路由（[TopLevelDestination]）
 * 2. 切换 Tab 自动保存状态（`saveState = true`, `restoreState = true`）
 * 3. 支持在特定 Tab 内压入二级子页面，切换其他 Tab 再切回时，二级页面状态完美保持
 */
@Route(path = RouterPath.Compose.NavigationBar)
class NavigationBarActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    TypeSafeNavigationBarApp()
                }
            }
        }
    }

    @Composable
    fun TypeSafeNavigationBarApp() {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = { TypeSafeNavigationBar(navController) },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = TopLevelDestination.Home,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable<TopLevelDestination.Home> {
                    HomeTabContent(navController)
                }

                composable<TopLevelDestination.Discover> {
                    DiscoverTabContent(navController)
                }

                composable<TopLevelDestination.Profile> {
                    ProfileTabContent()
                }

                composable<SubDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<SubDetailRoute>()
                    SubDetailContent(route, navController)
                }
            }
        }
    }

    @Composable
    fun TypeSafeNavigationBar(navController: NavHostController) {
        val destinations = listOf(
            TopLevelDestination.Home,
            TopLevelDestination.Discover,
            TopLevelDestination.Profile,
        )

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        NavigationBar {
            destinations.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any {
                    it.hasRoute(screen::class)
                } == true

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        navController.navigate(screen) {
                            // 弹出到导航图起始目的地，避免在返回栈中堆积过多冗余目的地
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // 避免多次点击同一 Tab 创建重复实例
                            launchSingleTop = true
                            // 恢复先前选中的 Tab 及其内部返回栈状态
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(imageVector = screen.icon, contentDescription = screen.label)
                    },
                    label = {
                        Text(text = screen.label)
                    },
                )
            }
        }
    }

    @Composable
    fun HomeTabContent(navController: NavHostController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "【首页 Tab】官方 Multi-BackStack 架构", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "通过 NavDestination.hasRoute(Class) 判断高亮项，完全消除字符串匹配；在各 Tab 内可继续压入二级页面。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "当前页面状态：正常渲染", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "建议测试：点击下方切换到「发现」Tab，点击进入详情页，再切换回「首页」，再切回「发现」，观察返回栈是否依然被保存。",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }

    @Composable
    fun DiscoverTabContent(navController: NavHostController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "【发现 Tab】", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "本页面提供进入二级子页面的入口，验证 Tab 独立返回栈保持机制。",
                style = MaterialTheme.typography.bodyMedium,
            )

            Button(
                onClick = {
                    navController.navigate(
                        SubDetailRoute(
                            fromTab = "Discover",
                            itemId = 888,
                            itemTitle = "Compose Multi-BackStack 状态保持深度解析",
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("在「发现」Tab 内压入二级详情页")
            }
        }
    }

    @Composable
    fun ProfileTabContent() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "【我的 Tab】", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "强类型目的地 TopLevelDestination.Profile 独立挂载，无需关注 route 字符串拼写。",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    @Composable
    fun SubDetailContent(route: SubDetailRoute, navController: NavHostController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "二级详情页（归属于 ${route.fromTab} Tab）", style = MaterialTheme.typography.titleLarge)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "条目 ID: ${route.itemId}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "条目名称: ${route.itemTitle}", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "此时您可以随意切换底栏到「首页」或「我的」，再切回「发现」，本页面依然完整驻留在返回栈顶端！",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("返回发现列表页 (popBackStack)")
            }
        }
    }
}
