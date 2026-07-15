package com.example.william.my.module.opensource.activity.imageloader

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * https://github.com/bumptech/glide
 */
@Route(path = RouterPath.OpenSource.ImageLoader.Glide)
class GlideActivity : BasicImageActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        load()
    }

    private fun load() {
        Glide.with(this).load(Constants.Url_Image1).into(mBinding.basicsImage)
    }
}
