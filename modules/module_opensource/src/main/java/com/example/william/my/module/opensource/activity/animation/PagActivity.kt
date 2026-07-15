package com.example.william.my.module.opensource.activity.animation

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.opensource.databinding.OpenActivityPagBinding

/**
 * https://github.com/Tencent/libpag
 */
@Route(path = RouterPath.OpenSource.Animation.Pag)
class PagActivity : BaseVBActivity<OpenActivityPagBinding>() {

    override fun getViewBinding(): OpenActivityPagBinding {
        return OpenActivityPagBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initPagAnim()
    }

    private fun initPagAnim() {
        mBinding.pagImageView.let {
            it.path = Constants.Url_PAG
            it.setRepeatCount(-1)
            it.play()
        }
    }
}