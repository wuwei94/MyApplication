package com.example.william.my.module.sample.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.activity.BasicResponseActivity
import com.example.william.my.basic.basic_module.router.path.RouterPath
import com.example.william.my.module.sample.hook.HookManager

@Route(path = RouterPath.Sample.Hook)
class HookActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        setTag()

        setHook()
    }

    private fun setTag() {
        HookManager.setViewTag(mBinding.basicsResponse, "name", "hook")
    }

    private fun setHook() {
        HookManager.hookOnClick(this, mBinding.basicsResponse)
    }
}
