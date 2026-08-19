package com.example.william.my.module.sample.activity

import android.graphics.Typeface
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Typeface 字体加载与切换演示
 *
 * 演示：
 * 1. 系统内置字体族（DEFAULT、DEFAULT_BOLD、MONOSPACE、SERIF、SANS_SERIF）。
 * 2. 外部字体文件加载（通过 `Typeface.createFromAsset(assets, path)`）。
 */
@Route(path = RouterPath.Sample.Typeface)
class TypefaceActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 Android Typeface 字体加载与动态切换")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "默认字体（Typeface.DEFAULT）",
            "粗体样式（Typeface.DEFAULT_BOLD）",
            "等宽字体（Typeface.MONOSPACE）",
            "衬线字体（Typeface.SERIF）",
            "Asset 自定义字体（fonts/juice.ttf）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                mBinding.basicsResponse.typeface = Typeface.DEFAULT
                appendLog("【字体切换】已切换为系统默认字体（DEFAULT）")
            }

            1 -> {
                mBinding.basicsResponse.typeface = Typeface.DEFAULT_BOLD
                appendLog("【字体切换】已切换为系统粗体（DEFAULT_BOLD）")
            }

            2 -> {
                mBinding.basicsResponse.typeface = Typeface.MONOSPACE
                appendLog("【字体切换】已切换为等宽字体（MONOSPACE）")
            }

            3 -> {
                mBinding.basicsResponse.typeface = Typeface.SERIF
                appendLog("【字体切换】已切换为衬线字体（SERIF）")
            }

            4 -> {
                try {
                    val typeface = Typeface.createFromAsset(assets, "fonts/juice.ttf")
                    mBinding.basicsResponse.typeface = typeface
                    appendLog("【字体切换】已成功加载 Asset 字体文件: fonts/juice.ttf")
                } catch (e: Exception) {
                    appendLog("【字体加载失败】${e.message}")
                }
            }
        }
    }
}
