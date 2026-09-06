package com.example.william.my.module.arch.compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseActivity
import com.example.william.my.module.arch.compose.data.ArticleComposeIntent
import com.example.william.my.module.arch.compose.data.ArticleComposeUiEffect
import com.example.william.my.module.arch.compose.viewmodel.ArticleComposeViewModel
import com.loren.component.view.composesmartrefresh.SmartSwipeRefresh
import com.loren.component.view.composesmartrefresh.SmartSwipeStateFlag
import com.loren.component.view.composesmartrefresh.ThresholdScrollStrategy
import com.loren.component.view.composesmartrefresh.rememberSmartSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Compose MVI — Jetpack Compose 现代化响应式 MVI 架构
 *
 * 结合声明式 UI（Jetpack Compose）与单向数据流（Model-View-Intent）的最佳实践：
 * 1. Intent（意图）：由 UI 事件触发并通过协程 Channel 发送给 ViewModel（如 Refresh / LoadMore）；
 * 2. State（状态）：ViewModel 汇聚业务与分页数据，通过不可变的 StateFlow 单向驱动 Compose 渲染；
 * 3. Effect（副作用）：单次瞬时事件（如刷新/加载完成、网络错误 Toast）通过独立 Channel 管道分发，避免重组重放与状态去重问题；
 * 4. 下拉刷新：使用 SmartRefresh Compose（[SmartSwipeRefresh]）配合经典样式 [ClassicsRefreshHeader] / [ClassicsRefreshFooter]，完成时序与传统 XML 架构页面（MviActivity）的 ClassicsHeader（保留 500ms 完成提示再平滑收起）保持完全一致；
 * 5. UI 与基类规范：继承 [BaseActivity] 保证沉浸式状态栏与屏幕密度适配与各架构示例统一，列表项高度 48dp 且文本居中对齐（14sp），与传统 View 架构示例（arch_item_recycler）保持视觉完全一致。
 */
@Route(path = RouterPath.Arch.ComposeMVI)
class ComposeMviActivity : BaseActivity() {

