package com.example.william.my.module.compose.activity.nav3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 声明式导航键接口契约 (Navigation 3 NavKey)
 *
 * 在 Navigation 3 架构中，导航目的地完全由不可变状态数据模型（NavKey）表征：
 * 1. 零 Android/Navigation 组件依赖，纯 Kotlin 数据对象；
 * 2. 天然支持单元测试与纯 JVM 验证；
 * 3. 页面渲染层（NavDisplay）仅负责根据 NavKey 映射对应的 Composable。
 */
sealed interface DemoNavKey {
    /** 导航项展示标题 */
    val title: String

    /** 导航项图标 */
    val icon: ImageVector

    /**
     * 首页目的地
     */
    data object Home : DemoNavKey {
        override val title: String = "首页 (Home)"
        override val icon: ImageVector = Icons.Default.Home
    }

    /**
     * 话题详情目的地
     *
     * @param topicId 话题 ID
     * @param topicTitle 话题名称
     * @param topicDesc 话题说明
     */
    data class TopicDetail(
        val topicId: String,
        val topicTitle: String,
        val topicDesc: String,
    ) : DemoNavKey {
        override val title: String = "主题: $topicTitle"
        override val icon: ImageVector = Icons.Default.Info
    }

    /**
     * 用户画像目的地
     *
     * @param userId 用户 ID
     * @param userName 用户名称
     * @param bio 简介
     */
    data class UserProfile(
        val userId: String,
        val userName: String,
        val bio: String,
    ) : DemoNavKey {
        override val title: String = "个人资料: $userName"
        override val icon: ImageVector = Icons.Default.Person
    }

    /**
     * 设置目的地
     */
    data object Settings : DemoNavKey {
        override val title: String = "偏好设置 (Settings)"
        override val icon: ImageVector = Icons.Default.Settings
    }
}

/**
 * Navigation 3 声明式导航状态托管容器
 *
 * 核心设计哲学：
 * 1. **State Hoisting（状态提升）**：返回栈（BackStack）不再是 NavController 内部黑盒，
 *    而是提升为纯 Compose 响应式状态列表；
 * 2. **可预测性与时间旅行**：返回栈的推入、弹出、重置即是对标准 List 的修改，
 *    UI 自动响应状态变化触发重组和转场动画；
 * 3. **多窗格/自适应天然契合**：平板或折叠屏双栏场景下，可同时消费 backStack 中倒数两个 Key
 *    直接实现 List-Detail 联动，无需繁重的 NavHost 嵌套。
 */
class Nav3State(initial: DemoNavKey = DemoNavKey.Home) {
    /** 响应式返回栈列表 */
    val backStack = mutableStateListOf<DemoNavKey>(initial)

    /** 记录导航方向（1: Forward, -1: Backward），用于驱动过渡转场动画 */
    var navDirection by mutableIntStateOf(1)
        private set

    /** 当前栈顶目的地 */
    val currentKey: DemoNavKey
        get() = backStack.lastOrNull() ?: DemoNavKey.Home

    /**
     * 导航到新目的地（压栈入栈）
     */
    fun push(key: DemoNavKey) {
        navDirection = 1
        backStack.add(key)
    }

    /**
     * 弹出栈顶目的地（出栈出栈）
     *
     * @return 是否成功弹出（栈底保留至少 1 个元素）
     */
    fun pop(): Boolean {
        if (backStack.size > 1) {
            navDirection = -1
            backStack.removeAt(backStack.lastIndex)
            return true
        }
        return false
    }

    /**
     * 回退到栈底根节点
     */
    fun popToRoot() {
        if (backStack.size > 1) {
            navDirection = -1
            val root = backStack.first()
            backStack.clear()
            backStack.add(root)
        }
    }
}

/**
 * Navigation 3 架构演练页面
 *
 * 对标 Google 官方 Now in Android (NiA) 最新 Navigation 3 声明式解耦方案：
 * - 声明式 [DemoNavKey] 强类型键体系；
 * - 纯状态驱动的 [Nav3State] 返回栈管理；
 * - 无侵入式的 [AnimatedContent] 自定义场景转场；
 * - 系统级物理返回键 [BackHandler] 统一拦截与受控出栈；
 * - 实时可视化的返回栈深度看板（BackStack Visualizer）。
 */
@Route(path = RouterPath.Compose.Nav3)
class Nav3Activity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Nav3AppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Nav3AppContent() {
    val navState = remember { Nav3State(DemoNavKey.Home) }

    // 拦截系统返回事件：当返回栈深度大于 1 时，优先弹出 Compose 内部栈顶；否则放行给系统关闭 Activity
    BackHandler(enabled = navState.backStack.size > 1) {
        navState.pop()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = navState.currentKey.title) },
                navigationIcon = {
                    if (navState.backStack.size > 1) {
                        IconButton(onClick = { navState.pop() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回上一页",
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // 返回栈可视化监视器（直观展示 Navigation 3 状态驱动的本质）
            BackStackVisualizer(
                stack = navState.backStack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )

            // 导航显示区（NavDisplay 替代品：基于 AnimatedContent 的声明式渲染）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Nav3Display(navState = navState)
            }
        }
    }
}

