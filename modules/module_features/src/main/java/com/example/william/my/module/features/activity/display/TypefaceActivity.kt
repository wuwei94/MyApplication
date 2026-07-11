package com.example.william.my.module.features.activity.display

import android.graphics.Typeface
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.activity.BasicResponseActivity
import com.example.william.my.basic.basic_module.router.path.RouterPath

@Route(path = RouterPath.Features.Display.Typeface)
class TypefaceActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initTypeface()
    }

    private fun initTypeface() {
        val typeface = Typeface.createFromAsset(assets, "fonts/juice.ttf")
        mBinding.basicsResponse.typeface = typeface
    }
}
