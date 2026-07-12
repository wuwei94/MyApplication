package com.example.william.my.module.component.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.databinding.BasicsLayoutResponseBinding
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.lib.activity.BaseFragmentActivity
import com.example.william.my.lib.fragment.BaseVBFragment

/**
 * onBackPressedDispatcher
 */
@Route(path = RouterPath.Component.OnBackPressed)
class OnBackPressedActivity : BaseFragmentActivity() {

    override fun setFragment(): Fragment {
        return BackPressedFragment()
    }

    class BackPressedFragment : BaseVBFragment<BasicsLayoutResponseBinding>() {

        override fun getViewBinding(): BasicsLayoutResponseBinding {
            return BasicsLayoutResponseBinding.inflate(layoutInflater)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            requireActivity().onBackPressedDispatcher.addCallback(object :
                OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Utils.toast("handleOnBackPressed")
                    requireActivity().finish()
                }
            })
        }
    }
}
