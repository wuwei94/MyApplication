package com.example.william.my.module.kotlin.activity

import android.view.View
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

    override fun observeViewModel() {
        super.observeViewModel()

        mViewModel.login.observe(this) {
            showResponse(it)
        }
    }

    override fun onResponseClick(view: View) {
        super.onResponseClick(view)

        mViewModel.login(Constants.Value_Username, Constants.Value_Password)
    }
}