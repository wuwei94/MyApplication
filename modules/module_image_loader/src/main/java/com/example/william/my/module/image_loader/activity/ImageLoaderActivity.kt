package com.example.william.my.module.image_loader.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.imageloader.IImageLoader
import com.example.william.my.core.imageloader.coil.ImageLoader as CoilImageLoader
import com.example.william.my.core.imageloader.glide.ImageLoader as GlideImageLoader

/**
 * lib_image_loader — 统一图片加载抽象层
 *
 * 核心机制与避坑点：
 * 1. 接口抽象：IImageLoader 定义 loadImage / loadImageRound / loadImageRadius / clear，调用方不感知底层实现
 * 2. 双实现切换：coil 与 glide 两套 ImageLoader 可在运行时替换，同一 View 调用路径保持一致
 * 3. 实现约束：各实现内部需自行处理生命周期取消与主线程回调，封装层不做二次调度
 */
@Route(path = RouterPath.ImageLoader.ImageLoader)
class ImageLoaderActivity : BasicImageActivity() {

    // 默认使用 Glide 实现，列表第一项可切换为 Coil 实现
    private var imageLoader: IImageLoader = GlideImageLoader

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        loadImage()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 切换底层实现 (Glide ⇄ Coil)",
        "2. 加载基础图片 (loadImage)",
        "3. 加载圆形图片 (loadImageRound)",
        "4. 加载圆角图片 (loadImageRadius)",
        "5. 清除当前图片 (clear)",
        "6. 加载异常链接触发错误回退",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> switchLoader()
            1 -> loadImage()
            2 -> loadRound()
            3 -> loadRadius()
            4 -> clear()
            5 -> loadError()
        }
    }

    private fun switchLoader() {
        imageLoader = if (imageLoader === GlideImageLoader) CoilImageLoader else GlideImageLoader
        loadImage()
    }

    private fun loadImage() {
        with(imageLoader) {
            binding.basicsImage.loadImage(this@ImageLoaderActivity, Constants.Url_Image1)
        }
    }

    private fun loadRound() {
        with(imageLoader) {
            binding.basicsImage.loadImageRound(this@ImageLoaderActivity, Constants.Url_Image2)
        }
    }

    private fun loadRadius() {
        with(imageLoader) {
            binding.basicsImage.loadImageRadius(this@ImageLoaderActivity, Constants.Url_Image1, 48)
        }
    }

    private fun clear() {
        with(imageLoader) {
            binding.basicsImage.clear(this@ImageLoaderActivity)
        }
    }

    private fun loadError() {
        with(imageLoader) {
            binding.basicsImage.loadImage(this@ImageLoaderActivity, "https://invalid-url.example.com/not_exist.png")
        }
    }
}
