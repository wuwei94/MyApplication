package com.example.william.my.module.compose.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Text 组件基础样式示例：下划线 + 斜体 + 单行截断。
 *
 * 提取为顶层函数（而非 Activity 成员）以便：
 * 1. 页面只负责 setContent 挂载；
 * 2. Roborazzi 截图测试可以直接以任意主题组合渲染本组件。
 *
 * @param str 展示文本
 * @param onClick 文本点击回调（页面侧打点日志，测试侧为空实现）
 */
@Composable
fun TextExample(
    str: String,
    onClick: () -> Unit = {},
) {
    Text(
        text = str,
        modifier = Modifier
            .padding(6.dp)
            .clickable { onClick() },
        color = Color.Black,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        textDecoration = TextDecoration.Underline,
        fontStyle = FontStyle.Italic,
        maxLines = 1,
    )
}
