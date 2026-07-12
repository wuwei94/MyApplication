package com.example.william.my.module.opensource.activity.widget

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.lib.activity.BaseVBActivity
import com.example.william.my.module.opensource.databinding.OpenActivityPhotoViewBinding

/**
 * https://github.com/chrisbanes/PhotoView
 */
@Route(path = RouterPath.OpenSource.Widget.PhotoView)
class PhotoViewActivity : BaseVBActivity<OpenActivityPhotoViewBinding>() {

    override fun getViewBinding(): OpenActivityPhotoViewBinding {
        return OpenActivityPhotoViewBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        showPhotoView()
    }

    private fun showPhotoView() {
        mBinding.photoView.setImageResource(R.drawable.ic_launcher)
    }
}