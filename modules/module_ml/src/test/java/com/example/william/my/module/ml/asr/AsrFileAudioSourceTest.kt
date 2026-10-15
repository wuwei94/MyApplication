package com.example.william.my.module.ml.asr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * [FileAudioSource] 的 WAV 解析与重叠切窗测试
 *
 * 移植官方 `FileAudioSourceTest`。三条被钉死的行为：
 * 1. 首块读满 `interval`，其后每块只读 `interval - overlap` 个**新**采样再左移拼接；
 * 2. 读不满时**尾部补 0**（当静音），因此最后一块可能比 interval 短；
 * 3. 坏文件必须抛出可读的异常，而不是 `ArrayIndexOutOfBoundsException`。
 *
 * ⚠️ 第 2 条要**同时**覆盖「首块短」与「非首块短」两种情形：首块的 `bufferForSamples` 是刚
 * `FloatArray(n)` 出来的（本来就是全 0），补 0 是空操作，**删掉它也测不出来**；只有非首块短时，
 * 缓冲区尾部残留的是上一块左移前的旧采样，此时补 0 才真正起作用。（这是变异验证查出来的缺口。）
 *
 * 与官方测试的差异只在**异常文案**：本模块的报错是中文（会直接显示在页面的 Toast 里）。
 */
class AsrFileAudioSourceTest {

    @Test
    fun getAudioData_parsesWavCorrectly() {
        val sampleRate = 16000
        // 0.1 秒 = 1600 个采样
        val samples = ShortArray(1600) { i -> i.toShort() }
        val inputStream = ByteArrayInputStream(createWavBytes(samples, sampleRate))

        val audioSource =
            FileAudioSource(
                inputStream = inputStream,
                samplingRate = sampleRate,
                interval = 100.milliseconds,
                overlap = 0.milliseconds,
            )

        val audioData = audioSource.getAudioData().map { it.clone() }.toList()

        assertEquals(1, audioData.size)
        assertEquals(1600, audioData[0].size)
        assertEquals((0f / 32768f).toDouble(), audioData[0][0].toDouble(), 1e-6)
        assertEquals((1599f / 32768f).toDouble(), audioData[0][1599].toDouble(), 1e-6)
    }

    @Test
    fun getAudioData_handlesOverlap() {
        val sampleRate = 16000
        // 0.2 秒 = 3200 采样；interval 0.1s(1600) / overlap 0.05s(800) → 步长 800
        val samples = ShortArray(3200) { i -> i.toShort() }
        val inputStream = ByteArrayInputStream(createWavBytes(samples, sampleRate))

        val audioSource =
            FileAudioSource(
                inputStream = inputStream,
                samplingRate = sampleRate,
                interval = 100.milliseconds,
                overlap = 50.milliseconds,
            )

        val audioData = audioSource.getAudioData().map { it.clone() }.toList()

        assertEquals(3, audioData.size)

        // 块 1: [0, 1600)
        assertEquals((0f / 32768f).toDouble(), audioData[0][0].toDouble(), 1e-6)
        assertEquals((1599f / 32768f).toDouble(), audioData[0][1599].toDouble(), 1e-6)

        // 块 2: [800, 2400)
        assertEquals((800f / 32768f).toDouble(), audioData[1][0].toDouble(), 1e-6)
        assertEquals((2399f / 32768f).toDouble(), audioData[1][1599].toDouble(), 1e-6)

        // 块 3: [1600, 3200)
        assertEquals((1600f / 32768f).toDouble(), audioData[2][0].toDouble(), 1e-6)
        assertEquals((3199f / 32768f).toDouble(), audioData[2][1599].toDouble(), 1e-6)
    }

    @Test
    fun getAudioData_shortAudio_returnsWhatItCan() {
        val sampleRate = 16000
        // 只有 0.05 秒 = 800 采样，但 interval 是 1600
        val samples = ShortArray(800) { i -> i.toShort() }
        val inputStream = ByteArrayInputStream(createWavBytes(samples, sampleRate))

        val audioSource =
            FileAudioSource(
                inputStream = inputStream,
                samplingRate = sampleRate,
                interval = 100.milliseconds,
                overlap = 0.milliseconds,
            )

        val audioData = audioSource.getAudioData().map { it.clone() }.toList()

        // 仍然产出定长一块，多出来的槽位补 0
        assertEquals(1, audioData.size)
        assertEquals(1600, audioData[0].size)
        assertEquals((0f / 32768f).toDouble(), audioData[0][0].toDouble(), 1e-6)
        assertEquals((499f / 32768f).toDouble(), audioData[0][499].toDouble(), 1e-6)
        assertEquals((799f / 32768f).toDouble(), audioData[0][799].toDouble(), 1e-6)
        assertTrue(audioData[0].slice(800 until 1600).all { it == 0f })
    }

