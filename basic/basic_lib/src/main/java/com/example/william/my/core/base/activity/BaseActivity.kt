package com.example.william.my.core.base.activity

import android.Manifest
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.LogUtils
import com.example.william.my.core.base.utils.DensityAdaptUtils
import com.gyf.immersionbar.ImmersionBar
import com.permissionx.guolindev.PermissionX

open class BaseActivity : AppCompatActivity() {

    protected val TAG = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setTheme(R.style.base_WindowAnimTheme_Slide)

        initARouter()
        initEventBus()
        initStatusBar()

        initViewBinding()
        initView(savedInstanceState)

        initViewModel()
        observeViewModel()
    }

    override fun setContentView(view: View?) {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        super.setContentView(view)
    }

    override fun setContentView(layoutResID: Int) {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        super.setContentView(layoutResID)
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

    private fun initStatusBar() {
        if (enableTransparentStatusBar()) {
            transparentStatusBar()
        }
    }

    protected open fun initPermission() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.POST_NOTIFICATIONS,
            )
            //.explainReasonBeforeRequest()
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "PermissionX需要您同意以下权限才能正常使用",
                    "Allow",
                    "Deny"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "您需要手动在“设置”中允许必要的权限",
                    "OK",
                    "Cancel"
                )
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    LogUtils.e("全部权限已授予: $grantedList")
                } else {
                    LogUtils.e("权限被拒绝: $deniedList")
                }
            }
    }

    protected open fun transparentStatusBar() {
        ImmersionBar.with(this)
            .transparentStatusBar()  //透明状态栏，不写默认透明色
            .fitsSystemWindows(fitsSystemWindows()) //解决状态栏和布局重叠问题
            .statusBarDarkFont(statusBarDarkFont()) //状态栏字体是深色，不写默认为亮色
            .keyboardEnable(keyboardEnable()) // 解决软键盘与底部输入框冲突问题，默认为false
            .init()
    }

    protected open fun enableTransparentStatusBar(): Boolean {
        return true
    }

    protected open fun fitsSystemWindows(): Boolean {
        return true
    }

    protected open fun statusBarDarkFont(): Boolean {
        return true
    }

    protected open fun keyboardEnable(): Boolean {
        return false
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