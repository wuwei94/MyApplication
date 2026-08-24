package com.example.william.my.module.imageloader.activity

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
 * Glide 4 — 高性能图片加载框架
 *
 * Glide 是 Android 最流行的图片加载框架，由 Google 推荐使用。
 *
 * 核心特性：
 * 1. 多级缓存：内存缓存 + 磁盘缓存，自动管理缓存生命周期
 * 2. 丰富的变换：circleCrop（圆形裁剪）、RoundedCorners（圆角）、centerCrop（居中裁剪）等
 * 3. 生命周期感知：自动绑定 Activity/Fragment 生命周期，避免内存泄漏
 * 4. 渐变动画：支持 crossFade 等过渡动画，提升用户体验
 *
 * 基本用法：
 * ```kotlin
 * Glide.with(context)
 *     .load(url)
 *     .placeholder(R.drawable.placeholder)  // 占位图
 *     .error(R.drawable.error)              // 错误图
 *     .circleCrop()                         // 圆形裁剪
 *     .into(imageView)
 * ```
 *
 * 适用场景：
 * - 列表/网格中的图片加载
 * - 用户头像、商品图片等需要圆形/圆角显示
 * - 需要缓存和生命周期管理的场景
 *
 * https://github.com/bumptech/glide
 */
@Route(path = RouterPath.ImageLoader.Glide)
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
