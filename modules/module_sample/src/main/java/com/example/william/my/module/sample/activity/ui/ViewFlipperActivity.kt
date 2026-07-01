package com.example.william.my.module.sample.activity.ui

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.router.path.RouterPath
import com.example.william.my.lib.activity.BaseVBActivity
import com.example.william.my.module.sample.databinding.UiActivityViewFlipperBinding

@Route(path = RouterPath.Sample.UI.ViewFlipper)
class ViewFlipperActivity : BaseVBActivity<UiActivityViewFlipperBinding>() {

    override fun getViewBinding(): UiActivityViewFlipperBinding {
        return UiActivityViewFlipperBinding.inflate(layoutInflater)
    }

    override fun onStart() {
        super.onStart()
        mBinding.viewFlipper.startFlipping()
    }

    override fun onStop() {
        super.onStop()
        mBinding.viewFlipper.stopFlipping()
    }
}