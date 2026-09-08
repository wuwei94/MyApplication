package com.example.william.my.module.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.exception.HandlerException
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Compose模块入口页
 *
 * 展示 Jetpack Compose 基础组件、布局容器、手势交互、导航体系与 Canvas 数据可视化等示例列表。
 */
@Route(path = RouterPath.Compose.Main)
class ComposeMainActivity : ComponentActivity() {

    private val routerItems: ArrayList<RouterItem> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        buildRouterItems()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LazyColumnExample(routerItems, Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun buildRouterItems() {
        routerItems.add(RouterItem("── 基础 UI 组件与互操作 ──", ""))
        routerItems.add(RouterItem("ComposeView（View 宿主中嵌入 Compose：老工程/XML 渐进式接入）", RouterPath.Compose.ComposeViewActivity))
        routerItems.add(RouterItem("AndroidView（Compose 树中嵌入原生 View：老控件/地图/视频等互操作）", RouterPath.Compose.AndroidView))
        routerItems.add(RouterItem("Text（文本样式、段落排版与点击事件）", RouterPath.Compose.Text))
        routerItems.add(RouterItem("Button（交互按钮形态与点击防重）", RouterPath.Compose.Button))
        routerItems.add(RouterItem("Selection（复选框 Checkbox、开关 Switch 与分段 Slider）", RouterPath.Compose.Selection))
        routerItems.add(RouterItem("Chip（Material 3 四大标准标签芯片）", RouterPath.Compose.Chip))
        routerItems.add(RouterItem("Menu（下拉菜单 DropdownMenu 与 ExposedDropdownMenu 表单选择）", RouterPath.Compose.Menu))
        routerItems.add(RouterItem("Progress（线性与圆形环状进度指示器）", RouterPath.Compose.Progress))
        routerItems.add(RouterItem("Image（图片缩放裁剪、几何形状与着色滤镜）", RouterPath.Compose.Image))
        routerItems.add(RouterItem("TextField（输入框形态、软键盘行为与格式化）", RouterPath.Compose.TextField))
        routerItems.add(RouterItem("Theme（Material 3 动态取色方案与深浅色模式）", RouterPath.Compose.Theme))
        routerItems.add(RouterItem("Canvas（DrawScope 原子绘图原语：点/线/圆/弧/Path 路径基石）", RouterPath.Compose.Canvas))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 运行时机制与状态管理 ──", ""))
        routerItems.add(RouterItem("Remember（状态记忆与重组生命周期保持）", RouterPath.Compose.Remember))
        routerItems.add(RouterItem("Effect（副作用体系：Launched/Disposable/SideEffect 与 rememberUpdatedState）", RouterPath.Compose.Effect))
        routerItems.add(RouterItem("CompositionLocal（树作用域隐式数据穿透与 static 机制对比）", RouterPath.Compose.CompositionLocal))
        routerItems.add(RouterItem("Performance（稳定性契约、重组跳过与性能防抖）", RouterPath.Compose.Performance))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 布局排版与视口容器 ──", ""))
        routerItems.add(RouterItem("ConstraintLayout（约束布局与相对定位系统）", RouterPath.Compose.ConstraintLayout))
        routerItems.add(RouterItem("Insets（Edge-to-Edge 边到边沉浸与 IME 键盘避让）", RouterPath.Compose.Insets))
        routerItems.add(RouterItem("Adaptive（响应式断点与 List-Detail 双窗格跨端布局）", RouterPath.Compose.Adaptive))
        routerItems.add(RouterItem("CollapsingTopBar（Material 3 折叠吸顶标题栏）", RouterPath.Compose.CollapsingTopBar))
        routerItems.add(RouterItem("BottomSheet（Material 3 模态抽屉底栏）", RouterPath.Compose.BottomSheet))
        routerItems.add(RouterItem("Dialog（AlertDialog 确认弹窗与 Snackbar 提示条）", RouterPath.Compose.Dialog))
        routerItems.add(RouterItem("LazyLayout（吸顶悬浮标题与 animateItem 项重排动效）", RouterPath.Compose.LazyLayout))
        routerItems.add(RouterItem("StaggeredGrid（交错瀑布流与自适应列策略）", RouterPath.Compose.StaggeredGrid))
        routerItems.add(RouterItem("SmartRefresh（下拉刷新与上拉加载流式适配）", RouterPath.Compose.SmartRefresh))
        routerItems.add(RouterItem("ScrollableTab（可滚动标签与 HorizontalPager 分页联动）", RouterPath.Compose.ScrollableTab))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 动效转场与手势交互 ──", ""))
        routerItems.add(RouterItem("Animation（声明式动效体系：尺寸形变、淡入淡出与过渡）", RouterPath.Compose.Animation))
        routerItems.add(RouterItem("DragGestures（单轴约束与全向自由拖拽手势原语）", RouterPath.Compose.DragGestures))
        routerItems.add(RouterItem("TransformGestures（双指缩放、平移与多角度旋转原语）", RouterPath.Compose.TransformGestures))
        routerItems.add(RouterItem("AnchoredDraggable（手势锚点磁吸与侧滑操作项原语）", RouterPath.Compose.AnchoredDraggable))
        routerItems.add(RouterItem("GuaguaCard（手势擦除与 Canvas BlendMode 图层遮罩实战）", RouterPath.Compose.GuaguaCard))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 现代导航与架构体系 ──", ""))
        routerItems.add(RouterItem("NavHost（Navigation 2.x 时代：NavController 控制器与强类型安全路由）", RouterPath.Compose.NavHost))
        routerItems.add(RouterItem("Nav3（Navigation 3.0 时代：推翻黑盒控制器，纯声明式状态提升与 NavDisplay）", RouterPath.Compose.Nav3))
        routerItems.add(RouterItem("NavigationBar（Material 3 底部导航栏 UI：Tab 视觉组件与返回栈联动）", RouterPath.Compose.NavigationBar))
        routerItems.add(RouterItem("SharedElement（声明式共享元素跨页面连续转场动效）", RouterPath.Compose.SharedElement))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── Canvas 数据可视化图表 ──", ""))
        routerItems.add(RouterItem("LineChart（Compose 贝塞尔平滑折线图与动态绘制）", RouterPath.Compose.LineChart))
        routerItems.add(RouterItem("BarChart（Compose 分组圆角柱状图与坐标轴标尺）", RouterPath.Compose.BarChart))
        routerItems.add(RouterItem("PieChart（Compose 环形甜甜圈图与占比动效）", RouterPath.Compose.PieChart))
        routerItems.add(RouterItem("RadarChart（Compose 六维能力雷达图与多边形绘制）", RouterPath.Compose.RadarChart))
        routerItems.add(RouterItem("ChartLinkage（Compose 多图表全景联动看板实战）", RouterPath.Compose.ChartLinkage))
    }

    @Composable
    fun LazyColumnExample(itemsList: List<RouterItem>, modifier: Modifier = Modifier) {
        val scrollState = rememberLazyListState()

        LazyColumn(
            state = scrollState,
            modifier = modifier,
        ) {
            items(itemsList) { item ->
                LazyColumnItemExample(item) {
                    if (!item.mRouterPath.isNullOrEmpty()) {
                        try {
                            ARouter.getInstance().build(item.mRouterPath).navigation()
                        } catch (e: HandlerException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LazyColumnItemExample(routerItem: RouterItem? = null, onClick: () -> Unit) {
        val title = routerItem?.mRouterName.orEmpty()
        val path = routerItem?.mRouterPath.orEmpty()

        if (path.isEmpty()) {
            if (title.isNotEmpty()) {
                // 分组标题栏
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                // 分组间隙
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            // 可点击的常规路由项：固定高度 48dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
