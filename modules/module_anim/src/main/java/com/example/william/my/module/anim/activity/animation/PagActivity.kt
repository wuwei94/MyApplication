package com.example.william.my.module.anim.activity.animation

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.anim.databinding.AnimActivityPagBinding

/**
 * PAG (Portable Animated Graphics) — 腾讯开源的动画渲染方案
 *
 * PAG 是腾讯研发的轻量级动画渲染方案，支持 AE 动画导出和实时渲染。
 *
 * 核心特性：
 * 1. 高性能：支持硬件加速，渲染效率高
 * 2. 全平台覆盖：支持 Android、iOS、Web、Desktop
 * 3. 丰富的特效：支持粒子、描边、填充等 AE 特效
 * 4. 文件体积小：二进制格式，比 JSON 更紧凑
 *
 * 基本用法：
 * ```kotlin
 * pagView.setRepeatCount(-1)  // 无限循环
 * pagView.play()              // 开始播放
 * pagView.pause()             // 暂停播放
 * ```
 *
 * 适用场景：
 * - 启动动画、加载动画
 * - 社交表情、礼物动画
 * - 复杂的 AE 动画展示
 *
 * https://github.com/Tencent/libpag
 */
@Route(path = RouterPath.Anim.Pag)
class PagActivity : BaseVBActivity<AnimActivityPagBinding>() {

    override fun getViewBinding(): AnimActivityPagBinding {
        return AnimActivityPagBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initPagAnim()
    }

    private fun initPagAnim() {
        mBinding.pagImageView.let {
            it.path = Constants.Url_PAG
            it.setRepeatCount(-1)
            it.play()
        }
    }
}
