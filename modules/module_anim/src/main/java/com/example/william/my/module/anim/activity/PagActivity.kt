package com.example.william.my.module.anim.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.anim.databinding.AnimActivityPagBinding

/**
 * PAG (Portable Animated Graphics) — 腾讯开源的动画渲染方案
 *
 * PAG 是腾讯研发的轻量级动画渲染方案，支持 AE 动画导出和实时渲染。
 *
 * 核心机制与避坑点：
 * 1. 高性能：支持硬件加速，渲染效率高
 * 2. 全平台覆盖：支持 Android、iOS、Web、Desktop
 * 3. 丰富的特效：支持粒子、描边、填充等 AE 特效
 * 4. 文件体积小：二进制格式，比 JSON 更紧凑
 *
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
