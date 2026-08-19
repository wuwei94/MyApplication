package com.example.william.my.module.sample.activity

import android.graphics.Typeface
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Typeface — 字体加载与切换
 *
 * Typeface 是 Android 的字体管理类，用于加载和切换字体样式。
 *
 * 核心特性：
 * 1. 系统内置字体：DEFAULT、DEFAULT_BOLD、MONOSPACE、SERIF、SANS_SERIF
 * 2. 自定义字体：支持从 assets、resources、文件加载字体
 * 3. 字体样式：支持粗体、斜体、粗斜体等样式组合
 * 4. 性能优化：字体文件会被缓存，避免重复加载
 *
 * 基本用法：
 * ```kotlin
 * // 系统内置字体
 * textView.typeface = Typeface.DEFAULT
 * textView.typeface = Typeface.DEFAULT_BOLD
 * textView.typeface = Typeface.MONOSPACE
 *
 * // 自定义字体
 * val typeface = Typeface.createFromAsset(assets, "fonts/custom.ttf")
 * textView.typeface = typeface
 * ```
 *
 * 适用场景：
 * - 自定义字体样式
 * - 品牌字体展示
 * - 特殊字体需求（如等宽字体、手写字体）
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
