package com.example.william.my.basic.basic_shared.activity

import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.databinding.BasicsLayoutResponseRecyclerBinding

abstract class BasicResponseActivity : BasicRecyclerActivity() {

    protected lateinit var mBinding: BasicsLayoutResponseRecyclerBinding

    override fun initViewBinding() {
        mBinding = BasicsLayoutResponseRecyclerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mRecycler = mBinding.basicsRecycler
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.basicsResponse.setOnClickListener {
            onResponseClick(it)
        }
    }

    protected open fun onResponseClick(view: View) {

    }

    override fun showResponse(response: String?) {
        runOnUiThread {
            response?.let {
                if (!it.startsWith("onResponse: ")) {
                    mBinding.basicsResponse.text = it
                    mBinding.basicsResponse.gravity = Gravity.CENTER
                } else {
                    mBinding.basicsResponse.text = it.formatString()
                    mBinding.basicsResponse.gravity = Gravity.NO_GRAVITY
                }
            }
        }
    }
}
