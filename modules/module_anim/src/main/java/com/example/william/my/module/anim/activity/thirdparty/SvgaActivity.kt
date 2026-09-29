package com.example.william.my.module.anim.activity.thirdparty

import android.net.http.HttpResponseCache
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import java.io.File

/**
 * SVGA — 跨平台动效布局预览
 *
 * 核心机制与避坑点：
 * 1. 解析播放：SVGAParser 解析 .svga 后交给 SVGAImageView 渲染
 * 2. 缓存：可配合 HttpResponseCache 缓存远端资源
 * 3. 布局预览：本页解析本地/远端资源并播放，无交互操作项
 *
 * 官方参考：
 * https://github.com/svga/SVGAPlayer-Android
 */
@Route(path = RouterPath.Anim.Svga)
class SvgaActivity : BasicLayoutActivity() {

    private lateinit var svgaImageView: SVGAImageView

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initPreview()
        initSvgaPlayer()
    }

    private fun initPreview() {
        val previewView = layoutInflater.inflate(
            com.example.william.my.module.anim.R.layout.anim_layout_svga_preview,
            container,
            false,
        )
        svgaImageView = previewView.findViewById(
            com.example.william.my.module.anim.R.id.anim_svga_player,
        )
        setView(previewView)
    }

    /**
     * 解析并播放本地 SVGA 资源。
     *
     * 先安装 HttpResponseCache（库内部走 HTTP 时可复用），再经 SVGAParser 解码到 ImageView。
     */
    private fun initSvgaPlayer() {
        HttpResponseCache.install(
            File(applicationContext.cacheDir, "svg"),
            (1024 * 1024 * 128).toLong(),
        )
        SVGAParser.shareParser().init(this)
        SVGAParser.shareParser()
            .decodeFromAssets(
                Constants.Url_SVGA_Heartbeat,
                object : SVGAParser.ParseCompletion {
                    override fun onError() {}
                    override fun onComplete(videoItem: SVGAVideoEntity) {
                        svgaImageView.setImageDrawable(SVGADrawable(videoItem))
                        svgaImageView.startAnimation()
                    }
                },
            )
    }
}
