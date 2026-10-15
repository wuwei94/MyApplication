package com.example.william.my.module.ml.asr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.BufferedReader
import kotlin.math.abs

/**
 * [LogMel] 的数值正确性。
 *
 * 这里对比的**不是**自己写的参考实现，而是**真 librosa 生成的 golden 向量**
 * （`src/test/resources/asr/mel_golden_*.txt`，由 `librosa.feature.melspectrogram` 产出，
 * 生成脚本见 `.workbuddy/tmp/gen_mel_golden.py`）。这样「mel 换成自己的实现」这件事
 * 是被外部权威口径验证过的，而不是自说自话。
 */
class AsrMelTest {

    @Test
    fun logMel_matchesLibrosa_80Mel() = verifyAgainstLibrosa(80)

    @Test
    fun logMel_matchesLibrosa_128Mel() = verifyAgainstLibrosa(128)

    @Test
    fun frameCount_centerTrueFormula() {
        assertEquals(11, LogMel.frameCount(1600, 512, 160))
        assertEquals(3001, LogMel.frameCount(480_000, 512, 160))
        assertEquals(0, LogMel.frameCount(0, 512, 160))
    }

    @Test
    fun nfft_roundedUpToPowerOfTwo() {
        // 400 不是 2 的幂 → 512（whisper 的配置就吃这条）；512 已是 → 自身
        assertEquals(512, LogMel.nextPowerOfTwo(400))
        assertEquals(512, LogMel.nextPowerOfTwo(512))
        assertEquals(1, LogMel.nextPowerOfTwo(1))
        assertEquals(1024, LogMel.nextPowerOfTwo(513))
    }

    @Test
    fun melFrequency_roundTrip() {
        for (freq in intArrayOf(0, 100, 500, 1000, 2000, 4000, 8000)) {
            val back = LogMel.melToHz(LogMel.hzToMel(freq.toFloat()))
            assertTrue("freq=$freq 往返偏差过大：$back", abs(back - freq) < 0.05f)
        }
        // Slaney 口径的两个锚点：1000 Hz 恰好落在 minLogMel=15（对数段起点），
        // 而 1000 Hz 以下走线性段（f_sp = 200/3）。
        assertEquals(15.0f, LogMel.hzToMel(1000.0f), 1e-4f)
        assertEquals(600.0f / (200.0f / 3.0f), LogMel.hzToMel(600.0f), 1e-3f)
        assertTrue("1200 Hz 属于对数段，mel 值必须大于 15：${LogMel.hzToMel(1200.0f)}", LogMel.hzToMel(1200.0f) > 15.0f)
    }

    @Test
    fun hannWindow_periodicSymmetric() {
        val window = LogMel.hannWindow(512)
        assertEquals(0.0f, window[0], 1e-6f)
        // 周期性 Hann 的首尾不相等：w[n] = 0.5 - 0.5cos(2πn/N)，n=N-1 时并不回到 0
        assertTrue("周期窗末点不应为 0：${window[511]}", window[511] > 0.0f)
        for (i in 1 until window.size / 2) {
            assertEquals("第 $i 点不对称", window[i], window[window.size - i], 1e-6f)
        }
    }

    @Test
    fun fft_dcInputOnlyBin0() {
        val n = 512
        val re = FloatArray(n) { 1.0f }
        val im = FloatArray(n)
        LogMel.fft(re, im)
        assertEquals(n.toFloat(), re[0], 1e-2f)
        assertEquals(0.0f, im[0], 1e-2f)
        for (k in 1 until n) {
            assertTrue("频点 $k 应为 0：${re[k]}/${im[k]}", abs(re[k]) < 1e-2f && abs(im[k]) < 1e-2f)
        }
    }

    @Test
    fun fft_integerCycleSineAtItsBin() {
        val n = 512
        val bin = 8
        val re = FloatArray(n) { kotlin.math.sin(2.0 * Math.PI * bin * it / n).toFloat() }
        val im = FloatArray(n)
        LogMel.fft(re, im)
        val magnitudeAtBin = kotlin.math.sqrt(re[bin] * re[bin] + im[bin] * im[bin])
        assertEquals(n / 2.0f, magnitudeAtBin, 1.0f)
        for (k in 0 until n) {
            if (k == bin || k == n - bin) continue
            val magnitude = kotlin.math.sqrt(re[k] * re[k] + im[k] * im[k])
            assertTrue("频点 $k 不应有能量：$magnitude", magnitude < 1.0f)
        }
    }

    @Test
    fun melFilterbank_nonNegativeAndLowFreqWeight() {
        val filters = LogMel.melFilterBank(SAMPLE_RATE, 512, 80)
        assertEquals(80, filters.size)
        assertEquals(512 / 2 + 1, filters[0].size)
        for ((row, weights) in filters.withIndex()) {
            assertTrue("第 $row 行出现负权重", weights.all { it >= 0.0f })
            assertTrue("第 $row 行全零", weights.any { it > 0.0f })
        }
        // slaney 归一化下低频滤波器峰值更大（enorm = 2/(f[i+2]-f[i]) 随频率递减）
        val lowPeak = filters[0].max()
        val highPeak = filters[79].max()
        assertTrue("低频峰值 $lowPeak 应大于高频峰值 $highPeak", lowPeak > highPeak)
    }

    private fun verifyAgainstLibrosa(nMels: Int) {
        val signal = FloatArray(SAMPLES) { i -> ((i % 97) - 48) / 64.0f }
        val expected = loadGolden("/asr/mel_golden_512_160_$nMels.txt")
        val actual = LogMel.spectrogram(signal, SAMPLE_RATE, 512, nMels, HOP)

        assertEquals("mel 行数", nMels, actual.size)
        assertEquals("帧数", expected[0].size, actual[0].size)

        var worst = 0.0f
        var worstAt = ""
        for (m in 0 until nMels) {
            assertEquals("第 $m 行帧数", expected[m].size, actual[m].size)
            for (f in actual[m].indices) {
                val diff = abs(actual[m][f] - expected[m][f])
                if (diff > worst) {
                    worst = diff
                    worstAt = "mel=$m frame=$f 实际=${actual[m][f]} 参考=${expected[m][f]}"
                }
            }
        }
        assertTrue("与 librosa 的最大绝对偏差 $worst 超过容差 $TOLERANCE（$worstAt）", worst < TOLERANCE)
    }

    private fun loadGolden(resource: String): Array<FloatArray> {
        val stream = checkNotNull(javaClass.getResourceAsStream(resource)) { "缺少 golden 资源：$resource" }
        return stream.bufferedReader().use { reader: BufferedReader ->
            reader.readLines()
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .map { line -> line.trim().split(" ").map { it.toFloat() }.toFloatArray() }
                .toTypedArray()
        }
    }

    private companion object {
        const val SAMPLE_RATE = 16_000
        const val HOP = 160
        const val SAMPLES = 1600

        /**
         * 容差取 1e-3（log 量级上的绝对偏差）。
         *
         * 实测最大偏差在 1e-5 量级 —— 余量给的是 float32 与 librosa 内部可能的
         * float64 中间计算差异。这个容差仍有鉴别力：窗函数、mel 滤波器组、
         * 是否 `center` 补零，任何一处写错都会造成 1e-1 以上的偏差。
         */
        const val TOLERANCE = 1e-3f
    }
}
