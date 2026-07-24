package com.example.william.my.core.server.nano

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.server.utils.NetworkUtils

/**
 * https://github.com/NanoHttpd/nanohttpd
 */
@Route(path = RouterPath.Server.Nano)
class NanoActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        showIPAddress()
    }

    private fun showIPAddress() {
        showResponse("http://" + NetworkUtils.getIPAddress(true) + ":" + NanoServer.DEFAULT_SERVER_PORT)
    }

    override fun onStart() {
        super.onStart()
        NanoService.startService(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        NanoService.stopService(this)
    }
}
