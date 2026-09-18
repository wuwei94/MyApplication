package com.example.william.my.module.systemservice.permission.activity

import android.Manifest
import android.os.Build
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.permissionx.guolindev.PermissionX

/**
 * 运行时权限 — PermissionX 链式开源库方案
 *
 * PermissionX 是郭霖开源的运行时权限请求库，在 Jetpack ActivityResult 之上
 * 封装了链式 API、理由解释弹窗与永久拒绝后的设置页引导，大幅简化权限申请流程。
 * 与原生契约方案对比：PermissionX 额外提供 onExplainRequestReason 与
 * onForwardToSettings 两个弹窗能力，原生方案需自行实现这些 UI。
 *
 * 核心机制与避坑点：
 * 1. 链式调用：init → permissions → request 三步完成权限申请
 * 2. 理由弹窗：onExplainRequestReason 在拒绝后弹窗解释申请原因
 * 3. 设置引导：onForwardToSettings 永久拒绝后引导用户跳转系统设置
 * 4. 前置解释：explainReasonBeforeRequest 在首次申请前即展示理由弹窗
 *
 * https://github.com/guolindev/PermissionX
 */
@Route(path = RouterPath.SystemService.PermissionX)
class PermissionXActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("运行时权限申请（PermissionX 链式开源库模式）\n\n演示链式调用、前置解释弹窗与设置页引导")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "申请通知权限 — POST_NOTIFICATIONS (Android 13+)",
        "申请多媒体存储权限（带理由解释弹窗）",
        "申请多媒体存储权限（前置解释 + 永久拒绝设置引导）",
        "申请全部常用基础权限（通知 + 多媒体存储 全流程托管）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> requestNotificationPermission()
            1 -> requestStorageWithExplainReason()
            2 -> requestStorageWithForwardToSettings()
            3 -> requestAllBasePermissions()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            appendLog("----------------------------------------")
            appendLog("【PermissionX】当前系统版本（< Android 13）默认已开启通知权限，无需动态申请")
            return
        }

        appendLog("----------------------------------------")
        appendLog("【PermissionX】正在发起通知权限申请：POST_NOTIFICATIONS")

        PermissionX.init(this)
            .permissions(Manifest.permission.POST_NOTIFICATIONS)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    appendLogAccent("✓ 通知权限已授予：$grantedList")
                } else {
                    appendLog("✗ 通知权限被拒绝：$deniedList")
                }
            }
    }

    private fun requestStorageWithExplainReason() {
        val permissions = getStoragePermissions()

        appendLog("----------------------------------------")
        appendLog("【PermissionX】正在发起多媒体存储权限申请（配置理由解释弹窗）")

        PermissionX.init(this)
            .permissions(permissions)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "应用需要访问您的照片、视频与音频媒体库以读取和缓存资源，请在接下来的对话框中允许。",
                    "允许",
                    "取消",
                )
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    appendLogAccent("✓ 存储权限已全部授予：$grantedList")
                } else {
                    appendLog("✗ 存储权限被拒绝：$deniedList")
                }
            }
    }

    private fun requestStorageWithForwardToSettings() {
        val permissions = getStoragePermissions()

        appendLog("----------------------------------------")
        appendLog("【PermissionX】正在发起多媒体存储权限申请（前置理由解释 + 永久拒绝设置页引导）")

        PermissionX.init(this)
            .permissions(permissions)
            .explainReasonBeforeRequest()
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "为了浏览和加载相册与媒体资源，需要您授予多媒体存储访问权限。",
                    "确定",
                    "拒绝",
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "您已勾选了不再提示或永久拒绝了媒体权限，请前往系统“设置”中手动允许以恢复功能。",
                    "前往设置",
                    "取消",
                )
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    appendLogAccent("✓ 存储权限已全部授予：$grantedList")
                } else {
                    appendLog("✗ 存储权限被拒绝：$deniedList")
                }
            }
    }

    private fun requestAllBasePermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions.addAll(getStoragePermissions())

        appendLog("----------------------------------------")
        appendLog("【PermissionX】正在发起通用基础权限申请（全流程托管）")

        PermissionX.init(this)
            .permissions(permissions)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "为了保证基础功能（通知提醒与多媒体资源加载）正常工作，需要获取以下必要权限。",
                    "允许",
                    "取消",
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "部分必要基础权限被永久拒绝，请在系统设置中手动开启。",
                    "去设置",
                    "取消",
                )
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    appendLogAccent("✓ 所有基础常用权限已全部授予！")
                } else {
                    appendLog("✗ 仍有基础权限未授予：$deniedList")
                }
            }
    }

    private fun getStoragePermissions(): List<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
        )
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }
}
