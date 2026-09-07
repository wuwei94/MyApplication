package com.example.william.my.core.base.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [DensityAdaptUtils] 屏幕适配核心算法单元测试
 */
class DensityAdaptUtilsTest {

    @Test
    fun calculateDensity_standardWidth_calculatesCorrectDensityAndDpi() {
        // 1080px 宽度，基准 360dp，系统原生 density 2.75，scaledDensity 2.75（fontScale = 1.0）
        val result = calculateDensity(
            widthPixels = 1080,
            noncompatDensity = 2.75f,
            noncompatScaledDensity = 2.75f,
            designWidthDp = 360f,
        )

        // targetDensity = 1080 / 360 = 3.0f
        assertEquals(3.0f, result.density, 0.001f)
        // targetScaledDensity = 3.0f * 1.0f = 3.0f
        assertEquals(3.0f, result.scaledDensity, 0.001f)
        // targetDensityDpi = (160 * 3.0).toInt() = 480
        assertEquals(480, result.densityDpi)
    }

    @Test
    fun calculateDensity_customFontScale_scalesFontCorrectly() {
        // 用户在系统设置中放大了字体：fontScale = 1.25（scaledDensity = 3.4375, density = 2.75）
        val result = calculateDensity(
            widthPixels = 1080,
            noncompatDensity = 2.75f,
            noncompatScaledDensity = 3.4375f,
            designWidthDp = 360f,
        )

        assertEquals(3.0f, result.density, 0.001f)
        // 缩放后的 targetScaledDensity 应为 3.0f * 1.25f = 3.75f
        assertEquals(3.75f, result.scaledDensity, 0.001f)
        assertEquals(480, result.densityDpi)
    }

    @Test
    fun calculateDensity_customDesignWidth_calculatesCustomDensity() {
        // 自定义平板或宽屏设计稿 720dp，真实屏幕 1440px
        val result = calculateDensity(
            widthPixels = 1440,
            noncompatDensity = 2.0f,
            noncompatScaledDensity = 2.0f,
            designWidthDp = 720f,
        )

        // targetDensity = 1440 / 720 = 2.0f
        assertEquals(2.0f, result.density, 0.001f)
        assertEquals(2.0f, result.scaledDensity, 0.001f)
        assertEquals(320, result.densityDpi)
    }
}
