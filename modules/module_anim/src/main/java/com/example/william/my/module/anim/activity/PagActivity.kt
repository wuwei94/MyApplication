package com.example.william.my.module.anim.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import org.libpag.PAGImageView

/**
 * PAG — 二进制动效布局预览
 *
 * 核心机制与避坑点：
 * 1. 二进制格式：PAG 文件比 JSON Lottie 更紧凑，适合端内资源分发
 * 2. 循环播放：setRepeatCount(-1) 无限循环；页面销毁时随 View 释放
 * 3. 布局预览：本页加载资源并自动播放，无交互操作项
 *
 * 官方参考：
 * https://github.com/Tencent/libpag
 */
@Route(path = RouterPath.Anim.Pag)
class PagActivity : BasicLayoutActivity() {

    private lateinit var pagImageView: PAGImageView

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initPreview()
        initPagAnim()
    }

    private fun initPreview() {
        val previewView = layoutInflater.inflate(
            com.example.william.my.module.anim.R.layout.anim_layout_pag_preview,
            container,
            false,
        )
        pagImageView = previewView.findViewById(
            com.example.william.my.module.anim.R.id.anim_pag_player,
        )
        setView(previewView)
    }

    /**
     * 加载 PAG 资源并循环播放。
     */
    private fun initPagAnim() {
        pagImageView.let {
            it.path = Constants.Url_PAG
            it.setRepeatCount(-1)
            it.play()
        }
    }
}
