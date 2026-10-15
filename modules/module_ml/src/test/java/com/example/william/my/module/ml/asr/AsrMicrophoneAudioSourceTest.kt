package com.example.william.my.module.ml.asr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * [MicrophoneAudioSource] 的滑动窗口 + 静音门限 + 错误码处理测试
 *
 * 移植官方 `MicrophoneAudioSourceTest`，靠注入 [AudioReader] 把这套窗口逻辑拉进纯 JVM 覆盖 ——
 * 不需要真麦克风，也不需要 Robolectric。三条被钉死的行为：
 * 1. 每步读 `interval - overlap` 个**新**采样，左移后产出完整一块；
 * 2. 每步先算 RMS，低于 0.005 就整块写 0（抑制底噪被识别成幻听文本）；
 * 3. `ERROR_DEAD_OBJECT` / `ERROR_INVALID_OPERATION` 结束序列，其余负值当瞬时错误跳过重试。
 */
class AsrMicrophoneAudioSourceTest {

    /** 可精确控制每次 `read` 返回值的假读取器 */
    class MockAudioReader(private val responses: List<ReadResponse>) : AudioReader {

        private var responseIndex = 0

        override fun close() = Unit

        override fun read(audioData: ShortArray, offsetInShorts: Int, sizeInShorts: Int): Int {
            if (responseIndex >= responses.size) return 0
            val response = responses[responseIndex++]
            if (response.result > 0 && response.data != null) {
                val toCopy = minOf(response.result, response.data.size, sizeInShorts)
                System.arraycopy(response.data, 0, audioData, offsetInShorts, toCopy)
                return toCopy
            }
            return response.result
        }
    }

    data class ReadResponse(val result: Int, val data: ShortArray? = null)

    @Test
    fun testGetAudioData_yieldsCorrectChunks() {
        val stepSamples = 8000
        val responses =
            listOf(
                ReadResponse(stepSamples, ShortArray(stepSamples) { 1000 }),
                ReadResponse(stepSamples, ShortArray(stepSamples) { 2000 }),
                ReadResponse(stepSamples, ShortArray(stepSamples) { 3000 }),
                ReadResponse(0),
            )

        val results = createSource(responses, overlap = 0.5.seconds).getAudioData().map { it.clone() }.toList()

        assertEquals(3, results.size)
        assertEquals(24000, results[0].size)
        assertTrue(results[0].take(16000).all { it == 0f })
        assertTrue(results[0].takeLast(8000).all { it == (1000f / 32768f) })

        assertEquals(24000, results[1].size)
        assertTrue(results[1].take(8000).all { it == 0f })
        assertTrue(results[1].slice(8000 until 16000).all { it == (1000f / 32768f) })
        assertTrue(results[1].takeLast(8000).all { it == (2000f / 32768f) })

        assertEquals(24000, results[2].size)
        assertTrue(results[2].slice(0 until 8000).all { it == (1000f / 32768f) })
        assertTrue(results[2].slice(8000 until 16000).all { it == (2000f / 32768f) })
        assertTrue(results[2].takeLast(8000).all { it == (3000f / 32768f) })
    }

    @Test
    fun testSilenceDetection_zerosOutSilentChunks() {
        val stepSamples = 16000
        val responses =
            listOf(
                ReadResponse(stepSamples, ShortArray(stepSamples) { 1000 }), // 有声音
                ReadResponse(stepSamples, ShortArray(stepSamples) { 10 }), // 低于门限
                ReadResponse(0),
            )

        val results = createSource(responses, overlap = 0.seconds).getAudioData().map { it.clone() }.toList()

        assertEquals(2, results.size)
        assertEquals(32000, results[0].size)
        assertTrue(results[0].take(16000).all { it == 0f })
        assertTrue(results[0].takeLast(16000).all { it == (1000f / 32768f) })

        assertEquals(32000, results[1].size)
        assertTrue(results[1].take(16000).all { it == (1000f / 32768f) })
        assertTrue(results[1].takeLast(16000).all { it == 0f })
    }

    @Test
    fun testError_deadObject_terminatesSequence() {
        val stepSamples = 8000
        val responses =
            listOf(
                ReadResponse(stepSamples, ShortArray(stepSamples) { 1000 }),
                ReadResponse(-6), // AudioRecord.ERROR_DEAD_OBJECT
            )

        assertEquals(1, createSource(responses).getAudioData().toList().size)
    }

    @Test
    fun testError_invalidOperation_terminatesSequence() {
        val responses = listOf(ReadResponse(-3)) // AudioRecord.ERROR_INVALID_OPERATION
        assertTrue(createSource(responses).getAudioData().toList().isEmpty())
    }

