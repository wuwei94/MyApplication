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
 * 运行时权限 — 按权限分组请求与结果展示
 *
 * 使用 Jetpack ActivityResultContracts.RequestMultiplePermissions 替代已废弃的
 * ActivityCompat.requestPermissions + onRequestPermissionsResult 模式。
 *
 * 核心原理：
 * 1. 按场景将权限分为 9 组（通知、相机、存储、位置、麦克风、联系人、电话、日历、短信）
 * 2. 点击列表项时，先过滤当前设备 API 不支持的权限（如 POST_NOTIFICATIONS 需 API 33+）
 * 3. 再过滤已授权的权限，仅请求尚未授予的权限
 * 4. 通过 registerForActivityResult() 注册回调，自动在 Activity 销毁时解注册
 * 5. 区分"普通拒绝"和"永久拒绝"（shouldShowRequestPermissionRationale 返回 false）
 *
 * 适用场景：
 * - 运行时权限申请的标准实现参考
 * - 按权限分组批量请求
 * - 处理权限永久拒绝后引导用户前往设置页
 */
@Route(path = RouterPath.SystemService.Permission)
class PermissionActivity : BasicResponseActivity() {

    /**
     * 权限分组：label → 权限列表。
     * 按场景组织，避免一次性请求全部权限。
     */
    private val permissionGroups = linkedMapOf(
        "通知" to buildNotificationPermission(),
        "相机" to arrayOf(
            Manifest.permission.CAMERA
        ),
        "存储" to arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ),
        "位置" to arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ),
        "麦克风" to arrayOf(
            Manifest.permission.RECORD_AUDIO
        ),
        "联系人" to arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.GET_ACCOUNTS
        ),
        "电话" to buildPhonePermissions(),
        "日历" to arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        ),
        "短信" to arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
    )

    private var currentGroupName = ""

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantedPermissions ->
            val granted = grantedPermissions.filterValues { it }
            val denied = grantedPermissions.filterValues { !it }

            val result = if (denied.isEmpty()) {
                val names = granted.keys.joinToString("、") { permissionLabel(it) }
                "【$currentGroupName】已授权：$names"
            } else {
                val permanentlyDenied = denied.keys.filter { perm ->
                    !shouldShowRequestPermissionRationale(perm)
                }
                val deniedNames = denied.keys.joinToString("、") { permissionLabel(it) }
                if (permanentlyDenied.isNotEmpty()) {
                    "【$currentGroupName】已拒绝：$deniedNames（永久拒绝）"
                } else {
                    "【$currentGroupName】已拒绝：$deniedNames"
                }
            }
            appendLog(result)
        }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("运行时权限申请\n\n点击下方列表请求各组权限")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "通知 — POST_NOTIFICATIONS（Android 13+）",
            "相机 — CAMERA",
            "存储 — READ/WRITE_EXTERNAL_STORAGE",
            "位置 — FINE/COARSE_LOCATION",
            "麦克风 — RECORD_AUDIO",
            "联系人 — READ/WRITE_CONTACTS, GET_ACCOUNTS",
            "电话 — READ_PHONE_STATE, CALL_PHONE 等",
            "日历 — READ/WRITE_CALENDAR",
            "短信 — SEND/RECEIVE/READ_SMS"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        val groupName = permissionGroups.keys.elementAt(position)
        val permissions = permissionGroups.values.elementAt(position)
        currentGroupName = groupName
        requestGroupedPermissions(permissions)
    }

    private fun requestGroupedPermissions(permissions: Array<String>) {
        val availablePerms = permissions.filter { isPermissionAvailable(it) }.toTypedArray()
        val deniedPerms = availablePerms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPerms.isEmpty()) {
            appendLog("【$currentGroupName】权限已全部授权")
            return
        }

        requestPermissions.launch(deniedPerms.toTypedArray())
    }

    private fun isPermissionAvailable(permission: String): Boolean {
        return when (permission) {
            Manifest.permission.READ_PHONE_NUMBERS -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            Manifest.permission.ACCEPT_HANDOVER -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            Manifest.permission.ANSWER_PHONE_CALLS -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            Manifest.permission.POST_NOTIFICATIONS -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            else -> true
        }
    }

    private fun permissionLabel(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR -> "日历"
            Manifest.permission.CAMERA -> "相机"
            Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.GET_ACCOUNTS -> "联系人"
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION -> "位置"
            Manifest.permission.RECORD_AUDIO -> "麦克风"
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.CALL_PHONE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG -> "电话"
            Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS -> "短信"
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE -> "存储"
            Manifest.permission.POST_NOTIFICATIONS -> "通知"
            else -> permission.substringAfterLast(".")
        }
    }

    private fun buildPhonePermissions(): Array<String> {
        val perms = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            perms.add(Manifest.permission.READ_PHONE_NUMBERS)
        }
        return perms.toTypedArray()
    }

    private fun buildNotificationPermission(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
    }
}
