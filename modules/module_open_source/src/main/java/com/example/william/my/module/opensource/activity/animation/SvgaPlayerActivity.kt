package com.example.william.my.module.opensource.activity.animation

import android.net.http.HttpResponseCache
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.opensource.databinding.OpenActivitySvgaBinding
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import java.io.File

/**
 * SVGA — 轻量级动画格式
 *
 * SVGA 是一种跨平台的动画格式，由 YY 团队开发，广泛应用于直播礼物、表情等场景。
 *
 * 核心特性：
 * 1. 跨平台：支持 Android、iOS、Web 等多平台
 * 2. 文件体积小：比 GIF 小，比 Lottie 更轻量
 * 3. 高性能：优化的渲染引擎，适合大量动画同时播放
 * 4. 易于集成：设计工具导出即可使用
 *
 * 基本用法：
 * ```kotlin
 * SVGAParser.shareParser().init(context)
 * SVGAParser.shareParser().decodeFromAssets("animation.svga", object : SVGAParser.ParseCompletion {
 *     override fun onComplete(videoItem: SVGAVideoEntity) {
 *         val drawable = SVGADrawable(videoItem)
 *         svgaImageView.setImageDrawable(drawable)
 *         svgaImageView.startAnimation()
 *     }
 *     override fun onError() {}
 * })
 * ```
 *
 * 适用场景：
 * - 直播礼物、打赏动画
 * - 社交表情、贴纸
 * - 营销活动动画
 *
 * https://github.com/svga/SVGAPlayer-Android
 */
@Route(path = RouterPath.OpenSource.SVGAPlayer)
class SvgaPlayerActivity : BaseVBActivity<OpenActivitySvgaBinding>() {

    override fun getViewBinding(): OpenActivitySvgaBinding {
        return OpenActivitySvgaBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initSVGAPlayer()
    }

    private fun initSVGAPlayer() {
        HttpResponseCache.install(
            File(applicationContext.cacheDir, "svg"),
            (1024 * 1024 * 128).toLong()
        )
        SVGAParser.shareParser().init(this)
        SVGAParser.shareParser()
            .decodeFromAssets(Constants.Url_SVGA, object : SVGAParser.ParseCompletion {
                override fun onError() {}
                override fun onComplete(videoItem: SVGAVideoEntity) {
                    val drawable = SVGADrawable(videoItem)
                    mBinding.svgaImageView.setImageDrawable(drawable)
                    mBinding.svgaImageView.startAnimation()
                }
            })
    }
}