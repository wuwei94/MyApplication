package com.example.william.my.module.anim.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.anim.databinding.AnimActivityPagBinding

/**
 * PAG — 二进制动效布局预览
 *
 * 核心机制与避坑点：
 * 1. 二进制格式：PAG 文件比 JSON Lottie 更紧凑，适合端内资源分发
 * 2. 循环播放：setRepeatCount(-1) 无限循环；页面销毁时随 View 释放
 * 3. 布局预览：本页加载远端资源并自动播放，无交互操作项
 *
 * 官方参考：
 * https://github.com/Tencent/libpag
 */
@Route(path = RouterPath.Anim.Pag)
class PagActivity : BaseVBActivity<AnimActivityPagBinding>() {

    override fun getViewBinding(): AnimActivityPagBinding = AnimActivityPagBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initPagAnim()
    }

    /**
     * 加载远端 PAG 资源并循环播放。
     */
    private fun initPagAnim() {
        binding.pagImageView.let {
            it.path = Constants.Url_PAG
            it.setRepeatCount(-1)
            it.play()
        }
    }
}
