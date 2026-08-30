package com.example.william.my.core.base.utils

import android.content.res.Resources

/**
 * 屏幕适配工具类（基于修改 density 的今日头条方案）
 *
 * 设计基准宽度：360dp（对应 1080px 设计稿在 3x 密度下的标准宽度 360dp）
 */
object DensityAdaptUtils {

    private const val DEFAULT_DESIGN_WIDTH_DP = 360f

    /**
     * 适配 Resources 的 DisplayMetrics 与 Configuration
     *
     * @param resources 当前 Activity 的 Resources
     * @param designWidthDp 设计基准宽度（dp），默认 360dp
     * @return 适配后的 Resources
     */
    fun adaptWidth(resources: Resources, designWidthDp: Float = DEFAULT_DESIGN_WIDTH_DP): Resources {
        val dm = resources.displayMetrics
        val targetDensity = dm.widthPixels / designWidthDp
        val targetDensityDpi = (160 * targetDensity).toInt()

        dm.density = targetDensity
        dm.densityDpi = targetDensityDpi
        resources.configuration.densityDpi = targetDensityDpi

        return resources
    }
}