/**
 * 声明式返回栈可视化控件
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BackStackVisualizer(
    stack: List<DemoNavKey>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Navigation 3 返回栈监控 (State List)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "深度: ${stack.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                stack.forEachIndexed { index, key ->
                    val isTop = index == stack.lastIndex
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "${index + 1}. ${key.title.substringBefore(":")}",
                                fontWeight = if (isTop) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = key.icon,
                                contentDescription = null,
                                modifier = Modifier.padding(2.dp),
                            )
                        },
                    )
                }
            }
        }
    }
}

/**
 * Navigation 3 核心渲染分发器
 *
 * 类似官方 NavDisplay，通过模式匹配将当前栈顶的 [DemoNavKey] 映射到对应的 Composable 页面，
 * 彻底消除了 Navigation 2 中复杂的 NavGraphBuilder 和 NavDestination 配置。
 */
@Composable
private fun Nav3Display(navState: Nav3State) {
    AnimatedContent(
        targetState = navState.currentKey,
        transitionSpec = {
            if (navState.navDirection >= 0) {
                // 前进：右进左出
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut(),
                )
            } else {
                // 后退：左进右出
                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut(),
                )
            }
        },
        label = "Nav3Transition",
    ) { targetKey ->
        when (targetKey) {
            is DemoNavKey.Home -> HomeScreen(
                onNavigateToTopic = { id, name, desc ->
                    navState.push(DemoNavKey.TopicDetail(id, name, desc))
                },
                onNavigateToSettings = {
                    navState.push(DemoNavKey.Settings)
                },
            )

            is DemoNavKey.TopicDetail -> TopicDetailScreen(
                topic = targetKey,
                onNavigateToAuthor = { id, name, role ->
                    navState.push(DemoNavKey.UserProfile(id, name, role))
                },
                onPop = { navState.pop() },
            )

            is DemoNavKey.UserProfile -> UserProfileScreen(
                user = targetKey,
                onPopToRoot = { navState.popToRoot() },
                onPop = { navState.pop() },
            )

            is DemoNavKey.Settings -> SettingsScreen(
                onPop = { navState.pop() },
            )
        }
    }
}

// ───────────────────────────────────────────
// 页面组件实现（与导航控制器解耦，仅接收 Key 与事件回调）
// ───────────────────────────────────────────

@Composable
private fun HomeScreen(
    onNavigateToTopic: (String, String, String) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Navigation 3 核心优势",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. 返回栈完全由纯 Compose 状态驱动，测试无需 Mock NavController\n" +
                        "2. 强类型 NavKey 作为唯一凭证，避免字符串拼接与类型转换隐患\n" +
                        "3. 页面彻底与导航框架解耦，天然适应平板/折叠屏双栏联动\n" +
                        "4. 支持直接对返回栈列表做函数式操作（filter, dropLast 等）",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Text(
            text = "推荐话题 (Topics)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )

        Button(
            onClick = {
                onNavigateToTopic(
                    "T001",
                    "Kotlin 2.0 & K2 编译器",
                    "K2 编译器带来前端吞吐量翻倍与 FIR 架构深度优化。",
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("进入 Kotlin 2.0 话题")
        }

        Button(
            onClick = {
                onNavigateToTopic(
                    "T002",
                    "Jetpack Compose Navigation 3",
                    "彻底拥抱声明式状态提升，告别旧时代的 Fragment 栈遗留设计。",
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("进入 Navigation 3 话题")
        }

        OutlinedButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("前往偏好设置")
        }
    }
}

@Composable
private fun TopicDetailScreen(
    topic: DemoNavKey.TopicDetail,
    onNavigateToAuthor: (String, String, String) -> Unit,
    onPop: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "话题 ID: ${topic.topicId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topic.topicTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = topic.topicDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        Text(
            text = "深层下钻操作",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )

        Button(
            onClick = {
                onNavigateToAuthor(
                    "A9527",
                    "Google Android 架构师",
                    "专注于 Jetpack Compose、MVI 与多模块架构实践。",
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("查看主讲人资料 (压入二级子页)")
        }

        OutlinedButton(
            onClick = onPop,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("返回上一级 (Pop)")
        }
    }
}

@Composable
private fun UserProfileScreen(
    user: DemoNavKey.UserProfile,
    onPopToRoot: () -> Unit,
    onPop: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(24.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = user.userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "UID: ${user.userId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onPopToRoot,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("直接回到首页 (Pop to Root 清栈)")
        }

        OutlinedButton(
            onClick = onPop,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("返回上一级 (Pop)")
        }
    }
}

@Composable
private fun SettingsScreen(onPop: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "声明式状态演示",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "在 Navigation 3 体系中，页面不需要接收 NavController，仅向外抛出明确的导航意图事件 (Lambdas)，极大地提高了 Composable 的可复用度与独立可测性。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        OutlinedButton(
            onClick = onPop,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("返回上一级")
        }
    }
}
