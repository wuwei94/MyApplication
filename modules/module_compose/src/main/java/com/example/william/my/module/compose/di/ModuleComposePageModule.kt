package com.example.william.my.module.compose.di

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.compose.ComposeMainActivity
import com.example.william.my.module.compose.activity.adaptive.AdaptiveActivity
import com.example.william.my.module.compose.activity.anchoreddraggable.AnchoredDraggableActivity
import com.example.william.my.module.compose.activity.androidview.AndroidViewActivity
import com.example.william.my.module.compose.activity.animation.AnimationActivity
import com.example.william.my.module.compose.activity.basic.ButtonActivity
import com.example.william.my.module.compose.activity.basic.CompositionLocalActivity
import com.example.william.my.module.compose.activity.basic.ConstraintLayoutActivity
import com.example.william.my.module.compose.activity.basic.EffectActivity
import com.example.william.my.module.compose.activity.basic.ImageActivity
import com.example.william.my.module.compose.activity.basic.TextActivity
import com.example.william.my.module.compose.activity.bottomsheet.BottomSheetActivity
import com.example.william.my.module.compose.activity.canvas.CanvasActivity
import com.example.william.my.module.compose.activity.chart.BarChartActivity
import com.example.william.my.module.compose.activity.chart.ChartLinkageActivity
import com.example.william.my.module.compose.activity.chart.LineChartActivity
import com.example.william.my.module.compose.activity.chart.PieChartActivity
import com.example.william.my.module.compose.activity.chart.RadarChartActivity
import com.example.william.my.module.compose.activity.chip.ChipActivity
import com.example.william.my.module.compose.activity.collapsingtopbar.CollapsingTopBarActivity
import com.example.william.my.module.compose.activity.composeview.ComposeViewActivity
import com.example.william.my.module.compose.activity.dialog.DialogActivity
import com.example.william.my.module.compose.activity.draggable.DragGesturesActivity
import com.example.william.my.module.compose.activity.dragging.GuaguaCardActivity
import com.example.william.my.module.compose.activity.insets.InsetsActivity
import com.example.william.my.module.compose.activity.lazylayout.LazyLayoutActivity
import com.example.william.my.module.compose.activity.menu.MenuActivity
import com.example.william.my.module.compose.activity.nav3.Nav3Activity
import com.example.william.my.module.compose.activity.navhost.NavHostActivity
import com.example.william.my.module.compose.activity.navigation.NavigationBarActivity
import com.example.william.my.module.compose.activity.performance.PerformanceActivity
import com.example.william.my.module.compose.activity.progress.ProgressActivity
import com.example.william.my.module.compose.activity.remember.RememberActivity
import com.example.william.my.module.compose.activity.selection.SelectionActivity
import com.example.william.my.module.compose.activity.sharedelement.SharedElementActivity
import com.example.william.my.module.compose.activity.smartrefresh.SmartRefreshActivity
import com.example.william.my.module.compose.activity.staggeredgrid.StaggeredGridActivity
import com.example.william.my.module.compose.activity.tab.ScrollableTabActivity
import com.example.william.my.module.compose.activity.textfield.TextFieldActivity
import com.example.william.my.module.compose.activity.theme.ThemeActivity
import com.example.william.my.module.compose.activity.transformgestures.TransformGesturesActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_compose 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleComposePageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Adaptive)
    fun provideAdaptiveActivity(): Class<out Activity> = AdaptiveActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.AnchoredDraggable)
    fun provideAnchoredDraggableActivity(): Class<out Activity> = AnchoredDraggableActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.AndroidView)
    fun provideAndroidViewActivity(): Class<out Activity> = AndroidViewActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Animation)
    fun provideAnimationActivity(): Class<out Activity> = AnimationActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Button)
    fun provideButtonActivity(): Class<out Activity> = ButtonActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.CompositionLocal)
    fun provideCompositionLocalActivity(): Class<out Activity> = CompositionLocalActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.ConstraintLayout)
    fun provideConstraintLayoutActivity(): Class<out Activity> = ConstraintLayoutActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Effect)
    fun provideEffectActivity(): Class<out Activity> = EffectActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Image)
    fun provideImageActivity(): Class<out Activity> = ImageActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Text)
    fun provideTextActivity(): Class<out Activity> = TextActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.BottomSheet)
    fun provideBottomSheetActivity(): Class<out Activity> = BottomSheetActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Canvas)
    fun provideCanvasActivity(): Class<out Activity> = CanvasActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.BarChart)
    fun provideBarChartActivity(): Class<out Activity> = BarChartActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.ChartLinkage)
    fun provideChartLinkageActivity(): Class<out Activity> = ChartLinkageActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.LineChart)
    fun provideLineChartActivity(): Class<out Activity> = LineChartActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.PieChart)
    fun providePieChartActivity(): Class<out Activity> = PieChartActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.RadarChart)
    fun provideRadarChartActivity(): Class<out Activity> = RadarChartActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Chip)
    fun provideChipActivity(): Class<out Activity> = ChipActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.CollapsingTopBar)
    fun provideCollapsingTopBarActivity(): Class<out Activity> = CollapsingTopBarActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.ComposeView)
    fun provideComposeViewActivity(): Class<out Activity> = ComposeViewActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Dialog)
    fun provideDialogActivity(): Class<out Activity> = DialogActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.DragGestures)
    fun provideDragGesturesActivity(): Class<out Activity> = DragGesturesActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.GuaguaCard)
    fun provideGuaguaCardActivity(): Class<out Activity> = GuaguaCardActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Insets)
    fun provideInsetsActivity(): Class<out Activity> = InsetsActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.LazyLayout)
    fun provideLazyLayoutActivity(): Class<out Activity> = LazyLayoutActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Menu)
    fun provideMenuActivity(): Class<out Activity> = MenuActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Nav3)
    fun provideNav3Activity(): Class<out Activity> = Nav3Activity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.NavHost)
    fun provideNavHostActivity(): Class<out Activity> = NavHostActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.NavigationBar)
    fun provideNavigationBarActivity(): Class<out Activity> = NavigationBarActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Performance)
    fun providePerformanceActivity(): Class<out Activity> = PerformanceActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Progress)
    fun provideProgressActivity(): Class<out Activity> = ProgressActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Remember)
    fun provideRememberActivity(): Class<out Activity> = RememberActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Selection)
    fun provideSelectionActivity(): Class<out Activity> = SelectionActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.SharedElement)
    fun provideSharedElementActivity(): Class<out Activity> = SharedElementActivity::class.java

    @OptIn(ExperimentalFoundationApi::class)
    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.SmartRefresh)
    fun provideSmartRefreshActivity(): Class<out Activity> = SmartRefreshActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.StaggeredGrid)
    fun provideStaggeredGridActivity(): Class<out Activity> = StaggeredGridActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.ScrollableTab)
    fun provideScrollableTabActivity(): Class<out Activity> = ScrollableTabActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.TextField)
    fun provideTextFieldActivity(): Class<out Activity> = TextFieldActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Theme)
    fun provideThemeActivity(): Class<out Activity> = ThemeActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.TransformGestures)
    fun provideTransformGesturesActivity(): Class<out Activity> = TransformGesturesActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Compose.Main)
    fun provideComposeMainActivity(): Class<out Activity> = ComposeMainActivity::class.java
}
