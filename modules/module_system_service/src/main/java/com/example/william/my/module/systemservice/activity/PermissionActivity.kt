package com.example.william.my.module.systemservice.activity

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
 * 运行时权限（Jetpack 原生契约方案）
 *
 * 使用 Jetpack ActivityResultContracts.RequestMultiplePermissions 申请 App 基础运行时权限：
 * - 通知权限 (POST_NOTIFICATIONS，Android 13+)
 * - 多媒体存储权限 (READ_MEDIA_IMAGES / READ_MEDIA_VIDEO / READ_MEDIA_AUDIO，Android 13+；Android 12 及以下为 READ/WRITE_EXTERNAL_STORAGE)
 */
@Route(path = RouterPath.SystemService.Permission)
class PermissionActivity : BasicResponseActivity() {

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

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "申请通知权限 — POST_NOTIFICATIONS (Android 13+)",
            "申请多媒体存储权限 — READ_MEDIA_* (Android 13+) / READ_EXTERNAL_STORAGE",
            "申请全部常用基础权限（通知 + 多媒体存储）"
        )
    }

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

    private fun isPermissionAvailable(permission: String): Boolean {
        return when (permission) {
            Manifest.permission.POST_NOTIFICATIONS -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            else -> true
        }
    }

    private fun permissionLabel(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE -> "外部存储"
            Manifest.permission.READ_MEDIA_IMAGES -> "照片库"
            Manifest.permission.READ_MEDIA_VIDEO -> "视频库"
            Manifest.permission.READ_MEDIA_AUDIO -> "音频库"
            Manifest.permission.POST_NOTIFICATIONS -> "系统通知"
            else -> permission.substringAfterLast(".")
        }
    }

    private fun buildNotificationPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
    }

    private fun buildStoragePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
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
