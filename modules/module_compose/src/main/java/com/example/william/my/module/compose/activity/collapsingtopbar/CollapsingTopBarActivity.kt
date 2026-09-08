package com.example.william.my.module.compose.activity.collapsingtopbar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ui.theme.MyApplicationTheme

/**
 * CollapsingTopBar — Material 3 折叠吸顶标题栏
 *
 * 在 Compose 中替代传统 View 体系的 CoordinatorLayout + AppBarLayout + CollapsingToolbarLayout。
 *
 * 核心机制：
 * 1. 嵌套滚动连接：[Modifier.nestedScroll] 将列表的嵌套滑动事件分发给 [TopAppBarScrollBehavior]；
 * 2. 折叠吸顶行为：[TopAppBarDefaults.exitUntilCollapsedScrollBehavior]，向上滑动时标题视差折叠为标准高度并吸顶；
 * 3. 展开大标题：使用 [LargeTopAppBar]，折叠时平滑过渡为普通单行 TopAppBar 样式；
 * 4. 滚动状态持久化：[rememberTopAppBarState] 保持滑动偏移与吸顶状态。
 *
 * 官方文档：
 * https://developer.android.google.cn/develop/ui/compose/components/app-bars
 */
@Route(path = RouterPath.Compose.CollapsingTopBar)
class CollapsingTopBarActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                    rememberTopAppBarState(),
                )

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        LargeTopAppBar(
                            title = {
                                Text("Collapsing TopBar")
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "返回",
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { }) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = "收藏",
                                    )
                                }
                                IconButton(onClick = { }) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = "分享",
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior,
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                        )
                    },
                ) { innerPadding ->
                    CollapsingContent(innerPadding)
                }
            }
        }
    }

    @Composable
    private fun CollapsingContent(innerPadding: PaddingValues) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CoordinatorLayout 演进机制说明",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "传统 View 体系中需要 CoordinatorLayout + AppBarLayout + CollapsingToolbarLayout 联动。\n" +
                                "在 Compose 中，通过 Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) " +
                                "将滑动事件传递给 TopAppBar，以极少代码实现更平滑的折叠吸顶。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }

            items(25) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Text(
                        text = "列表内容项 #${index + 1}（上滑观察 LargeTopAppBar 折叠与吸顶效果）",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
