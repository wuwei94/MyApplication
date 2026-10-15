package com.example.william.my.module.ml.asr

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin

/**
 * **librosa 兼容的 log-Mel 频谱**。
 *
 * 官方 ASR 样例这一段调的是 [jlibrosa](https://github.com/Subtitle-Synchronizer/jlibrosa) 的
 * `generateMelSpectroGram`（`MelSpectroProcessor.defaultGenerateMelSpectrogram`），
 * 依赖一个**仓库外**的 `jlibrosa-…-jar-with-dependencies.jar`，由 Gradle 在**构建期**
 * 从 GitHub 拉取。本模块**刻意不复刻这个依赖**，原因是它不可复现：
 * 本地构建会因拿不到该 JAR 而直接失败，且「构建期联网下载二进制」本身也不是一个可长期成立的约定。
 *
 * 因此这里按 **librosa 的语义**重写，而不是按 jlibrosa 的实现逐行翻译：
 * jlibrosa 自己的设计目标就是 mirror librosa，官方注释里也点明
 * 「JLibrosa's generateMelSpectroGram uses librosa's default parameters. In librosa, the default
 * normalization for mel filters is 'slaney'」。等价于
 * `librosa.feature.melspectrogram(y, sr, n_fft, hop_length, window="hann", center=True,
 * pad_mode="constant", power=2.0, n_mels, fmin=0, fmax=sr/2, htk=False, norm="slaney")`，
 * 再逐元素取 `ln(x + 2⁻²⁴)`。
 *
 * 数值正确性由单测用**真 librosa 生成的 golden 向量**逐元素校验（见 `AsrMelTest`），
 * 不是「看起来差不多」。
 *
 * 需要留意的两处上游取舍（都沿用官方，不自行改动）：
 * 1. 官方把 `nFFT` **向上取到 2 的幂**（whisper 的 400 → 512）后才做 FFT —— 本模块保持一致，
 *    所以这里要求 [nFft] 已是 2 的幂，由调用方经 [nextPowerOfTwo] 折算。
 * 2. 噪声底 `2⁻²⁴` 是官方常量，不是 librosa 的默认（librosa 用 `amin=1e-10`）。
 */
internal object LogMel {

    /** `2⁻²⁴`：官方 `MelSpectroProcessor.LOG_ZERO_GUARD_VALUE`，防止 `ln(0)`。 */
    val LOG_ZERO_GUARD: Float = 2.0f.pow(-24)

    /**
     * 计算 log-Mel 频谱。
     *
     * @param waveform 已做预加重的单声道波形
     * @param sampleRate 采样率（官方固定 16 kHz）
     * @param nFft 已经是 2 的幂的 FFT 长度
     * @param nMels mel 滤波器组数量
     * @param hopLength 帧移
     * @return `[nMels][frames]`，值为 `ln(mel + 2⁻²⁴)`
     */
    fun spectrogram(
        waveform: FloatArray,
        sampleRate: Int,
        nFft: Int,
        nMels: Int,
        hopLength: Int,
    ): Array<FloatArray> {
        require(nFft > 0 && nFft and (nFft - 1) == 0) { "nFft 必须是 2 的幂：$nFft" }
        require(hopLength > 0) { "hopLength 必须为正：$hopLength" }

        val frames = frameCount(waveform.size, nFft, hopLength)
        val power = powerSpectrogram(waveform, nFft, hopLength, frames)
        val filters = melFilterBank(sampleRate, nFft, nMels)

        return Array(nMels) { m ->
            FloatArray(frames) { f ->
                var sum = 0.0f
                val row = filters[m]
                for (k in row.indices) {
                    val weight = row[k]
                    if (weight != 0.0f) sum += weight * power[k][f]
                }
                ln(sum + LOG_ZERO_GUARD)
            }
        }
    }

    /**
     * `center=True` 时的帧数：`1 + len(y) / hop`
     *
     * 两侧各补 `nFft / 2` 个零（librosa 1.x 的 `pad_mode="constant"` 默认值）。
     */
    fun frameCount(sampleCount: Int, nFft: Int, hopLength: Int): Int {
        if (sampleCount <= 0) return 0
        return 1 + sampleCount / hopLength
    }

    /** 周期 Hann 窗（`scipy.signal.get_window("hann", n, fftbins=True)` 的等价形式） */
    fun hannWindow(n: Int): FloatArray = FloatArray(n) { i -> (0.5 - 0.5 * cos(2.0 * PI * i / n)).toFloat() }

    /** `|STFT|²`，形状 `[nFft/2 + 1][frames]` */
    private fun powerSpectrogram(
        waveform: FloatArray,
        nFft: Int,
        hopLength: Int,
        frames: Int,
    ): Array<FloatArray> {
        val bins = nFft / 2 + 1
        val window = hannWindow(nFft)
        val pad = nFft / 2
        val re = FloatArray(nFft)
        val im = FloatArray(nFft)
        val result = Array(bins) { FloatArray(frames) }

        for (f in 0 until frames) {
            val start = f * hopLength - pad
            for (i in 0 until nFft) {
                val index = start + i
                re[i] = if (index in waveform.indices) waveform[index] * window[i] else 0.0f
                im[i] = 0.0f
            }
            fft(re, im)
            for (k in 0 until bins) {
                val power = re[k] * re[k] + im[k] * im[k]
                result[k][f] = if (power.isNaN()) 0.0f else power
            }
        }
        return result
    }

