package com.example.william.my.module.compose.activity.basic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlinx.coroutines.launch

/**
 * HorizontalPager — 水平分页
 *
 * 演示 Compose 中 HorizontalPager 的分页滑动与程序化滚动。
 *
 * 核心特性：
 * 1. 声明式分页容器
 * 2. 支持手势与程序化滚动
 *
 * 适用场景：
 * - 图片轮播
 * - 引导页、卡片流
 *
 * https://developer.android.google.cn/jetpack/compose/layouts/pager
 */
@Route(path = RouterPath.Compose.HorizontalPager)
class HorizontalPagerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HorizontalPagerExample()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Preview(showBackground = true)
    @Composable
    fun HorizontalPagerExample() {
        Box {
            val pagerState = rememberPagerState(pageCount = { 10 })

            HorizontalPager(
                state = pagerState,
                // 禁用手势滑动
                userScrollEnabled = true,
            ) { page ->
                // 页面内容
                Text(
                    text = "Page: $page",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                )
            }

            // 滚动到指定页
            val coroutineScope = rememberCoroutineScope()
            Button(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = {
                    coroutineScope.launch {
                        // 调用 pagerState 的滚动方法
                        pagerState.scrollToPage(5)
                    }
                },
            ) {
                Text("Jump to Page 5")
            }
        }
    }
}
