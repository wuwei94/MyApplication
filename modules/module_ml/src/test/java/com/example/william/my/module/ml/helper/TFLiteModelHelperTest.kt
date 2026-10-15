package com.example.william.my.module.ml.helper

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TFLiteModelHelper 纯函数单元测试
 *
 * 这些函数是端侧推理链路上最容易在「调阈值 / 改坐标映射」时静默出错的部分，
 * 且不依赖 Android 运行时，因此用纯 JVM 单测兜底。
 *
 * 只覆盖 helper 当前对外暴露的纯函数；页面下线的同时，对应的解码函数与用例一并移除。
 */
class TFLiteModelHelperTest {

    // ─────────────────── 推理配置 ───────────────────

    @Test
    fun cpuThreads_clampToTwoFour() {
        assertTrue(TFLiteModelHelper.CPU_THREADS in 2..4)
    }

    @Test
    fun getTopK_descendingWithOriginalIndex() {
        val topK = TFLiteModelHelper.getTopK(floatArrayOf(0.1f, 0.7f, 0.2f), k = 2)

        assertEquals(2, topK.size)
        assertEquals(1, topK[0].first)
        assertEquals(0.7f, topK[0].second, 1e-6f)
        assertEquals(2, topK[1].first)
    }

    @Test
    fun getTopK_returnAllWhenKExceedsSize() {
        assertEquals(2, TFLiteModelHelper.getTopK(floatArrayOf(1f, 2f), k = 5).size)
    }

    // ─────────────────── 词表解析 ───────────────────

    @Test
    fun buildVocabulary_singleColumnLineNumberAsId() {
        val vocabulary = TFLiteModelHelper.buildVocabulary(
            listOf("[PAD]", "[unused0]", "", "the", "a"),
        )

        assertEquals(0, vocabulary["[PAD]"])
        assertEquals(1, vocabulary["[unused0]"])
        assertEquals(3, vocabulary["the"])
        assertEquals(4, vocabulary["a"])
        // 空行必须占位，否则其后所有 token 的 id 都会前移
        assertEquals(2, vocabulary[""])
        assertEquals(5, vocabulary.size)
    }

    @Test
    fun buildVocabulary_doubleColumnSecondColumnAsId() {
        val vocabulary = TFLiteModelHelper.buildVocabulary(
            listOf("<PAD> 0", "<START> 1", "<UNKNOWN> 2", "the 3"),
        )

        assertEquals(0, vocabulary["<PAD>"])
        assertEquals(2, vocabulary["<UNKNOWN>"])
        assertEquals(3, vocabulary["the"])
        // 反向断言：曾经的错法是把整行当 key，导致 vocab["the"] 查不到、全部退化成 PAD
        assertEquals(null, vocabulary["the 3"])
    }

    @Test
    fun buildVocabulary_explicitIdOverridesLineNumber() {
        val vocabulary = TFLiteModelHelper.buildVocabulary(
            listOf("<PAD> 0", "<START> 1", "the 7"),
        )

        assertEquals(7, vocabulary["the"])
    }

    // ─────────────────── 语义分割后处理 ───────────────────

    @Test
    fun decodeSegmentationMask_channelInterleavedArgmax() {
        // 2x2 像素、3 类，行优先且通道交错：像素 p 的三类得分位于 [p*3, p*3+3)
        val logits = floatArrayOf(
            0.2f, 0.9f, 0.1f, // 像素 0 -> 类别 1
            0.7f, 0.1f, 0.2f, // 像素 1 -> 类别 0
            0.1f, 0.1f, 0.8f, // 像素 2 -> 类别 2
            0.9f, 0.1f, 0.0f, // 像素 3 -> 类别 0
        )

        val mask = TFLiteModelHelper.decodeSegmentationMask(
            logits = logits,
            width = 2,
            height = 2,
            classCount = 3,
        )

        assertArrayEquals(intArrayOf(1, 0, 2, 0), mask)
    }

    @Test
    fun decodeSegmentationMask_zeroMaskWhenTooShort() {
        val mask = TFLiteModelHelper.decodeSegmentationMask(
            logits = floatArrayOf(1f, 2f, 3f),
            width = 2,
            height = 2,
            classCount = 3,
        )

        assertArrayEquals(IntArray(4), mask)
    }

    @Test
    fun countSegmentationClasses_countAndIgnoreOutOfRange() {
        val counts = TFLiteModelHelper.countSegmentationClasses(intArrayOf(0, 1, 1, 9, -1), classCount = 2)

        assertArrayEquals(intArrayOf(1, 2), counts)
    }

    @Test
    fun colorizeSegmentationMask_backgroundTransparent() {
        val colors = intArrayOf(0xFF000000.toInt(), 0xFFFF0000.toInt())

        val pixels = TFLiteModelHelper.colorizeSegmentationMask(
            mask = intArrayOf(0, 1),
            colors = colors,
            backgroundTransparent = true,
        )

        assertEquals(0, pixels[0])
        assertEquals(0xFFFF0000.toInt(), pixels[1])
    }

    @Test
    fun colorizeSegmentationMask_keepBackgroundColor() {
        val colors = intArrayOf(0xFF000000.toInt(), 0xFFFF0000.toInt())

        val pixels = TFLiteModelHelper.colorizeSegmentationMask(
            mask = intArrayOf(0, 1),
            colors = colors,
            backgroundTransparent = false,
        )

        assertEquals(0xFF000000.toInt(), pixels[0])
    }

    @Test
    fun colorizeSegmentationMask_outOfRangeFallsBackTransparent() {
        val pixels = TFLiteModelHelper.colorizeSegmentationMask(
            mask = intArrayOf(7),
            colors = intArrayOf(0xFF000000.toInt()),
            backgroundTransparent = false,
        )

        assertEquals(0, pixels[0])
    }
}