    /**
     * slaney 归一化的 mel 滤波器组，形状 `[nMels][nFft/2 + 1]`
     *
     * 对应 `librosa.filters.mel(sr, n_fft, n_mels, fmin=0, fmax=sr/2, htk=False, norm="slaney")`。
     */
    fun melFilterBank(
        sampleRate: Int,
        nFft: Int,
        nMels: Int,
        fMin: Float = 0.0f,
        fMax: Float = sampleRate / 2.0f,
    ): Array<FloatArray> {
        val bins = nFft / 2 + 1
        // librosa: np.linspace(0, sr / 2, 1 + n_fft // 2)
        val fftFreqs = FloatArray(bins) { it * (sampleRate / 2.0f) / (bins - 1) }

        val melMin = hzToMel(fMin)
        val melMax = hzToMel(fMax)
        val melF = FloatArray(nMels + 2) { i ->
            melToHz(melMin + (melMax - melMin) * i / (nMels + 1))
        }
        val fDiff = FloatArray(nMels + 1) { melF[it + 1] - melF[it] }

        return Array(nMels) { m ->
            val lowerDiff = fDiff[m]
            val upperDiff = fDiff[m + 1]
            val enorm = 2.0f / (melF[m + 2] - melF[m])
            FloatArray(bins) { k ->
                // librosa: lower = -ramps[i] / fdiff[i]，ramps[i] = mel_f[i] - fft_freqs
                //          upper = ramps[i+2] / fdiff[i+1]
                val lower = (fftFreqs[k] - melF[m]) / lowerDiff
                val upper = (melF[m + 2] - fftFreqs[k]) / upperDiff
                val triangle = minOf(lower, upper).coerceAtLeast(0.0f)
                triangle * enorm
            }
        }
    }

    /** `librosa.hz_to_mel(freq, htk=False)`（Slaney 公式） */
    fun hzToMel(freq: Float): Float = if (freq >= MIN_LOG_HZ) MIN_LOG_MEL + ln(freq / MIN_LOG_HZ) / LOG_STEP else (freq - F_MIN) / F_SP

    /** `librosa.mel_to_hz(mel, htk=False)` */
    fun melToHz(mel: Float): Float = if (mel >= MIN_LOG_MEL) MIN_LOG_HZ * exp(LOG_STEP * (mel - MIN_LOG_MEL)) else F_MIN + F_SP * mel

    /** 向上取到 2 的幂（官方 `getSmallestPowerOfTwoGreaterOrEqualTo`，n 已是 2 的幂时取自身） */
    fun nextPowerOfTwo(n: Int): Int {
        if (n > 0 && n and (n - 1) == 0) return n
        var p = 1
        while (p < n) p = p shl 1
        return p
    }

    /** 原地 radix-2 复数 FFT（[re] / [im] 等长且为 2 的幂） */
    fun fft(re: FloatArray, im: FloatArray) {
        val n = re.size
        require(n == im.size) { "实部与虚部长度必须一致" }
        require(n > 0 && n and (n - 1) == 0) { "FFT 长度必须是 2 的幂：$n" }

        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j or bit
            if (i < j) {
                val tr = re[i]
                re[i] = re[j]
                re[j] = tr
                val ti = im[i]
                im[i] = im[j]
                im[j] = ti
            }
        }

        var length = 2
        while (length <= n) {
            val half = length / 2
            // 预计算本级的旋转因子，避免逐次复乘累积误差
            val cosTable = FloatArray(half) { k -> cos(-2.0 * PI * k / length).toFloat() }
            val sinTable = FloatArray(half) { k -> sin(-2.0 * PI * k / length).toFloat() }
            var blockStart = 0
            while (blockStart < n) {
                for (k in 0 until half) {
                    val evenIndex = blockStart + k
                    val oddIndex = evenIndex + half
                    val wr = cosTable[k]
                    val wi = sinTable[k]
                    val oddRe = re[oddIndex]
                    val oddIm = im[oddIndex]
                    val tr = oddRe * wr - oddIm * wi
                    val ti = oddRe * wi + oddIm * wr
                    re[oddIndex] = re[evenIndex] - tr
                    im[oddIndex] = im[evenIndex] - ti
                    re[evenIndex] += tr
                    im[evenIndex] += ti
                }
                blockStart += length
            }
            length = length shl 1
        }
    }

    private const val F_MIN = 0.0f
    private const val F_SP = 200.0f / 3.0f
    private const val MIN_LOG_HZ = 1000.0f
    private val MIN_LOG_MEL = (MIN_LOG_HZ - F_MIN) / F_SP
    private val LOG_STEP = ln(6.4f) / 27.0f
}
