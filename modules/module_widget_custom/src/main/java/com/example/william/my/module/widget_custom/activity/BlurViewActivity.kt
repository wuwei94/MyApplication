package com.example.william.my.module.widget_custom.activity

import android.os.Bundle
import android.widget.SeekBar
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget_custom.databinding.DemoActivityBlurViewBinding

/**
 * BlurView — 局部背景高斯模糊控件
 *
 * 使用项目自定义 BlurView 控件实现高斯模糊效果。通过 SeekBar 实时调节模糊半径（0~100），
 * 支持对指定 ImageView 或背景图进行实时模糊处理。
 *
 * 核心机制与避坑点：
 * 1. Bitmap 缩放模糊：内部使用 Bitmap 缩放 + 模糊算法，无 API 版本限制
 * 2. 指定模糊源：通过 setImageView() 绑定需要模糊的源图片
 * 3. 实时半径调节：通过 setImageBlur(radius) 动态调整模糊程度
 * 4. 兼容性优先：相比 RenderScript / RenderEffect，全版本可用
 *
 * @see RenderScriptActivity RenderScript 模糊方案（已废弃，API 31 起废弃）
 * @see RenderEffectActivity RenderEffect 模糊方案（推荐，API 31+）
 * https://developer.android.com/media/platform/rendereffect
 */
@Route(path = RouterPath.WidgetCustom.BlurView)
class BlurViewActivity : BaseVBActivity<DemoActivityBlurViewBinding>() {

    override fun getViewBinding(): DemoActivityBlurViewBinding = DemoActivityBlurViewBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initBlurView()
    }

    private fun initBlurView() {
        binding.blurSeekBar.max = 100
        binding.blurBlurView.setImageView(R.drawable.shared_ic_launcher)
        binding.blurSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.blurBlurView.setImageBlur(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }
}
