package com.example.william.my.module.arch.activity

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.lib.activity.BaseFragmentActivity
import com.example.william.my.module.arch.fragment.MvvmFragment

/**
 * MVVM：Model-View-ViewModel
 */
@Route(path = RouterPath.Arch.MVVM)
class MvvmActivity : BaseFragmentActivity() {

    override fun setFragment(): Fragment {
        return MvvmFragment()
    }
}