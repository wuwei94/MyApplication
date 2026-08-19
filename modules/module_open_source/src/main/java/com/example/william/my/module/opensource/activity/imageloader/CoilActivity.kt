package com.example.william.my.module.opensource.activity.imageloader

import android.os.Bundle
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Coil 3 — 现代 Kotlin 图片加载库
 *
 * Coil 是一个基于 Kotlin 协程的现代图片加载库，由 Google 推荐使用。
 *
 * 核心特性：
 * 1. 轻量快速：基于 Kotlin 协程，性能优秀
 * 2. 协程驱动：原生支持 Kotlin 协程与 Flow
 * 3. 自动缓存：内存与磁盘缓存自动管理
 * 4. 简单易用：一行代码加载图片
 *
 * 基本用法：
 * ```kotlin
 * // 一行代码加载图片
 * imageView.load("https://example.com/image.jpg")
 *
 * // 带配置的加载
 * imageView.load(url) {
 *     placeholder(R.drawable.placeholder)
 *     error(R.drawable.error)
 *     crossfade(true)
 * }
 * ```
 *
 * 适用场景：
 * - Kotlin 项目中的图片加载
 * - 需要协程支持的场景
 * - 轻量级图片加载需求
 *
 * https://github.com/coil-kt/coil
 */
@Route(path = RouterPath.OpenSource.Coil)
class CoilActivity : BasicImageActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        loadDefault()
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "基础图片加载（Url_Image1）",
            "切换第二张图片（Url_Image2）",
            "淡入过渡动画（crossfade）",
            "占位图与错误图（placeholder / error）",
            "异常链接加载测试（触发 error）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> loadDefault()
            1 -> loadSecond()
            2 -> loadWithCrossfade()
            3 -> loadWithPlaceholder()
            4 -> loadError()
        }
    }

    private fun loadDefault() {
        mBinding.basicsImage.load(Constants.Url_Image1)
    }

    private fun loadSecond() {
        mBinding.basicsImage.load(Constants.Url_Image2)
    }

    private fun loadWithCrossfade() {
        mBinding.basicsImage.load(Constants.Url_Image1) {
            crossfade(true)
            crossfade(1000)
        }
    }

    private fun loadWithPlaceholder() {
        mBinding.basicsImage.load(Constants.Url_Image2) {
            placeholder(R.drawable.shared_ic_launcher)
            error(R.drawable.shared_ic_launcher)
        }
    }

    private fun loadError() {
        mBinding.basicsImage.load("https://invalid-url.example.com/not_exist.png") {
            placeholder(R.drawable.shared_ic_launcher)
            error(R.drawable.shared_ic_launcher)
        }
    }
}
