package com.example.william.my.module.arch.activity

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseFragmentActivity
import com.example.william.my.module.arch.fragment.MviFragment

/**
 * MVI：Model-View-Intent
 */
@Route(path = RouterPath.Arch.MVI)
class MviActivity : BaseFragmentActivity() {

    override fun setFragment(): Fragment {
        return MviFragment()
    }
}