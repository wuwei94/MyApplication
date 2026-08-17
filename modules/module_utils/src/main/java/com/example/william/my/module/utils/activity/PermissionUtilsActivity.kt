package com.example.william.my.module.utils.activity

import android.os.Build
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 权限请求工具类演示
 *
 * 演示 BlankJ PermissionUtils 单项权限、权限组与系统特殊权限申请。
 *
 * 注意：
 * 当 targetSdkVersion >= 30 时，如果要申请 ACCESS_BACKGROUND_LOCATION 权限，
 * 需要先申请 ACCESS_FINE_LOCATION 或 ACCESS_COARSE_LOCATION 权限，避免同时申请导致弹窗失效。
 */
@Route(path = RouterPath.Utils.PermissionUtils)
class PermissionUtilsActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 BlankJ PermissionUtils 权限申请与管理")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "requestCalendar (申请日历权限)",
            "requestCamera (申请相机权限)",
            "requestStorage (申请存储权限)",
            "requestPermissionGroup (申请多项组合权限)",
            "requestWriteSettings (申请修改系统设置)",
            "requestDrawOverlays (申请悬浮窗权限)",
            "launchAppDetailsSettings (跳转应用详情设置)",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> requestCalendar()
            1 -> requestCamera()
            2 -> requestStorage()
            3 -> requestPermissionGroup()
            4 -> requestWriteSettings()
            5 -> requestDrawOverlays()
            6 -> launchAppDetailsSettings()
        }
    }

    /**
     * 申请日历权限
     */
    private fun requestCalendar() {
        requestSinglePermission(PermissionConstants.CALENDAR, "Calendar")
    }

    /**
     * 申请相机权限
     */
    private fun requestCamera() {
        requestSinglePermission(PermissionConstants.CAMERA, "Camera")
    }

    /**
     * 申请存储权限
     */
    private fun requestStorage() {
        requestSinglePermission(PermissionConstants.STORAGE, "Storage")
    }

    private fun requestSinglePermission(
        @PermissionConstants.PermissionGroup permission: String,
        name: String
    ) {
        PermissionUtils.permission(permission)
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(granted: MutableList<String>) {
                    appendLog("$name 权限已授予: $granted")
                }

                override fun onDenied(
                    deniedForever: MutableList<String>,
                    denied: MutableList<String>
                ) {
                    if (deniedForever.isNotEmpty()) {
                        appendLog("$name 权限被永久拒绝: $deniedForever")
                    } else {
                        appendLog("$name 权限被拒绝: $denied")
                    }
                }
            })
            .request()
    }

    /**
     * 申请组合权限
     */
    private fun requestPermissionGroup() {
        PermissionUtils.permissionGroup(
            PermissionConstants.CALENDAR,
            PermissionConstants.CAMERA,
            PermissionConstants.LOCATION
        )
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(granted: MutableList<String>) {
                    appendLog("组合权限已授予: $granted")
                }

                override fun onDenied(
                    deniedForever: MutableList<String>,
                    denied: MutableList<String>
                ) {
                    if (deniedForever.isNotEmpty()) {
                        appendLog("组合权限被永久拒绝: $deniedForever")
                    } else {
                        appendLog("组合权限被拒绝: $denied")
                    }
                }
            })
            .request()
    }

    /**
     * 申请修改系统权限
     */
    private fun requestWriteSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionUtils.requestWriteSettings(object : PermissionUtils.SimpleCallback {
                override fun onGranted() {
                    appendLog("Write Settings 权限已授予")
                }

                override fun onDenied() {
                    appendLog("Write Settings 权限被拒绝")
                }
            })
        }
    }

    /**
     * 申请悬浮窗权限
     */
    private fun requestDrawOverlays() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionUtils.requestDrawOverlays(object : PermissionUtils.SimpleCallback {
                override fun onGranted() {
                    appendLog("Draw Overlays 权限已授予")
                }

                override fun onDenied() {
                    appendLog("Draw Overlays 权限被拒绝")
                }
            })
        }
    }

    /**
     * 启动应用程序详细信息设置
     */
    private fun launchAppDetailsSettings() {
        PermissionUtils.launchAppDetailsSettings()
        appendLog("已跳转至应用详情设置页面")
    }
}