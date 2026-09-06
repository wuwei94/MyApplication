package com.example.william.my.module.compose.activity.basic

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.component.TextExample

/**
 * Text 组件基础样式示例页。
 *
 * 展示内容集中在 [TextExample]（顶层 Composable），本页只负责挂载与点击日志，
 * 使同一组件可被 Roborazzi 截图测试在任意主题下渲染。
 */
@Route(path = RouterPath.Compose.Text)
class TextActivity : ComponentActivity() {

    private val TAG = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextExample(
                str = "Hello Text",
                onClick = { Log.e(TAG, "clickable") },
            )
        }
    }
}
