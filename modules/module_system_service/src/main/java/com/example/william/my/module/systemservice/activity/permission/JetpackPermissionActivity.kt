package com.example.william.my.module.systemservice.activity.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 运行时权限 — Jetpack ActivityResultContracts 原生契约方案
 *
 * 使用 Jetpack ActivityResultContracts.RequestMultiplePermissions 申请运行时权限，
 * 替代已废弃的 onRequestPermissionsResult 回调。覆盖通知权限（Android 13+）与
 * 多媒体存储权限（Android 13+ 为 READ_MEDIA_*，低版本为 READ/WRITE_EXTERNAL_STORAGE）。
 *
 * 核心机制与避坑点：
 * 1. 契约注册：registerForActivityResult + RequestMultiplePermissions 声明式注册
 * 2. 结果回调：grantedPermissions Map 区分已授权与被拒绝权限
 * 3. 永久拒绝检测：shouldShowRequestPermissionRationale 判断用户是否勾选「不再提示」
 * 4. 版本适配：按 Build.VERSION 动态选择 READ_MEDIA_* 或 READ_EXTERNAL_STORAGE
 *
 * https://developer.android.com/training/permissions/requesting
 */
@Route(path = RouterPath.SystemService.JetpackPermission)
class JetpackPermissionActivity : BasicResponseActivity() {

    private var currentActionName = ""

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantedPermissions ->
            val granted = grantedPermissions.filterValues { it }
            val denied = grantedPermissions.filterValues { !it }

            val result = if (denied.isEmpty()) {
                val names = granted.keys.joinToString("、") { permissionLabel(it) }
                "【$currentActionName】授权成功：$names"
            } else {
                val permanentlyDenied = denied.keys.filter { perm ->
                    !shouldShowRequestPermissionRationale(perm)
                }
                val deniedNames = denied.keys.joinToString("、") { permissionLabel(it) }
                if (permanentlyDenied.isNotEmpty()) {
                    "【$currentActionName】授权失败：$deniedNames（用户勾选了不再提示/永久拒绝）"
                } else {
                    "【$currentActionName】授权失败：$deniedNames（普通拒绝）"
                }
            }
            appendLog(result)
        }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("运行时权限申请（Jetpack ActivityResult 契约模式）\n\n演示使用官方标准 API 请求通知与多媒体存储权限")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 申请通知权限 — POST_NOTIFICATIONS (Android 13+)",
        "2. 申请多媒体存储权限 — READ_MEDIA_* (Android 13+) / READ_EXTERNAL_STORAGE",
        "3. 申请全部常用基础权限（通知 + 多媒体存储）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> requestPermissionsDirectly("通知权限", buildNotificationPermissions())
            1 -> requestPermissionsDirectly("多媒体存储权限", buildStoragePermissions())
            2 -> requestPermissionsDirectly("全部基础权限", buildAllPermissions())
        }
    }

    private fun requestPermissionsDirectly(actionName: String, permissions: Array<String>) {
        currentActionName = actionName
        val availablePerms = permissions.filter { isPermissionAvailable(it) }.toTypedArray()
        if (availablePerms.isEmpty()) {
            appendLog("【$currentActionName】当前系统版本无需动态申请该权限")
            return
        }

        val deniedPerms = availablePerms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPerms.isEmpty()) {
            appendLog("【$currentActionName】权限已全部授予，无需重复申请")
            return
        }

        appendLog("----------------------------------------")
        appendLog("【Jetpack】正在发起权限申请：${deniedPerms.joinToString(", ")}")
        requestPermissions.launch(deniedPerms.toTypedArray())
    }

    private fun isPermissionAvailable(permission: String): Boolean = when (permission) {
        Manifest.permission.POST_NOTIFICATIONS -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO,
        -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ->
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        else -> true
    }

    private fun permissionLabel(permission: String): String = when (permission) {
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE -> "外部存储"
        Manifest.permission.READ_MEDIA_IMAGES -> "照片库"
        Manifest.permission.READ_MEDIA_VIDEO -> "视频库"
        Manifest.permission.READ_MEDIA_AUDIO -> "音频库"
        Manifest.permission.POST_NOTIFICATIONS -> "系统通知"
        else -> permission.substringAfterLast(".")
    }

    private fun buildNotificationPermissions(): Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }

    private fun buildStoragePermissions(): Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }

    private fun buildAllPermissions(): Array<String> {
        val list = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        list.addAll(buildStoragePermissions())
        return list.toTypedArray()
    }
}
