package com.example.william.my.module.image_loader.activity

import android.os.Bundle
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Coil 3 — 基于 Kotlin 协程的图片加载库
 *
 * 核心机制与避坑点：
 * 1. 协程驱动：ImageView.load 扩展在 View 上发起异步请求，请求与 View 生命周期自动关联取消；默认复用全局 ImageLoader 单例
 * 2. 缓存策略：内存 + 磁盘缓存自动管理，同一 URL 重复 load 命中缓存直接展示
 * 3. 请求配置：ImageRequest.Builder 内配置 crossfade / placeholder / error，失败时展示 error 资源
 * 4. 版本约束：coil3 包名与 coil2 不兼容，依赖与 API 需按大版本对齐
 *
 * 官方参考：
 * https://coil-kt.github.io/coil/
 */
@Route(path = RouterPath.ImageLoader.Coil)
class CoilActivity : BasicImageActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        loadDefault()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 加载基础图片 (Url_Image1)",
        "2. 切换第二张图片 (Url_Image2)",
        "3. 启用淡入过渡动画 (crossfade)",
        "4. 配置占位图与错误图 (placeholder / error)",
        "5. 加载异常链接触发 error 回退",
    )

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
        binding.basicsImage.load(Constants.Url_Image1)
    }

    private fun loadSecond() {
        binding.basicsImage.load(Constants.Url_Image2)
    }

    private fun loadWithCrossfade() {
        binding.basicsImage.load(Constants.Url_Image1) {
            crossfade(true)
            crossfade(1000)
        }
    }

    private fun loadWithPlaceholder() {
        binding.basicsImage.load(Constants.Url_Image2) {
            placeholder(R.drawable.shared_ic_launcher)
            error(R.drawable.shared_ic_launcher)
        }
    }

    private fun loadError() {
        binding.basicsImage.load("https://invalid-url.example.com/not_exist.png") {
            placeholder(R.drawable.shared_ic_launcher)
            error(R.drawable.shared_ic_launcher)
        }
    }
}
