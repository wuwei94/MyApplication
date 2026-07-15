package com.example.william.my.module.opensource.activity.imageloader

import android.os.Bundle
import coil.load
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * https://github.com/coil-kt/coil
 * 圆形变换（CircleCropTransformation）和圆角变换（RoundedCornersTransformation）
 */
@Route(path = RouterPath.OpenSource.ImageLoader.Coil)
class CoilActivity : BasicImageActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        load()
    }

    private fun load() {
        mBinding.basicsImage.load(Constants.Url_Image1)
    }
}
