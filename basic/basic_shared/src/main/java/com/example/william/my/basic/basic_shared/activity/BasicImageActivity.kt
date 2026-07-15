package com.example.william.my.basic.basic_shared.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutImageRecyclerBinding

abstract class BasicImageActivity : BasicRecyclerActivity() {

    protected lateinit var mBinding: SharedLayoutImageRecyclerBinding

    override fun initViewBinding() {
        mBinding = SharedLayoutImageRecyclerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mRecycler = mBinding.basicsRecycler
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.basicsImage.setOnClickListener {
            onImageClick(it)
        }
    }

    protected open fun onImageClick(view: View) {

    }

    protected fun showImage(bitmap: Bitmap?) {
        runOnUiThread {
            mBinding.basicsImage.setImageBitmap(bitmap)
        }
    }

    protected fun showImage(resId: Int) {
        runOnUiThread {
            mBinding.basicsImage.setImageResource(resId)
        }
    }
}