    /**
     * **非首块**读不满时，尾部必须补 0，不能残留上一块左移前的旧采样
     *
     * 这是上面「首块短」用例**测不到**的一条：首块的缓冲区是刚 `FloatArray(n)` 出来的、本来就是
     * 全 0，补 0 是空操作；只有走到第二块及以后，尾部残留的才是上一块的旧数据（漏补 0 会把上一块
     * 的音频当成静音尾重复一遍）。构造：1900 个采样 / interval 1600 / overlap 800 → 步长 800，
     * 于是第 2 块只读到 300 个新采样，尾部 [1100, 1600) 必须为 0。
     */
    @Test
    fun getAudioData_shortNonFirstChunk_zeroesStaleTail() {
        val sampleRate = 16000
        val samples = ShortArray(1900) { i -> i.toShort() }
        val inputStream = ByteArrayInputStream(createWavBytes(samples, sampleRate))

        val audioSource =
            FileAudioSource(
                inputStream = inputStream,
                samplingRate = sampleRate,
                interval = 100.milliseconds,
                overlap = 50.milliseconds,
            )

        val audioData = audioSource.getAudioData().map { it.clone() }.toList()

        assertEquals(2, audioData.size)
        assertEquals(1600, audioData[1].size)
        // 前 800 个是上一块的尾巴（左移保留的重叠区）
        assertEquals((800f / 32768f).toDouble(), audioData[1][0].toDouble(), 1e-6)
        assertEquals((1599f / 32768f).toDouble(), audioData[1][799].toDouble(), 1e-6)
        // 300 个新采样
        assertEquals((1600f / 32768f).toDouble(), audioData[1][800].toDouble(), 1e-6)
        assertEquals((1899f / 32768f).toDouble(), audioData[1][1099].toDouble(), 1e-6)
        // 尾部 500 个槽位必须是静音，而不是上一块残留的 1100..1599
        assertTrue(audioData[1].slice(1100 until 1600).all { it == 0f })
    }

    @Test
    fun constructor_throwsOnInvalidIntervalOverlap() {
        val exception =
            runCatching {
                FileAudioSource(
                    inputStream = ByteArrayInputStream(ByteArray(0)),
                    samplingRate = 16000,
                    interval = 1.seconds,
                    overlap = 1.seconds,
                )
            }.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
    }

    @Test
    fun getAudioData_throwsOnInvalidHeader() {
        val audioSource =
            FileAudioSource(
                inputStream = ByteArrayInputStream("NOTAWAVFILE".toByteArray()),
                samplingRate = 16000,
                interval = 1.seconds,
                overlap = 0.seconds,
            )

        val exception = runCatching { audioSource.getAudioData().toList() }.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertEquals("不是合法 WAV：文件过短", exception?.message)
    }

    @Test
    fun getAudioData_throwsOnMismatchedSampleRate() {
        val inputStream = ByteArrayInputStream(createWavBytes(ShortArray(1600), sampleRate = 44100))
        val audioSource =
            FileAudioSource(
                inputStream = inputStream,
                samplingRate = 16000,
                interval = 1.seconds,
                overlap = 0.seconds,
            )

        val exception = runCatching { audioSource.getAudioData().toList() }.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertEquals("采样率不符：要求 16000，文件为 44100", exception?.message)
    }

    /**
     * `data` 块声明的长度大于实际数据时**只取实到的部分**并补 0，而不是抛异常
     *
     * 本模块相对官方多了一道钳位（官方在这种情况下会抛 `ArrayIndexOutOfBoundsException`，
     * 一个被截断的 WAV 足以把页面打崩）。这里让头里声明 1600 个采样、实际只给 800 个。
     */
    @Test
    fun getAudioData_truncatedDataChunk_isClampedNotCrashing() {
        val sampleRate = 16000
        val wav = createWavBytes(ShortArray(800) { 1000 }, sampleRate)
        // 把 data 块声明的长度改大成 1600 个采样（3200 字节），实际数据仍只有 1600 字节
        val dataSizeIndex = 40
        ByteBuffer.wrap(wav, dataSizeIndex, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(1600 * 2)

        val audioSource =
            FileAudioSource(
                inputStream = ByteArrayInputStream(wav),
                samplingRate = sampleRate,
                interval = 100.milliseconds,
                overlap = 0.milliseconds,
            )

        val audioData = audioSource.getAudioData().map { it.clone() }.toList()

        assertEquals(1, audioData.size)
        assertEquals(1600, audioData[0].size)
        assertEquals((1000f / 32768f).toDouble(), audioData[0][0].toDouble(), 1e-6)
        assertEquals((1000f / 32768f).toDouble(), audioData[0][799].toDouble(), 1e-6)
        assertTrue(audioData[0].slice(800 until 1600).all { it == 0f })
    }

    /** 造一个合法的 16-bit 单声道 PCM WAV（44 字节头 + 裸数据） */
    private fun createWavBytes(samples: ShortArray, sampleRate: Int = 16000): ByteArray {
        val dataSize = samples.size * 2
        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray())
        buffer.putInt(36 + dataSize)
        buffer.put("WAVE".toByteArray())

        buffer.put("fmt ".toByteArray())
        buffer.putInt(16)
        buffer.putShort(1.toShort()) // PCM
        buffer.putShort(1.toShort()) // Mono
        buffer.putInt(sampleRate)
        buffer.putInt(sampleRate * 2) // Byte rate
        buffer.putShort(2.toShort()) // Block align
        buffer.putShort(16.toShort()) // Bits per sample

        buffer.put("data".toByteArray())
        buffer.putInt(dataSize)
        for (sample in samples) {
            buffer.putShort(sample)
        }

        return buffer.array()
    }
}
