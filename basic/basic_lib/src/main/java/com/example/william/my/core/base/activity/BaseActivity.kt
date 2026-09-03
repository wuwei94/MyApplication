package com.example.william.my.core.base.activity

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
import com.alibaba.android.arouter.launcher.ARouter
import com.example.william.my.core.base.utils.DensityAdaptUtils

open class BaseActivity : AppCompatActivity() {

    protected val TAG = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        initEdgeToEdge()
        super.onCreate(savedInstanceState)
        //setTheme(R.style.base_WindowAnimTheme_Slide)

        initARouter()
        initEventBus()

        initViewBinding()
        initView(savedInstanceState)

        initViewModel()
        observeViewModel()
    }

    private fun initEdgeToEdge() {
        if (enableEdgeToEdge()) {
            enableEdgeToEdge(statusBarStyle = getStatusBarStyle())
        }
    }

    override fun setContentView(view: View?) {
        // 启用 Window 级别的内容转场（Content Transitions，如 Explode/Slide/Fade）
        // 与共享元素转场（Activity Transitions）
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        super.setContentView(view)
        applyEdgeToEdgeInsets(view)
    }

    override fun setContentView(layoutResID: Int) {
        // 启用 Window 级别的内容转场（Content Transitions，如 Explode/Slide/Fade）
        // 与共享元素转场（Activity Transitions）
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
     * 状态栏和导航栏高度，并为根视图设置对应的 padding，避免页面顶部操作栏和底部按钮被系统栏遮挡；
     * 若子类重写 [fitsSystemWindows] 返回 false（如相机全屏预览、视频播放等），则不加 padding 实现全屏贯穿。
     *
     * @param view 需要应用系统栏安全内边距的根视图
     */
    protected open fun applyEdgeToEdgeInsets(view: View?) {
        if (view != null && fitsSystemWindows()) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                // 获取状态栏、导航栏与输入法软键盘（SystemBars + IME）的安全边距
                val insetsType = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
                val barsAndIme = insets.getInsets(insetsType)
                v.setPadding(barsAndIme.left, barsAndIme.top, barsAndIme.right, barsAndIme.bottom)
                insets
            }
        }
        // 确保状态栏图标与文字颜色生效（暗色/亮色字体）
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            statusBarDarkFont()
    }

    open fun initViewBinding() {

    }


    open fun initView(savedInstanceState: Bundle?) {

    }

    open fun initViewModel() {

    }

    open fun observeViewModel() {

    }

    override fun onStart() {
        super.onStart()
        //EventBusHelper.register(this)
    }

    override fun onStop() {
        super.onStop()
        //EventBusHelper.unregister(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        initARouter()
        setIntent(intent)
    }

    private fun initARouter() {
        ARouter.getInstance().inject(this)
    }

    private fun initEventBus() {

    }

    /**
     * 是否启用 Edge-to-Edge 全屏沉浸式
     *
     * @return true 表示在 onCreate 自动调用 [androidx.activity.enableEdgeToEdge]，透明化状态栏与导航栏并延伸布局到系统栏下方；false 则保持系统默认窗口行为
     */
    protected open fun enableEdgeToEdge(): Boolean {
        return true
    }

    /**
     * 是否自动为根视图应用系统栏安全内边距（防状态栏/导航栏遮挡内容）
     *
     * @return true（默认）表示由 [applyEdgeToEdgeInsets] 自动根据 SystemBars 动态设置 padding 留出安全区域；
     *         false 表示不添加 padding，内容直接贯穿到状态栏/导航栏下方（如相机全屏预览、视频播放等场景）
     */
    protected open fun fitsSystemWindows(): Boolean {
        return true
    }

    /**
     * 获取 Edge-to-Edge 状态栏样式
     *
     * 默认根据 [statusBarDarkFont] 返回浅色模式（黑色文字）或深色模式（白色文字）样式。
     */
    protected open fun getStatusBarStyle(): SystemBarStyle {
        return if (statusBarDarkFont()) {
            SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        } else {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        }
    }

    /**
     * 状态栏图标与文字颜色是否为深色（黑色）
     *
     * @return true 表示状态栏为深色字体（适合白色/浅色背景页面），false 表示白色字体（适合深色/全屏媒体页面）
     */
    protected open fun statusBarDarkFont(): Boolean {
        return true
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun getResources(): Resources {
        return DensityAdaptUtils.adaptWidth(super.getResources(), 360f)
    }
}