    @Test
    fun testError_genericNegative_continues() {
        val stepSamples = 8000
        val responses =
            listOf(
                ReadResponse(-1),
                ReadResponse(stepSamples, ShortArray(stepSamples) { 5000 }),
                ReadResponse(0),
            )

        val results = createSource(responses).getAudioData().map { it.clone() }.toList()

        assertEquals(1, results.size)
        assertEquals(24000, results[0].size)
        assertTrue(results[0].take(16000).all { it == 0f })
        assertTrue(results[0].takeLast(8000).all { it == (5000f / 32768f) })
    }

    @Test
    fun testError_badValueTransient_continuesAndRecovers() {
        val stepSamples = 8000
        val responses =
            listOf(
                ReadResponse(-2), // AudioRecord.ERROR_BAD_VALUE
                ReadResponse(stepSamples, ShortArray(stepSamples) { 4000 }),
                ReadResponse(0),
            )

        val results = createSource(responses).getAudioData().map { it.clone() }.toList()

        assertEquals(1, results.size)
        assertEquals(24000, results[0].size)
        assertTrue(results[0].take(16000).all { it == 0f })
        assertTrue(results[0].takeLast(8000).all { it == (4000f / 32768f) })
    }

    @Test
    fun testPartialReads_accumulateBeforeYield() {
        val stepSamples = 8000
        val responses =
            listOf(
                ReadResponse(4000, ShortArray(4000) { 1000 }), // 半格
                ReadResponse(4000, ShortArray(4000) { 2000 }), // 补足
                ReadResponse(0),
            )

        val results =
            createSource(responses, interval = 0.5.seconds, overlap = 0.seconds)
                .getAudioData()
                .map { it.clone() }
                .toList()

        assertEquals(1, results.size)
        assertEquals(16000, results[0].size)
        assertTrue(results[0].take(8000).all { it == 0f })
        assertTrue(results[0].slice(8000 until 12000).all { it == (1000f / 32768f) })
        assertTrue(results[0].slice(12000 until 16000).all { it == (2000f / 32768f) })
    }

    @Test
    fun testIntervalNotMultipleOfStep_accumulatesCorrectly() {
        val interval = 1.2.seconds // 19200 采样
        val overlap = 0.5.seconds // 8000 采样
        val stepSamples = 11200

        val responses =
            listOf(
                ReadResponse(stepSamples, ShortArray(stepSamples) { 1000 }),
                ReadResponse(stepSamples, ShortArray(stepSamples) { 2000 }),
                ReadResponse(stepSamples, ShortArray(stepSamples) { 3000 }),
                ReadResponse(0),
            )

        val results = createSource(responses, interval = interval, overlap = overlap).getAudioData().map { it.clone() }.toList()

        assertEquals(3, results.size)
        assertEquals(30400, results[0].size)
        assertTrue(results[0].take(19200).all { it == 0f })
        assertTrue(results[0].takeLast(11200).all { it == (1000f / 32768f) })

        assertEquals(30400, results[1].size)
        assertTrue(results[1].take(8000).all { it == 0f })
        assertTrue(results[1].slice(8000 until 19200).all { it == (1000f / 32768f) })
        assertTrue(results[1].takeLast(11200).all { it == (2000f / 32768f) })

        assertEquals(30400, results[2].size)
        assertTrue(results[2].slice(0 until 8000).all { it == (1000f / 32768f) })
        assertTrue(results[2].slice(8000 until 19200).all { it == (2000f / 32768f) })
        assertTrue(results[2].slice(19200 until 30400).all { it == (3000f / 32768f) })
    }

    @Test
    fun testClose_succeedsWithoutException() {
        createSource(emptyList()).close()
    }

    @Test
    fun testInit_invalidOverlap_throwsException() {
        val exception =
            runCatching { createSource(emptyList(), interval = 1.seconds, overlap = 1.seconds) }
                .exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
    }

    @Test
    fun testInit_audioRecordUnavailable_propagatesFailure() {
        val source =
            MicrophoneAudioSource(
                samplingRate = 16000,
                interval = 1.seconds,
                overlap = 0.5.seconds,
                getAudioReader = { throw IllegalStateException("AudioRecord 初始化失败") },
            )
        val exception = runCatching { source.getAudioData().toList() }.exceptionOrNull()
        assertTrue(exception is IllegalStateException)
    }

    private fun createSource(
        responses: List<ReadResponse>,
        samplingRate: Int = 16000,
        interval: kotlin.time.Duration = 1.seconds,
        overlap: kotlin.time.Duration = 0.5.seconds,
    ) = MicrophoneAudioSource(
        samplingRate = samplingRate,
        interval = interval,
        overlap = overlap,
        getAudioReader = { _ -> MockAudioReader(responses) },
    )
}