    private val mViewModel: ArticleComposeViewModel by viewModels {
        ArticleComposeViewModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val refreshState = rememberSmartSwipeRefreshState().apply {
                this.needFirstRefresh = true
            }
            val scrollState = rememberLazyListState()
            val uiState by mViewModel.state.collectAsState()

            var displayRefreshFlag by remember { mutableStateOf<SmartSwipeStateFlag?>(null) }
            var displayLoadMoreFlag by remember { mutableStateOf<SmartSwipeStateFlag?>(null) }

            // 快速滚动头尾允许的阈值策略
            with(LocalDensity.current) {
                refreshState.dragHeaderIndicatorStrategy = ThresholdScrollStrategy.UnLimited
                refreshState.dragFooterIndicatorStrategy =
                    ThresholdScrollStrategy.Fixed(160.dp.toPx())
                refreshState.flingHeaderIndicatorStrategy = ThresholdScrollStrategy.None
                refreshState.flingFooterIndicatorStrategy =
                    ThresholdScrollStrategy.Fixed(80.dp.toPx())
            }

            // 监听单次副作用事件（如刷新完成、加载更多完成、Toast 提示）
            LaunchedEffect(Unit) {
                mViewModel.effect.collect { effect ->
                    when (effect) {
                        is ArticleComposeUiEffect.RefreshComplete -> {
                            val flag = if (effect.isSuccess) {
                                SmartSwipeStateFlag.SUCCESS
                            } else {
                                SmartSwipeStateFlag.ERROR
                            }
                            displayRefreshFlag = flag
                            // 停留展示 500ms 刷新完成状态与对勾，与 SmartRefreshLayout 的 finishDuration 保持完全一致
                            delay(500)
                            refreshState.refreshFlag = flag
                            delay(300)
                            displayRefreshFlag = null
                        }

                        is ArticleComposeUiEffect.LoadMoreComplete -> {
                            val flag = if (effect.isSuccess) {
                                SmartSwipeStateFlag.SUCCESS
                            } else {
                                SmartSwipeStateFlag.ERROR
                            }
                            displayLoadMoreFlag = flag
                            // 停留展示 500ms 加载完成状态，与 SmartRefreshLayout 的 finishDuration 保持完全一致
                            delay(500)
                            refreshState.loadMoreFlag = flag
                            delay(300)
                            displayLoadMoreFlag = null
                        }

                        is ArticleComposeUiEffect.ShowToast -> {
                            Toast.makeText(
                                this@ComposeMviActivity,
                                effect.message,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                SmartSwipeRefresh(
                    modifier = Modifier.fillMaxSize(),
                    onRefresh = {
                        lifecycleScope.launch {
                            mViewModel.intent.send(ArticleComposeIntent.Refresh)
                        }
                    },
                    onLoadMore = {
                        lifecycleScope.launch {
                            mViewModel.intent.send(ArticleComposeIntent.LoadMore)
                        }
                    },
                    state = refreshState,
                    headerIndicator = {
                        ClassicsRefreshHeader(displayRefreshFlag ?: refreshState.refreshFlag)
                    },
                    footerIndicator = {
                        ClassicsRefreshFooter(displayLoadMoreFlag ?: refreshState.loadMoreFlag)
                    },
                    contentScrollState = scrollState,
                ) {
                    CompositionLocalProvider {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = scrollState,
                        ) {
                            items(
                                items = uiState.articles,
                                key = { it.id },
                            ) { item ->
                                ArticleItemView(item = item)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 与 SmartRefreshLayout 的 ClassicsHeader 对齐的 Compose 经典下拉刷新头
     */
    @Composable
    private fun ClassicsRefreshHeader(
        refreshFlag: SmartSwipeStateFlag,
        modifier: Modifier = Modifier,
    ) {
        var updateTimeText by remember {
            mutableStateOf(SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date()))
        }
        LaunchedEffect(refreshFlag) {
            if (refreshFlag == SmartSwipeStateFlag.SUCCESS) {
                updateTimeText =
                    SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date())
            }
        }

        val (titleText, showProgress) = when (refreshFlag) {
            SmartSwipeStateFlag.IDLE,
            SmartSwipeStateFlag.TIPS_DOWN,
            -> "下拉可以刷新" to false
            SmartSwipeStateFlag.TIPS_RELEASE -> "释放立即刷新" to false
            SmartSwipeStateFlag.REFRESHING -> "正在刷新..." to true
            SmartSwipeStateFlag.SUCCESS -> "刷新完成" to false
            SmartSwipeStateFlag.ERROR -> "刷新失败" to false
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(66.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (showProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .padding(2.dp),
                        color = Color(0xFF666666),
                        strokeWidth = 2.dp,
                    )
                } else {
                    val icon = when (refreshFlag) {
                        SmartSwipeStateFlag.SUCCESS -> Icons.Default.Check
                        SmartSwipeStateFlag.ERROR -> Icons.Default.Close
                        SmartSwipeStateFlag.TIPS_RELEASE -> Icons.Default.ArrowUpward
                        else -> Icons.Default.ArrowDownward
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(18.dp),
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = titleText,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "上次更新 $updateTimeText",
                        fontSize = 11.sp,
                        color = Color(0xFF999999),
                    )
                }
            }
        }
    }

    /**
     * 与 SmartRefreshLayout 的 ClassicsFooter 对齐的 Compose 经典上拉加载尾
     */
    @Composable
    private fun ClassicsRefreshFooter(
        loadMoreFlag: SmartSwipeStateFlag,
        modifier: Modifier = Modifier,
    ) {
        val (titleText, showProgress) = when (loadMoreFlag) {
            SmartSwipeStateFlag.IDLE,
            SmartSwipeStateFlag.TIPS_DOWN,
            -> "上拉加载更多" to false
            SmartSwipeStateFlag.TIPS_RELEASE -> "释放立即加载" to false
            SmartSwipeStateFlag.REFRESHING -> "正在加载..." to true
            SmartSwipeStateFlag.SUCCESS -> "加载完成" to false
            SmartSwipeStateFlag.ERROR -> "加载失败" to false
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (showProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(2.dp),
                        color = Color(0xFF666666),
                        strokeWidth = 2.dp,
                    )
                } else {
                    val icon = when (loadMoreFlag) {
                        SmartSwipeStateFlag.SUCCESS -> Icons.Default.Check
                        SmartSwipeStateFlag.ERROR -> Icons.Default.Close
                        SmartSwipeStateFlag.TIPS_RELEASE -> Icons.Default.ArrowDownward
                        else -> Icons.Default.ArrowUpward
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(16.dp),
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = titleText,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                )
            }
        }
    }

    @Composable
    private fun ArticleItemView(item: ArticleDetailData) {
        // 与 arch_item_recycler 保持一致：高 48dp，文字水平垂直居中，字号 14sp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = item.title ?: "",
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
