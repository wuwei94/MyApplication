package com.example.william.my.module.opensource.activity.utils

import android.Manifest
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.permissionx.guolindev.PermissionX

/**
 * PermissionX — 简化运行时权限申请
 *
 * PermissionX 是一个轻量级的权限申请库，简化 Android 运行时权限申请流程。
 *
 * 核心特性：
 * 1. 简单易用：链式调用，一行代码申请权限
 * 2. 自动处理：自动处理权限请求结果，无需手动判断
 * 3. 灵活配置：支持自定义权限说明对话框、引导对话框
 * 4. 兼容性好：支持 Android 6.0+ 运行时权限
 *
 * 基本用法：
 * ```kotlin
 * PermissionX.init(this)
 *     .permissions(Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS)
 *     .onExplainRequestReason { scope, deniedList ->
 *         scope.showRequestReasonDialog(deniedList, "需要这些权限", "允许", "拒绝")
 *     }
 *     .request { allGranted, grantedList, deniedList ->
 *         if (allGranted) {
 *             // 所有权限已授予
 *         }
 *     }
 * ```
 *
 * 适用场景：
 * - 运行时权限申请
 * - 需要自定义权限说明的场景
 * - 简化权限申请代码
 *
 * https://github.com/guolindev/PermissionX
 */
@Route(path = RouterPath.OpenSource.PermissionX)
class PermissionXActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项使用 PermissionX 申请权限")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("申请相机和通知权限")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        if (position == 0) {
            requestPermission()
        }
    }

    private fun requestPermission() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS, // 消息通知
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
                    appendLog("全部权限已授予: $grantedList")
                } else {
                    appendLog("权限被拒绝: $deniedList")
                }
            }
    }
}