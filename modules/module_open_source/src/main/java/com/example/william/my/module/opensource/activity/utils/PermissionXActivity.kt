package com.example.william.my.module.opensource.activity.utils

import android.Manifest
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.permissionx.guolindev.PermissionX

/**
 * PermissionX 权限申请
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