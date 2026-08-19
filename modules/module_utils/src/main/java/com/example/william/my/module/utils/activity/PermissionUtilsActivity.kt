package com.example.william.my.module.utils.activity

import android.os.Build
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * PermissionUtils — 权限请求工具
 *
 * BlankJ PermissionUtils 提供便捷的权限申请功能。
 *
 * 核心特性：
 * 1. 单项权限：支持单个权限申请
 * 2. 权限组：支持多个权限组合申请
 * 3. 系统权限：支持修改系统设置、悬浮窗等特殊权限
 * 4. 回调机制：提供授权、拒绝、永久拒绝回调
 *
 * 基本用法：
 * ```kotlin
 * // 申请单个权限
 * PermissionUtils.permission(PermissionConstants.CAMERA)
 *     .callback(object : PermissionUtils.FullCallback {
 *         override fun onGranted(granted: MutableList<String>) {
 *             // 权限已授予
 *         }
 *         override fun onDenied(deniedForever: MutableList<String>, denied: MutableList<String>) {
 *             // 权限被拒绝
 *         }
 *     })
 *     .request()
 *
 * // 申请多个权限
 * PermissionUtils.permissionGroup(
 *     PermissionConstants.CAMERA,
 *     PermissionConstants.STORAGE
 * )
 *     .callback(callback)
 *     .request()
 * ```
 *
 * 注意事项：
 * - 当 targetSdkVersion >= 30 时，如果要申请 ACCESS_BACKGROUND_LOCATION 权限，
 *   需要先申请 ACCESS_FINE_LOCATION 或 ACCESS_COARSE_LOCATION 权限，避免同时申请导致弹窗失效。
 *
 * 适用场景：
 * - 运行时权限申请
 * - 批量权限申请
 * - 系统特殊权限申请
 *
 * https://github.com/Blankj/AndroidUtilCode
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