package com.example.william.my.module.imageloader.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.imageloader.IImageLoader
import com.example.william.my.core.imageloader.coil.ImageLoader as CoilImageLoader
import com.example.william.my.core.imageloader.glide.ImageLoader as GlideImageLoader

/**
 * lib_image_loader — 统一图片加载封装
 *
 * `lib_image_loader` 通过 [IImageLoader] 抽象出统一的图片加载 API，
 * 并分别基于 Coil 与 Glide 提供两套实现（实现类名均为 `ImageLoader`）。
 *
 * 本页演示同一套 `IImageLoader` 接口在不同底层实现之间的无感切换，
 * 调用方无需关心底层是 Coil 还是 Glide。
 *
 * 核心 API：
 * - loadImage：普通加载
 * - loadImageRound：圆形加载
 * - loadImageRadius：圆角加载
 * - clear：清除图片
 */
@Route(path = RouterPath.ImageLoader.ImageLoader)
class ImageLoaderActivity : BasicImageActivity() {

    // 默认使用 Glide 实现，列表第一项可切换为 Coil 实现
    private var mImageLoader: IImageLoader = GlideImageLoader

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        loadImage()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "切换实现（Glide ⇄ Coil）",
        "基础加载 loadImage",
        "圆形加载 loadImageRound",
        "圆角加载 loadImageRadius",
        "清除图片 clear",
        "异常链接（触发 error）",
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
        mImageLoader = if (mImageLoader === GlideImageLoader) CoilImageLoader else GlideImageLoader
        loadImage()
    }

    private fun loadImage() {
        with(mImageLoader) {
            mBinding.basicsImage.loadImage(this@ImageLoaderActivity, Constants.Url_Image1)
        }
    }

    private fun loadRound() {
        with(mImageLoader) {
            mBinding.basicsImage.loadImageRound(this@ImageLoaderActivity, Constants.Url_Image2)
        }
    }

    private fun loadRadius() {
        with(mImageLoader) {
            mBinding.basicsImage.loadImageRadius(this@ImageLoaderActivity, Constants.Url_Image1, 48)
        }
    }

    private fun clear() {
        with(mImageLoader) {
            mBinding.basicsImage.clear(this@ImageLoaderActivity)
        }
    }

    private fun loadError() {
        with(mImageLoader) {
            mBinding.basicsImage.loadImage(this@ImageLoaderActivity, "https://invalid-url.example.com/not_exist.png")
        }
    }
}
