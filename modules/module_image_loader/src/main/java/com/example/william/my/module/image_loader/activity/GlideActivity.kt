package com.example.william.my.module.image_loader.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Glide — 生命周期感知的 Android 图片加载框架
 *
 * 核心机制与避坑点：
 * 1. 生命周期绑定：Glide.with(activity/fragment) 将请求挂到 Lifecycle，页面销毁自动 pause/cancel，避免泄漏
 * 2. 多级缓存：内存 + 磁盘缓存按变换后的尺寸与签名区分，同一 URL 不同 transform 分别缓存
 * 3. 变换链：circleCrop / RoundedCorners / centerCrop 等 BitmapTransformation 顺序生效，可组合
 * 4. 加载失败：placeholder 在加载中展示，error 在失败时回退；主线程发起，结果回调回主线程
 *
 * 官方参考：
 * https://bumptech.github.io/glide/
 */
@Route(path = RouterPath.ImageLoader.Glide)
class GlideActivity : BasicImageActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        loadDefault()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 加载基础图片 (Url_Image1)",
        "2. 圆形裁剪 (circleCrop)",
        "3. 圆角变换 (RoundedCorners: 48px)",
        "4. 居中裁剪 (centerCrop)",
        "5. 占位图与渐变动画 (crossFade)",
        "6. 加载异常链接触发 error 占位图",
    )

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
            .into(binding.basicsImage)
    }

    private fun loadCircleCrop() {
        Glide.with(this)
            .load(Constants.Url_Image1)
            .circleCrop()
            .into(binding.basicsImage)
    }

    private fun loadRoundedCorners() {
        Glide.with(this)
            .load(Constants.Url_Image1)
            .transform(RoundedCorners(48))
            .into(binding.basicsImage)
    }

    private fun loadCenterCrop() {
        Glide.with(this)
            .load(Constants.Url_Image1)
            .centerCrop()
            .into(binding.basicsImage)
    }

    private fun loadWithCrossFade() {
        Glide.with(this)
            .load(Constants.Url_Image2)
            .placeholder(R.drawable.shared_ic_launcher)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.basicsImage)
    }

    private fun loadError() {
        Glide.with(this)
            .load("https://invalid-url.example.com/not_exist.png")
            .placeholder(R.drawable.shared_ic_launcher)
            .error(R.drawable.shared_ic_launcher)
            .into(binding.basicsImage)
    }
}
