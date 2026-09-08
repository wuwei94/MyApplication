package com.example.william.my.module.compose.activity.basic

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.component.TextExample
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme

/**
 * Text — 文本组件与排版实战
 *
 * 演示 Compose Material 3 体系下 Text 的核心能力与排版特性：
 * 1. 独立被测组件挂载：集成 [TextExample]（供 Roborazzi 截图测试）；
 * 2. 富文本构建：[buildAnnotatedString] 混排颜色、背景色、字重、字距与超链接 [LinkAnnotation]；
 * 3. 文本选择容器：[SelectionContainer] 与局部禁用选择 [DisableSelection]；
 * 4. 文本溢出与截断控制：[TextOverflow.Ellipsis]、[TextOverflow.Clip] 与行数限制；
 * 5. Material 3 标准排版系统：展示 Typography 各级别文字阶梯。
 *
 * 官方文档：
 * https://developer.android.google.cn/develop/ui/compose/text/style-text
 */
@Route(path = RouterPath.Compose.Text)
class TextActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                TextGalleryScreen(
                    onToast = { message ->
                        Toast.makeText(this@TextActivity, message, Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }

    @Composable
    private fun TextGalleryScreen(onToast: (String) -> Unit) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. 基准测试组件区域
            SectionCard(title = "1. Roborazzi 截图基准组件 (TextExample)") {
                TextExample(
                    str = "Hello Text（点击触发回调日志）",
                    onClick = { onToast("TextExample 被点击") },
                )
            }

            // 2. 富文本 AnnotatedString 与超链接
            SectionCard(title = "2. AnnotatedString 富文本与超链接") {
                val annotatedText = buildAnnotatedString {
                    append("Compose 支持在单行中混排 ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("彩色高亮")
                    }
                    append("、")
                    withStyle(style = SpanStyle(background = Color(0xFFFFF59D), color = Color.Black)) {
                        append("背景强调")
                    }
                    append("、")
                    withStyle(
                        style = SpanStyle(
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                        ),
                    ) {
                        append("等宽字距")
                    }
                    append("，以及原生 ")
                    withLink(
                        LinkAnnotation.Url(
                            url = "https://developer.android.google.cn/jetpack/compose",
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.secondary,
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            ),
                        ),
                    ) {
                        append("官方文档链接")
                    }
                    append("。")
                }

                Text(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                )
            }

            // 3. 文本长按复制与局部禁用
            SectionCard(title = "3. SelectionContainer 长按选择与局部禁止") {
                SelectionContainer {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "本段文字处于 SelectionContainer 内，长按可以自由选择与复制。",
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        DisableSelection {
                            Text(
                                text = "【局部 DisableSelection】这段受保护的文本无法被选中或复制。",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }

                        Text(
                            text = "本段又恢复了可选状态，继续支持长按框选复制。",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            // 4. 文字溢出截断策略
            SectionCard(title = "4. 文字溢出与截断控制") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "单行末尾省略号 (TextOverflow.Ellipsis)：" +
                            "这是一段极长的单行文字，用于展示自动截断效果，多余内容会被省略号代替。",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Text(
                        text = "两行限制截断 (maxLines = 2)：" +
                            "在有限的卡片空间内，合理设置 maxLines 与 overflow 可以确保界面不被超长内容撑破变形，保持整洁统一的视觉体验。",
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // 5. Material 3 Typography 体系
            SectionCard(title = "5. Material 3 Typography 阶梯展示") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Headline Small", style = MaterialTheme.typography.headlineSmall)
                    Text("Title Medium", style = MaterialTheme.typography.titleMedium)
                    Text("Body Large (主要正文)", style = MaterialTheme.typography.bodyLarge)
                    Text("Body Medium (次要描述)", style = MaterialTheme.typography.bodyMedium)
                    Text("Label Small (标签辅助)", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    @Composable
    private fun SectionCard(
        title: String,
        content: @Composable () -> Unit,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                content()
            }
        }
    }
}
