package com.example.william.my.module.kotlin.activity

import android.os.Bundle
import androidx.activity.viewModels
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.kotlin.viewmodel.CoroutinesVMFactory
import com.example.william.my.module.kotlin.viewmodel.CoroutinesViewModel

/**
 * Android 上的 Kotlin 协程
 * https://developer.android.google.cn/kotlin/coroutines
 */
@Route(path = RouterPath.Kotlin.Coroutines)
class CoroutinesActivity : BasicResponseActivity() {

    private val mViewModel: CoroutinesViewModel by viewModels {
        CoroutinesVMFactory
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项发起协程登录请求")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("协程登录请求")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        if (position == 0) {
            appendLog("发起登录请求...")
            mViewModel.login(Constants.Value_Username, Constants.Value_Password)
        }
    }

    override fun observeViewModel() {
        super.observeViewModel()

        mViewModel.login.observe(this) {
            appendLog("登录结果: $it")
        }
    }
}