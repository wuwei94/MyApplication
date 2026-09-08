package com.example.william.my.module.compose.activity.theme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Theme — Material 3 动态主题与深浅色模式示例页
 *
 * 聚合展示 Compose 中现代样式系统的设计精髓：
 * 1. **深浅色模式热切换**：浅色 (Light)、深色 (Dark) 以及跟随系统 (System Default) 即时响应；
 * 2. **多套品牌配色矩阵**：极简经典蓝、活力翡翠绿、优雅日落橙等多套 Material 3 ColorScheme 调色板动态替换；
 * 3. **Typography 字体排版阶梯**：从 Display、Headline、Title、Body 到 Label 的规范展示；
 * 4. **语义化色彩容器**：展示 primary, secondary, tertiary 与 surfaceVariant 容器层级映射。
 *
 * https://developer.android.google.cn/develop/ui/compose/designsystems/material3
 */
@Route(path = RouterPath.Compose.Theme)
class ThemeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ThemePlaygroundScreen()
        }
    }

    private enum class ThemeMode(val label: String) {
        SYSTEM("跟随系统"),
        LIGHT("浅色模式"),
        DARK("深色模式"),
    }

    private enum class BrandPalette(
        val label: String,
        val primaryColor: Color,
        val lightScheme: ColorScheme,
        val darkScheme: ColorScheme,
    ) {
        BLUE(
            label = "科技蓝",
            primaryColor = Color(0xFF0061A4),
            lightScheme = lightColorScheme(
                primary = Color(0xFF0061A4),
                secondary = Color(0xFF535F70),
                tertiary = Color(0xFF6B5778),
            ),
            darkScheme = darkColorScheme(
                primary = Color(0xFF9ECAFF),
                secondary = Color(0xFFBBC7DB),
                tertiary = Color(0xFFD6BEE4),
            ),
        ),
        GREEN(
            label = "活力绿",
            primaryColor = Color(0xFF006D44),
            lightScheme = lightColorScheme(
                primary = Color(0xFF006D44),
                secondary = Color(0xFF4F6354),
                tertiary = Color(0xFF3C6472),
            ),
            darkScheme = darkColorScheme(
                primary = Color(0xFF63DB98),
                secondary = Color(0xFFB5CCBA),
                tertiary = Color(0xFFA4CDDC),
            ),
        ),
        ORANGE(
            label = "日落橙",
            primaryColor = Color(0xFF984715),
            lightScheme = lightColorScheme(
                primary = Color(0xFF984715),
                secondary = Color(0xFF775747),
                tertiary = Color(0xFF675F2F),
            ),
            darkScheme = darkColorScheme(
                primary = Color(0xFFFFB68F),
                secondary = Color(0xFFE6BEAC),
                tertiary = Color(0xFFD2C78E),
            ),
        ),
    }

    @Composable
    private fun ThemePlaygroundScreen() {
        var currentMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
        var currentPalette by remember { mutableStateOf(BrandPalette.BLUE) }

        val isDark = when (currentMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

        val activeColorScheme = if (isDark) {
            currentPalette.darkScheme
        } else {
            currentPalette.lightScheme
        }

        // 将选中的动态色彩注入当前树
        MaterialTheme(colorScheme = activeColorScheme) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ThemePlaygroundContent(
                        currentMode = currentMode,
                        onModeChange = { currentMode = it },
                        currentPalette = currentPalette,
                        onPaletteChange = { currentPalette = it },
                        isDark = isDark,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    @Composable
    private fun ThemePlaygroundContent(
        currentMode: ThemeMode,
        onModeChange: (ThemeMode) -> Unit,
        currentPalette: BrandPalette,
        onPaletteChange: (BrandPalette) -> Unit,
        isDark: Boolean,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. 深浅色模式控制
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. 深浅色模式即时切换 (当前状态: ${if (isDark) "深色" else "浅色"})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = currentMode == mode,
                                onClick = { onModeChange(mode) },
                                label = { Text(mode.label) },
                            )
                        }
                    }
                }
            }

            // 2. 品牌主题配色调色板
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. 品牌配色方案调色板",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BrandPalette.entries.forEach { palette ->
                            FilterChip(
                                selected = currentPalette == palette,
                                onClick = { onPaletteChange(palette) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(palette.primaryColor),
                                    )
                                },
                                label = { Text(palette.label) },
                            )
                        }
                    }
                }
            }

            // 3. 语义化颜色预览看板
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Material 3 语义色彩层级",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ColorChipItem("Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, Modifier.weight(1f))
                        ColorChipItem("Secondary", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary, Modifier.weight(1f))
                        ColorChipItem("Tertiary", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary, Modifier.weight(1f))
                    }
                }
            }

            // 4. Typography 字体层级阶梯
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "4. Typography 排版阶梯展示",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "HeadlineMedium · 28sp 页面大标题", style = MaterialTheme.typography.headlineMedium)
                    Text(text = "TitleMedium · 16sp 卡片中标题", style = MaterialTheme.typography.titleMedium)
                    Text(text = "BodyLarge · 16sp 标准正文内容展示，行高舒展", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "LabelSmall · 11sp 极简标签辅助说明文字", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    @Composable
    private fun ColorChipItem(name: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = name, color = fg, style = MaterialTheme.typography.labelMedium)
        }
    }
}
