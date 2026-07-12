package com.example.william.my.module.system.activity

import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils

/**
 * 权限申请
 */
@Route(path = RouterPath.System.Permission)
class PermissionActivity : BasicResponseActivity() {

    private val permissions = arrayOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR, //日历

        Manifest.permission.CAMERA, //相机

        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.GET_ACCOUNTS, //联系人

        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION, //位置信息

        Manifest.permission.RECORD_AUDIO, //麦克风

        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG, //手机权限

        Manifest.permission.BODY_SENSORS, //传感器

        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS, //短信

        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE //存储
    )

    override fun onResponseClick(view: View) {
        super.onResponseClick(view)

        val mPermissionList = mutableListOf<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission)
            }
        }
        if (mPermissionList.isNotEmpty()) {
            val perms = mPermissionList.toTypedArray()
            ActivityCompat.requestPermissions(this, perms, 1000)
        }
    }

    /**
     * 用户选择允许或拒绝后调用此方法
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                Utils.toast("同意权限申请")
            }
        }
    }
}
