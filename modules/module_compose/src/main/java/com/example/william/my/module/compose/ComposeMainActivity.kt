package com.example.william.my.module.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.exception.HandlerException
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Compose模块入口页
 *
 * 展示 Jetpack Compose 基础组件、手势、导航等示例列表。
 */
@Route(path = RouterPath.Compose.Main)
class ComposeMainActivity : ComponentActivity() {

    private val routerItems: ArrayList<RouterItem> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        buildRouterItems()

        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                LazyColumnExample(routerItems, Modifier.padding(innerPadding))
            }
        }
    }

    private fun buildRouterItems() {
        routerItems.add(RouterItem("Compose", RouterPath.Compose.ComposeActivity))
        routerItems.add(RouterItem("ComposeView", RouterPath.Compose.ComposeViewActivity))

        routerItems.add(RouterItem("Text", RouterPath.Compose.Text))
        routerItems.add(RouterItem("Button", RouterPath.Compose.Button))
        routerItems.add(RouterItem("Image", RouterPath.Compose.Image))
        routerItems.add(RouterItem("Canvas", RouterPath.Compose.Canvas))

        routerItems.add(RouterItem("ConstraintLayout", RouterPath.Compose.ConstraintLayout))
        routerItems.add(RouterItem("HorizontalPager", RouterPath.Compose.HorizontalPager))

        routerItems.add(RouterItem("CompositionLocal", RouterPath.Compose.CompositionLocal))

        routerItems.add(RouterItem("CoordinatorLayout", RouterPath.Compose.CoordinatorLayout))

        routerItems.add(RouterItem("GuaguaCard", RouterPath.Compose.GuaguaCard))

        routerItems.add(RouterItem("BackHandler", RouterPath.Compose.BackHandler))

        routerItems.add(RouterItem("NavHost", RouterPath.Compose.NavHost))

        routerItems.add(RouterItem("NavigationBar (Material 3 底部导航栏)", RouterPath.Compose.NavigationBar))

        routerItems.add(RouterItem("Remember", RouterPath.Compose.Remember))

        routerItems.add(RouterItem("Draggable", RouterPath.Compose.Draggable))
        routerItems.add(RouterItem("DragGestures", RouterPath.Compose.DragGestures))

        routerItems.add(RouterItem("AnchoredDraggable", RouterPath.Compose.AnchoredDraggable))

        routerItems.add(RouterItem("SmartRefresh", RouterPath.Compose.SmartRefresh))

        routerItems.add(RouterItem("ScrollableTab", RouterPath.Compose.ScrollableTab))

        routerItems.add(RouterItem("LineChart（Compose 贝塞尔折线图）", RouterPath.Compose.LineChart))
        routerItems.add(RouterItem("BarChart（Compose 分组圆角柱状图）", RouterPath.Compose.BarChart))
        routerItems.add(RouterItem("PieChart（Compose 环形甜甜圈图）", RouterPath.Compose.PieChart))
        routerItems.add(RouterItem("RadarChart（Compose 六维雷达图）", RouterPath.Compose.RadarChart))
        routerItems.add(RouterItem("ChartLinkage（Compose 多图表全景看板）", RouterPath.Compose.ChartLinkage))
    }

    @Composable
    fun LazyColumnExample(itemsList: List<RouterItem>, modifier: Modifier = Modifier) {
        // 使用 rememberLazyListState 保存滚动的位置
        val scrollState = rememberLazyListState()

        LazyColumn(
            state = scrollState,
            modifier = modifier,
        ) {
            items(itemsList) { item ->
                LazyColumnItemExample(item) {
                    try {
                        ARouter.getInstance().build(item.mRouterPath).navigation()
                    } catch (e: HandlerException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    @Composable
    fun LazyColumnItemExample(routerItem: RouterItem? = null, onClick: () -> Unit) {
        // 参考 shared_item_recycler 布局：无背景无边框，固定高度 48dp，文字垂直居中
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = routerItem?.mRouterName ?: "",
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}
