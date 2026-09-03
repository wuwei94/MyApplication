package com.example.william.my.core.base.ui.activity

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.alibaba.android.arouter.launcher.ARouter
import com.example.william.my.core.base.utils.DensityAdaptUtils

/**
 * 基础 Activity 基类
 *
 * 规范生命周期流转，集成 ARouter 依赖注入、EventBus 声明、
 * 全屏 Edge-to-Edge 沉浸式与现代 WindowInsets 安全区域自动避让体系。
 */
open class BaseActivity : AppCompatActivity() {

    // ==============================================================================
    // 1. 属性与控制器
    // ==============================================================================

    protected val TAG: String = this.javaClass.simpleName

    /**
     * 窗口 Insets 控制器（用于显示/隐藏系统栏、反色图标、控制软键盘等）
     */
    protected val insetsController: WindowInsetsControllerCompat by lazy {
        WindowCompat.getInsetsController(window, window.decorView)
    }

    // ==============================================================================
    // 2. Activity 生命周期
    // ==============================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        initEdgeToEdge()
        super.onCreate(savedInstanceState)
        // setTheme(R.style.base_WindowAnimTheme_Slide)

        initARouter()
        initEventBus()

        initViewBinding()
        initView(savedInstanceState)

        initViewModel()
        observeViewModel()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        initARouter()
        setIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        // EventBusHelper.register(this)
    }

    override fun onStop() {
        super.onStop()
        // EventBusHelper.unregister(this)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    // ==============================================================================
    // 3. 布局设置与 Insets 避让
    // ==============================================================================

    override fun setContentView(view: View?) {
        // 启用 Window 级别的内容转场（Content Transitions，如 Explode/Slide/Fade）与共享元素转场
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        super.setContentView(view)
        applyEdgeToEdgeInsets(view)
    }

    override fun setContentView(layoutResID: Int) {
        // 启用 Window 级别的内容转场（Content Transitions，如 Explode/Slide/Fade）与共享元素转场
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        super.setContentView(layoutResID)
        applyEdgeToEdgeInsets(findViewById(android.R.id.content))
    }

    /**
     * 适配 Edge-to-Edge 全屏沉浸式下的系统栏内边距
     *
     * 当启用 [enableEdgeToEdge] 后，系统 Window 会铺满整个屏幕（延伸到状态栏和导航栏下方）。
     * 若 [fitsSystemWindows] 返回 true（默认），通过 [ViewCompat.setOnApplyWindowInsetsListener] 动态获取
     * 状态栏、导航栏与输入法软键盘（IME）高度，并为根视图设置对应的安全 padding，避免内容被系统栏遮挡；
     * 若子类重写 [fitsSystemWindows] 返回 false（如相机全屏预览、视频播放等），则不加 padding 实现全屏贯穿。
     *
     * 细粒度控制：子类还可进一步重写 [fitsStatusBar]、[fitsNavigationBar]、[fitsIme] 进行独立开关。
     *
     * @param view 需要应用系统栏安全内边距的根视图
     */
    protected open fun applyEdgeToEdgeInsets(view: View?) {
        if (view != null && fitsSystemWindows()) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

                val top = if (fitsStatusBar()) systemBars.top else 0
                val bottom = if (fitsIme() && ime.bottom > 0) {
                    ime.bottom
                } else if (fitsNavigationBar()) {
                    systemBars.bottom
                } else {
                    0
                }
                val left = systemBars.left
                val right = systemBars.right

                v.setPadding(left, top, right, bottom)

                onImeInsetsChanged(ime.bottom)

                insets
            }
        }
        // 确保状态栏与底部导航栏图标/文字颜色生效（暗色/亮色字体）
        insetsController.isAppearanceLightStatusBars = statusBarDarkFont()
        insetsController.isAppearanceLightNavigationBars = navigationBarDarkIcon()
    }

    // ==============================================================================
    // 4. 业务初始化模板方法（按 onCreate 实际调用先后顺序排列）
    // ==============================================================================

    private fun initEdgeToEdge() {
        if (enableEdgeToEdge()) {
            enableEdgeToEdge(statusBarStyle = getStatusBarStyle())
        }
    }

    private fun initARouter() {
        ARouter.getInstance().inject(this)
    }

    private fun initEventBus() {
        // 子类或后续扩展全局 EventBus 初始化
    }

    open fun initViewBinding() {
        // BaseVBActivity 重写实现 ViewBinding 绑定
    }

    open fun initView(savedInstanceState: Bundle?) {
        // 子类重写实现视图控件事件与状态初始化
    }

    open fun initViewModel() {
        // 子类重写实现 ViewModel 获取
    }

    open fun observeViewModel() {
        // 子类重写实现 ViewModel 数据监听
    }

    // ==============================================================================
    // 5. Edge-to-Edge & WindowInsets 行为配置挂钩（子类按需重写）
    // ==============================================================================

    /**
     * 是否启用 Edge-to-Edge 全屏沉浸式
     *
     * @return true 表示在 onCreate 自动调用 [androidx.activity.enableEdgeToEdge]，透明化状态栏与导航栏并延伸布局到系统栏下方；false 则保持系统默认窗口行为
     */
    protected open fun enableEdgeToEdge(): Boolean = true

    /**
     * 是否自动为根视图应用系统栏安全内边距（防状态栏/导航栏遮挡内容）
     *
     * @return true（默认）表示由 [applyEdgeToEdgeInsets] 自动根据 SystemBars 动态设置 padding 留出安全区域；
     *         false 表示不添加 padding，内容直接贯穿到状态栏/导航栏下方（如相机全屏预览、视频播放等场景）
     */
    protected open fun fitsSystemWindows(): Boolean = true

    /**
     * 是否避让顶部状态栏
     * 仅在 [fitsSystemWindows] 为 true 时生效，默认 true
     */
    protected open fun fitsStatusBar(): Boolean = true

    /**
     * 是否避让底部导航栏（小白条）
     * 仅在 [fitsSystemWindows] 为 true 时生效，默认 true
     */
    protected open fun fitsNavigationBar(): Boolean = true

    /**
     * 是否自动避让输入法软键盘（IME）高度
     * 仅在 [fitsSystemWindows] 为 true 时生效，默认 true
     */
    protected open fun fitsIme(): Boolean = true

    /**
     * 获取 Edge-to-Edge 状态栏样式
     *
     * 默认根据 [statusBarDarkFont] 返回浅色模式（黑色文字）或深色模式（白色文字）样式。
     */
    protected open fun getStatusBarStyle(): SystemBarStyle = if (statusBarDarkFont()) {
        SystemBarStyle.light(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT,
        )
    } else {
        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
    }

    /**
     * 状态栏图标与文字颜色是否为深色（黑色）
     *
     * @return true 表示状态栏为深色字体（适合白色/浅色背景页面），false 表示白色字体（适合深色/全屏媒体页面）
     */
    protected open fun statusBarDarkFont(): Boolean = true

    /**
     * 底部导航栏（小白条）图标与文字颜色是否为深色（黑色）
     * 默认跟随 [statusBarDarkFont] 保持一致
     */
    protected open fun navigationBarDarkIcon(): Boolean = statusBarDarkFont()

    /**
     * 软键盘高度动态变化回调（单位：px）
     * 软键盘弹出时 imeHeight > 0，收起时 imeHeight == 0
     *
     * @param imeHeight 软键盘在屏幕底部的当前高度
     */
    protected open fun onImeInsetsChanged(imeHeight: Int) {}

    // ==============================================================================
    // 6. WindowInsetsController 常用操作便捷方法（供子类按需直接调用）
    // ==============================================================================

    /**
     * 动态设置状态栏文字与图标颜色
     *
     * @param isDark true 为深色（黑色，适合浅色背景），false 为浅色（白色，适合深色背景）
     */
    fun setStatusBarDarkFont(isDark: Boolean) {
        insetsController.isAppearanceLightStatusBars = isDark
    }

    /**
     * 动态设置底部导航栏（小白条）图标颜色
     *
     * @param isDark true 为深色，false 为浅色
     */
    fun setNavigationBarDarkIcon(isDark: Boolean) {
        insetsController.isAppearanceLightNavigationBars = isDark
    }

    /**
     * 显示状态栏
     */
    fun showStatusBar() {
        insetsController.show(WindowInsetsCompat.Type.statusBars())
    }

    /**
     * 隐藏状态栏
     */
    fun hideStatusBar() {
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
    }

    /**
     * 显示底部导航栏
     */
    fun showNavigationBar() {
        insetsController.show(WindowInsetsCompat.Type.navigationBars())
    }

    /**
     * 隐藏底部导航栏
     */
    fun hideNavigationBar() {
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
    }

    /**
     * 同时隐藏状态栏与导航栏（全屏沉浸模式，如视频播放、画廊、游戏）
     */
    fun hideSystemBars() {
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    /**
     * 同时显示状态栏与导航栏
     */
    fun showSystemBars() {
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    }

    /**
     * 设置全屏沉浸式交互行为（如边缘手势滑动临时调出系统栏）
     *
     * @param behavior 取值为 [WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE]、
     *                 [WindowInsetsControllerCompat.BEHAVIOR_DEFAULT] 等
     */
    fun setSystemBarsBehavior(behavior: Int) {
        insetsController.systemBarsBehavior = behavior
    }

    /**
     * 主动弹出软键盘
     *
     * @param targetView 承载焦点的输入控件
     */
    fun showIme(targetView: View) {
        targetView.requestFocus()
        insetsController.show(WindowInsetsCompat.Type.ime())
    }

    /**
     * 主动收起软键盘
     */
    fun hideIme() {
        insetsController.hide(WindowInsetsCompat.Type.ime())
    }

    // ==============================================================================
    // 7. 屏幕与系统底层适配
    // ==============================================================================

    override fun getResources(): Resources = DensityAdaptUtils.adaptWidth(super.getResources(), 360f)
}
