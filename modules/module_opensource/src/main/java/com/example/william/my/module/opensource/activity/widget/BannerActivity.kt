package com.example.william.my.module.opensource.activity.widget

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.opensource.databinding.OpenActivityBannerBinding
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator

/**
 * https://github.com/youth5201314/banner
 */
@Route(path = RouterPath.OpenSource.Widget.Banner)
class BannerActivity : BaseVBActivity<OpenActivityBannerBinding>() {

    override fun getViewBinding(): OpenActivityBannerBinding {
        return OpenActivityBannerBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initBanner()
    }

    private fun initBanner() {

        mBinding.banner.setAdapter(object :
            BannerImageAdapter<String>(arrayListOf("1", "2", "3", "4")) {
            override fun onBindView(
                holder: BannerImageHolder, data: String, position: Int, size: Int
            ) {
                holder.imageView?.setImageResource(R.drawable.shared_ic_launcher)
            }
        })
            .setIndicator(CircleIndicator(this))
            .addBannerLifecycleObserver(this) // 添加生命周期观察者
    }
}