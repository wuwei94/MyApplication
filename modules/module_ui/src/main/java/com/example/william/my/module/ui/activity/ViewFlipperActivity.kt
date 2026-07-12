package com.example.william.my.module.ui.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.lib.activity.BaseVBActivity
import com.example.william.my.module.ui.databinding.UiActivityViewFlipperBinding

@Route(path = RouterPath.UI.ViewFlipper)
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