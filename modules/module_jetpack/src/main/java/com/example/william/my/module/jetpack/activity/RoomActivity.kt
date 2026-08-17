package com.example.william.my.module.jetpack.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.jetpack.oauth.OAuth
import com.example.william.my.module.jetpack.oauth.OAuthDao
import com.example.william.my.module.jetpack.oauth.OAuthDataBase
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * Room
 * https://developer.android.google.cn/jetpack/androidx/releases/room
 */
@Route(path = RouterPath.JetPack.Room)
class RoomActivity : BasicResponseActivity() {

    private val mOAuthDao: OAuthDao by lazy {
        OAuthDataBase.getInstance(this).getOAuthDao()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项操作 Room 数据库")
        showOAuth()
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("插入 OAuth 数据", "清空 OAuth 数据")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> addOAuth()
            1 -> clearOAuth()
        }
    }

    private fun addOAuth() {
        /*
         * query ,Insert ,Update ,需要在后台线程执行
         */
        Executors.newSingleThreadExecutor().execute {
            val oAuth = OAuth()
            mOAuthDao.insertOAuth(oAuth)
            appendLog("插入 OAuth 数据成功")
        }
    }

    private fun clearOAuth() {
        Executors.newSingleThreadExecutor().execute {
            mOAuthDao.deleteAllOAuth()
            appendLog("清空 OAuth 数据成功")
        }
    }

    private fun showOAuth() {
        lifecycleScope.launch {
            mOAuthDao.getAllOAuthFlow().collect { list: List<OAuth> ->
                if (list.isEmpty()) {
                    appendLog("当前 Room 无数据")
                } else {
                    list.forEach { oauth ->
                        appendLog(Gson().toJson(oauth))
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mOAuthDao.deleteAllOAuth()
    }
}