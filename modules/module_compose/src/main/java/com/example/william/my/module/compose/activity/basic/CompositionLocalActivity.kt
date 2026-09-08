package com.example.william.my.module.compose.activity.basic

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme

/**
 * CompositionLocal 是通过组合隐式向下传递数据的工具
 *
 * 核心机制与选型对比：
 * 1. [compositionLocalOf]：细粒度重组追踪。当提供的数值改变时，只有实际读取了 [Local.current] 的可组合项会发生重组，
 *    适合频繁变化的数据；
 * 2. [staticCompositionLocalOf]：全量重组。当提供的数值改变时，整个 Provider 作用域子树都会无条件全量重组，
 *    适合极少变更的设计系统标尺（如主题配色、间距规范），读取时无需建立订阅依赖，读取性能更优；
 * 3. 常见内置 Local：展示 [LocalContext]、[LocalDensity]、[LocalConfiguration] 等系统级环境变量的获取与应用。
 */
@Route(path = RouterPath.Compose.CompositionLocal)
class CompositionLocalActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CompositionLocalScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    /**
     * 用户数据
     *
     * 用于演示 CompositionLocal 值传递的示例数据。
     */
    data class User(val name: String)

    // 编译器会提示变量名应该以“Local”为前缀
    private val LocalUser = compositionLocalOf { User("张三") }
    // 不想提供或无法提供有意义的默认值，可以直接抛异常。
    // val LocalUser = compositionLocalOf { error("LocalUser没有提供值！") }

    @Composable
    private fun CompositionLocalScreen(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. 基础数据穿透与层级覆盖
            BasicScopeOverrideCard()

            // 2. compositionLocalOf vs staticCompositionLocalOf 重组机理对比
            RecompositionScopeComparisonCard()

            // 3. 常用内置 Local* 环境变量
            BuiltInLocalsCard()

            // 4. 自定义设计系统规范穿透（Spacing 规范）
            CustomDesignSystemCard()
        }
    }

    /**
     * 1. 基础隐式向下穿透与覆盖重写卡片
     */
    @Composable
    private fun BasicScopeOverrideCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "1. 基础隐式向下传递与作用域覆盖",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "通过 CompositionLocalProvider 跨多层级传递数据，子树随时可重新 provides 局部覆盖，不影响同级或外层。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // 外层作用域
                val rootUser = LocalUser.current
                Text("外层默认值 (根节点): ${rootUser.name}")

                // 嵌套覆盖作用域
                CompositionLocalProvider(LocalUser provides User("李四 (局部覆盖)")) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val innerUser = LocalUser.current
                            Text(
                                text = "子树作用域读取: ${innerUser.name}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 2. compositionLocalOf vs staticCompositionLocalOf 重组对比
     */
    @Composable
    private fun RecompositionScopeComparisonCard() {
        var dynamicCounter by remember { mutableIntStateOf(0) }
        var staticCounter by remember { mutableIntStateOf(0) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "2. compositionLocalOf vs staticCompositionLocalOf",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "• compositionLocalOf: 值变化时仅重组读取了它的 Composable；\n• staticCompositionLocalOf: 值变化时整棵子树全量重组，适合主题配色等极少变动项。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { dynamicCounter++ },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("改变动态值 (+1)")
                    }
                    Button(
                        onClick = { staticCounter++ },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("改变静态值 (+1)")
                    }
                }

                // compositionLocalOf 演示容器
                CompositionLocalProvider(LocalDynamicValue provides dynamicCounter) {
                    ScopeContainerBox(title = "compositionLocalOf 子容器") {
                        val currentVal = LocalDynamicValue.current
                        Text("读取到的数值: $currentVal", fontWeight = FontWeight.SemiBold)
                    }
                }

                // staticCompositionLocalOf 演示容器
                CompositionLocalProvider(LocalStaticValue provides staticCounter) {
                    ScopeContainerBox(title = "staticCompositionLocalOf 子容器") {
                        val currentVal = LocalStaticValue.current
                        Text("读取到的数值: $currentVal", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    @Composable
    private fun ScopeContainerBox(title: String, content: @Composable () -> Unit) {
        var containerRecomposeCount by remember { mutableIntStateOf(0) }
        SideEffect { containerRecomposeCount++ }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "容器重组: $containerRecomposeCount 次",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                content()
            }
        }
    }

    /**
     * 3. 常用内置 Local* 演示卡片
     */
    @Composable
    private fun BuiltInLocalsCard() {
        val context = LocalContext.current
        val density = LocalDensity.current
        val configuration = LocalConfiguration.current

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "3. 常用内置 Local* 环境变量",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text("• LocalContext: ${context.javaClass.simpleName}", style = MaterialTheme.typography.bodySmall)
                Text("• LocalDensity: 屏幕密度系数 = ${density.density}, 字体缩放 = ${density.fontScale}", style = MaterialTheme.typography.bodySmall)
                val orientation = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) "竖屏" else "横屏"
                Text("• LocalConfiguration: 屏幕宽高 = ${configuration.screenWidthDp}dp × ${configuration.screenHeightDp}dp ($orientation)", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    /**
     * 4. 自定义设计系统规范穿透
     */
    @Composable
    private fun CustomDesignSystemCard() {
        var isCompact by remember { mutableStateOf(false) }
        val spacing = if (isCompact) {
            AppSpacing(cardPadding = 8.dp, itemGap = 4.dp)
        } else {
            AppSpacing(cardPadding = 16.dp, itemGap = 12.dp)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "4. 自定义设计系统规范（AppSpacing 注入）",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "使用 staticCompositionLocalOf 透传自定义设计系统规范（如外边距、内边距、圆角等），支持全局或局部动态换肤。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedButton(onClick = { isCompact = !isCompact }) {
                    Text(if (isCompact) "切换为宽松间距 (16dp)" else "切换为紧凑间距 (8dp)")
                }

                CompositionLocalProvider(LocalAppSpacing provides spacing) {
                    DesignSystemSampleConsumer()
                }
            }
        }
    }

    @Composable
    private fun DesignSystemSampleConsumer() {
        val spacing = LocalAppSpacing.current
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(spacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(spacing.itemGap),
            ) {
                Text(
                    text = "设计系统卡片（根据 LocalAppSpacing 自适应）",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "当前配置: 内边距 = ${spacing.cardPadding}, 元素间距 = ${spacing.itemGap}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

/**
 * 自定义设计系统间距实体
 */
data class AppSpacing(val cardPadding: Dp = 16.dp, val itemGap: Dp = 8.dp)

// 使用 staticCompositionLocalOf 定义极少变动的设计规范
private val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }

// 用于机理测试的动态与静态 Local 变量
private val LocalDynamicValue = compositionLocalOf { 0 }
private val LocalStaticValue = staticCompositionLocalOf { 0 }
