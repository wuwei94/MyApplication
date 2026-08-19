package com.example.william.my.module.opensource.activity.imageloader

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Glide 4 图片加载框架演示
 *
 * 核心特性：
 * 1. 完善的多级内存/磁盘缓存机制。
 * 2. 丰富的图像变换（circleCrop、RoundedCorners、centerCrop 等）。
 * 3. 动画与渐变过渡（DrawableTransitionOptions.withCrossFade）。
 * 4. 自动感知 Activity / Fragment 生命周期。
 */
@Route(path = RouterPath.OpenSource.Glide)
class GlideActivity : BasicImageActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        loadDefault()
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "基础图片加载（Url_Image1）",
            "圆形裁剪（circleCrop）",
            "圆角变换（RoundedCorners: 48px）",
            "居中裁剪（centerCrop）",
            "占位图与渐变动画（crossFade）",
            "加载异常链接（触发 error 占位图）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> loadDefault()
            1 -> loadCircleCrop()
            2 -> loadRoundedCorners()
            3 -> loadCenterCrop()
            4 -> loadWithCrossFade()
            5 -> loadError()
        }
    }

    private fun loadDefault() {
        Glide.with(this)
            .load(Constants.Url_Image1)
            .into(mBinding.basicsImage)
    }

    private fun loadCircleCrop() {
        Glide.with(this)
            .load(Constants.Url_Image1)
            .circleCrop()
            .into(mBinding.basicsImage)
    }

    private fun loadRoundedCorners() {
        Glide.with(this)
            .load(Constants.Url_Image1)
            .transform(RoundedCorners(48))
            .into(mBinding.basicsImage)
    }

    private fun loadCenterCrop() {
        Glide.with(this)
            .load(Constants.Url_Image1)
            .centerCrop()
            .into(mBinding.basicsImage)
    }

    private fun loadWithCrossFade() {
        Glide.with(this)
            .load(Constants.Url_Image2)
            .placeholder(R.drawable.shared_ic_launcher)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(mBinding.basicsImage)
    }

    private fun loadError() {
        Glide.with(this)
            .load("https://invalid-url.example.com/not_exist.png")
            .placeholder(R.drawable.shared_ic_launcher)
            .error(R.drawable.shared_ic_launcher)
            .into(mBinding.basicsImage)
    }
}
