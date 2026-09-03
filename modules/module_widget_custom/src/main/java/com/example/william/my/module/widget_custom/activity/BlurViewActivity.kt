package com.example.william.my.module.widget_custom.activity

import android.os.Bundle
import android.widget.SeekBar
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget_custom.databinding.DemoActivityBlurViewBinding

/**
 * 高斯模糊 — BlurView 自定义控件演示
 *
 * 使用项目自定义 BlurView 控件实现高斯模糊效果。通过 SeekBar 实时调节模糊半径（0~100），
 * 支持对指定 ImageView 或背景图进行实时模糊处理。
 *
 * 核心原理：
 * 1. BlurView 内部使用 Bitmap 缩放 + 模糊算法实现高斯模糊
 * 2. 通过 setImageView() 设置模糊源图片
 * 3. 通过 setImageBlur(radius) 实时调节模糊程度
 * 4. 相比 RenderScript / RenderEffect 方案，兼容性更好（无 API 版本限制）
 *
 * 适用场景：
 * - 毛玻璃效果（如弹窗背景模糊）
 * - 背景虚化（如个人中心头像区域）
 * - 实时模糊调节交互（配合 SeekBar / Slider）
 *
 * @see RenderScriptActivity RenderScript 模糊方案（已废弃，API 31 起废弃）
 * @see RenderEffectActivity RenderEffect 模糊方案（推荐，API 31+）
 */
@Route(path = RouterPath.WidgetCustom.BlurView)
class BlurViewActivity : BaseVBActivity<DemoActivityBlurViewBinding>() {

    override fun getViewBinding(): DemoActivityBlurViewBinding {
        return DemoActivityBlurViewBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initBlurView()
    }

    private fun initBlurView() {
        mBinding.blurSeekBar.max = 100
        mBinding.blurBlurView.setImageView(R.drawable.shared_ic_launcher)
        mBinding.blurSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mBinding.blurBlurView.setImageBlur(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }
}