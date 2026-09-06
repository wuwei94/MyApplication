package com.example.william.my.module.arch.ssot

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseActivity
import com.example.william.my.module.arch.ssot.data.OfflineFirstIntent
import com.example.william.my.module.arch.ssot.data.OfflineFirstUiEffect
import com.example.william.my.module.arch.ssot.data.OfflineFirstUiState
import com.example.william.my.module.arch.ssot.viewmodel.OfflineFirstViewModel
import com.loren.component.view.composesmartrefresh.SmartSwipeRefresh
import com.loren.component.view.composesmartrefresh.SmartSwipeStateFlag
import com.loren.component.view.composesmartrefresh.ThresholdScrollStrategy
import com.loren.component.view.composesmartrefresh.rememberSmartSwipeRefreshState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 离线优先与单一真实来源（Offline-First & SSOT）架构模式示例
 *
 * 对齐 Google 官方 [Now in Android](https://github.com/android/nowinandroid) 数据层最佳实践：
 * 1. 【SSOT 唯一数据源】：UI 永远只观察 Room 数据库通过 Flow 暴露的流，ViewModel 与 UI 绝不直接持有网络 DTO；
 * 2. 【网络写同步（Write-Only Sync）】：网络请求拉取后直接写入 Room 本地数据库，通过 Room 的 InvalidationTracker 自动推流触发 UI 重组；
 * 3. 【离线高可用（Offline-First）】：进入页面即刻展现 Room 本地缓存，断网或无网络环境下依然秒开可用；
 * 4. 【全链路可观测互动】：提供【网络同步】、【本地插入】、【清空数据库】操作，读者可直观验证 UI 数据变更全由 Room 驱动。
 */
@Route(path = RouterPath.Arch.OfflineFirst)
class OfflineFirstActivity : BaseActivity() {

    private val mViewModel: OfflineFirstViewModel by viewModels {
        OfflineFirstViewModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by mViewModel.uiState.collectAsState()
            val refreshState = rememberSmartSwipeRefreshState().apply {
                this.needFirstRefresh = true
            }
            val scrollState = rememberLazyListState()
            var displayRefreshFlag by remember { mutableStateOf<SmartSwipeStateFlag?>(null) }

            // 下拉刷新阈值策略
            with(LocalDensity.current) {
                refreshState.dragHeaderIndicatorStrategy = ThresholdScrollStrategy.UnLimited
                refreshState.flingHeaderIndicatorStrategy = ThresholdScrollStrategy.None
            }

            // 监听单次副作用事件（Toast / 同步状态）
            LaunchedEffect(Unit) {
                mViewModel.effect.collect { effect ->
                    when (effect) {
                        is OfflineFirstUiEffect.ShowToast -> {
                            Toast.makeText(this@OfflineFirstActivity, effect.message, Toast.LENGTH_SHORT).show()
                        }

                        is OfflineFirstUiEffect.SyncComplete -> {
                            val flag = if (effect.isSuccess) {
                                SmartSwipeStateFlag.SUCCESS
                            } else {
                                SmartSwipeStateFlag.ERROR
                            }
                            displayRefreshFlag = flag
                            delay(500)
                            refreshState.refreshFlag = flag
                            delay(300)
                            displayRefreshFlag = null
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. 顶部架构拓扑教学说明卡片
                    ArchitectureBannerCard()

                    // 2. SSOT 教学互动控制板（状态指标与验证按钮）
                    SsotControlDashboard(
                        uiState = uiState,
                        onSyncClick = { mViewModel.sendIntent(OfflineFirstIntent.Sync(0)) },
                        onInsertClick = {
                            val mockTitle = "本地离线笔记 #${System.currentTimeMillis() % 1000}"
                            mViewModel.sendIntent(OfflineFirstIntent.AddLocalArticle(mockTitle))
                        },
                        onClearClick = { mViewModel.sendIntent(OfflineFirstIntent.ClearLocalCache) },
                    )

                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))

                    // 3. 列表内容区（支持下拉刷新，完全展示 Room 数据库流数据）
                    SmartSwipeRefresh(
                        modifier = Modifier.fillMaxSize(),
                        onRefresh = {
                            mViewModel.sendIntent(OfflineFirstIntent.Sync(0))
                        },
                        state = refreshState,
                        headerIndicator = {
                            ClassicsRefreshHeader(displayRefreshFlag ?: refreshState.refreshFlag)
                        },
                        contentScrollState = scrollState,
                    ) {
                        if (uiState.articles.isEmpty()) {
                            EmptyDataView()
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = scrollState,
                            ) {
                                items(
                                    items = uiState.articles,
                                    key = { it.id },
                                ) { item ->
                                    ArticleDetailItemView(item = item)
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = Color(0xFFF0F0F0),
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 顶部架构原理解析卡片
     */
    @Composable
    private fun ArchitectureBannerCard() {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Now in Android 离线优先架构（SSOT 唯一数据源）",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "网络 API ──(Sync 写同步)──▶ Room 数据库 ──(Flow 响应式流)──▶ UI 呈现",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0284C7),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• 读：UI 仅订阅 Room Flow，UI 状态与 Room 实时强绑定\n• 写：网络请求只负责更新 Room，不直接透传给 UI\n• 离线：进入页面优先展示 Room 缓存，断网秒开无阻碍",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 15.sp,
                )
            }
        }
    }

    /**
     * SSOT 互动验证看板
     */
    @Composable
    private fun SsotControlDashboard(
        uiState: OfflineFirstUiState,
        onSyncClick: () -> Unit,
        onInsertClick: () -> Unit,
        onClearClick: () -> Unit,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // 实时状态指标栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Room 实时缓存: ${uiState.cacheCount} 条",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                    )

                    val syncStatusText = if (uiState.isSyncing) "同步中..." else "空闲"
                    val syncStatusColor = if (uiState.isSyncing) Color(0xFFEAB308) else Color(0xFF16A34A)
                    Text(
                        text = "状态: $syncStatusText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = syncStatusColor,
                    )

                    val timeText = uiState.lastSyncTime?.let {
                        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(it))
                    } ?: "未同步"
                    Text(
                        text = "同步: $timeText",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 互动验证按钮组
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onSyncClick,
                        modifier = Modifier.weight(1f).height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("网络同步", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onInsertClick,
                        modifier = Modifier.weight(1f).height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("本地插入", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onClearClick,
                        modifier = Modifier.weight(1f).height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("清空缓存", fontSize = 11.sp)
                    }
                }
            }
        }
    }

    /**
     * 空数据提示视图
     */
    @Composable
    private fun EmptyDataView() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Room 本地数据库暂无数据",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "下拉可拉取网络并同步写入 Room；\n或点击【本地插入】直接验证 Room 流响应式更新。",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                )
            }
        }
    }

    /**
     * 文章列表项视图
     */
    @Composable
    private fun ArticleDetailItemView(item: ArticleDetailData) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFE2E8F0), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "Room",
                    fontSize = 10.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = item.title,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
            )
        }
    }

    /**
     * 下拉刷新 ClassicsHeader
     */
    @Composable
    private fun ClassicsRefreshHeader(
        refreshFlag: SmartSwipeStateFlag,
        modifier: Modifier = Modifier,
    ) {
        val (titleText, showProgress) = when (refreshFlag) {
            SmartSwipeStateFlag.IDLE,
            SmartSwipeStateFlag.TIPS_DOWN,
            -> "下拉可以刷新" to false
            SmartSwipeStateFlag.TIPS_RELEASE -> "释放立即刷新" to false
            SmartSwipeStateFlag.REFRESHING -> "正在从网络同步并写入 Room..." to true
            SmartSwipeStateFlag.SUCCESS -> "同步完成" to false
            SmartSwipeStateFlag.ERROR -> "同步失败" to false
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

                Text(
                    text = titleText,
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                )
            }
        }
    }
}
