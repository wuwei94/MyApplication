package com.example.william.my.module.ml.asr

import com.google.ai.edge.litert.TensorType
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 宿主侧解码纯函数的测试：CTC 的 argmax / 压重复、输入长度的对齐策略、张量元素个数推导。
 *
 * 移植官方 `CtcDecoderTest` 与 `LiteRtRunnerTest` 里的纯函数部分 —— 这三个函数
 * 都不碰 `TensorBuffer`，因此不需要真机或真模型即可验证。
 */
class AsrDecoderTest {

    // ─────────────────── CtcDecoder ───────────────────

    @Test
    fun sampleMaximum_basicArgmax() {
        val decoder = CtcDecoder(vocabSize = 4)
        val logits =
            floatArrayOf(
                0.1f, 0.9f, 0.0f, 0.0f, // 帧 0 -> 1
                0.2f, 0.8f, 0.0f, 0.0f, // 帧 1 -> 1
                0.1f, 0.1f, 0.7f, 0.1f, // 帧 2 -> 2
            )
        assertArrayEquals(intArrayOf(1, 1, 2), decoder.sampleMaximum(logits))
    }

    @Test
    fun sampleMaximum_tiesPickTheLowestIndex() {
        val decoder = CtcDecoder(vocabSize = 3)
        // 第 1 帧是并列最大（下标 3 与 4）→ `maxByOrNull` 取第一个，即 token 0
        assertArrayEquals(
            intArrayOf(0, 0, 2),
            decoder.sampleMaximum(floatArrayOf(0.5f, 0.1f, 0.1f, 0.5f, 0.5f, 0.1f, 0.1f, 0.2f, 0.9f)),
        )
    }

    @Test
    fun collapseDuplicates_removesConsecutiveDuplicatesAndYieldsEos() {
        val decoder = CtcDecoder(vocabSize = 4)
        val output = decoder.collapseDuplicates(intArrayOf(1, 1, 2, 2, 2, 3, 1)).toList()
        assertArrayEquals(
            intArrayOf(1, 2, 3, 1, SpeechRecognizer.END_OF_SEQUENCE),
            output.map { it.first }.toIntArray(),
        )
    }

    @Test
    fun collapseDuplicates_timestampsAreFirstOccurrence() {
        val decoder = CtcDecoder(vocabSize = 4)
        val output = decoder.collapseDuplicates(intArrayOf(1, 2, 2, 3)).toList()
        // 1 在第 0 帧，2 在第 1 帧，3 在第 3 帧；EoS 的时间戳是总帧数
        assertArrayEquals(intArrayOf(0, 1, 3, 4), output.map { it.second }.toIntArray())
    }

    // ─────────────────── LiteRtRunner：输入长度对齐 ───────────────────

    @Test
    fun padOrTruncateFeatures_exactSize_returnsSameArray() {
        val features = floatArrayOf(1.0f, 2.0f, 3.0f)
        val result = LiteRtRunner.padOrTruncateFeatures(features, 3)
        assertTrue(features === result)
    }

    @Test
    fun padOrTruncateFeatures_needsPadding_postPadsWithZeros() {
        val result = LiteRtRunner.padOrTruncateFeatures(floatArrayOf(1.0f, 2.0f, 3.0f), 5)
        assertArrayEquals(floatArrayOf(1.0f, 2.0f, 3.0f, 0.0f, 0.0f), result, 0.0f)
    }

    @Test
    fun padOrTruncateFeatures_needsTruncation_keepsTail() {
        // 留下最近的音频（尾部），丢掉最早的数据 —— 官方注释的取舍
        val result = LiteRtRunner.padOrTruncateFeatures(floatArrayOf(1.0f, 2.0f, 3.0f, 4.0f, 5.0f), 3)
        assertArrayEquals(floatArrayOf(3.0f, 4.0f, 5.0f), result, 0.0f)
    }

    // ─────────────────── TensorType.numElements ───────────────────

    @Test
    fun numElements_scalar_returnsOne() {
        val tensorType = TensorType(TensorType.ElementType.FLOAT, TensorType.Layout(emptyList()))
        assertEquals(1, tensorType.numElements)
    }

    @Test
    fun numElements_oneDimension_returnsDimensionSize() {
        val tensorType = TensorType(TensorType.ElementType.INT, TensorType.Layout(listOf(5)))
        assertEquals(5, tensorType.numElements)
    }

    @Test
    fun numElements_multipleDimensions_returnsProductOfDimensions() {
        val tensorType = TensorType(TensorType.ElementType.FLOAT, TensorType.Layout(listOf(2, 3, 4)))
        assertEquals(24, tensorType.numElements)
    }

    @Test
    fun numElements_zeroDimension_returnsZero() {
        val tensorType = TensorType(TensorType.ElementType.FLOAT, TensorType.Layout(listOf(2, 0, 4)))
        assertEquals(0, tensorType.numElements)
    }
}
