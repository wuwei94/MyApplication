package com.example.william.my.module.ml.asr

import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

/**
 * log-Mel 频谱配置（对应官方 `LogMelSpectroConfig`）。
 *
 * 官方用 `kotlinx.serialization` 反序列化这份配置；本模块**不为此新增序列化插件**，
 * 改由 [AsrModelCatalog] 用 `org.json` 解析 —— 配置只有一层，收益不值一个编译器插件。
 */
internal data class LogMelSpectroConfig(
    /** FFT 长度。**会被向上取到 2 的幂**（whisper 的 400 → 512），与官方一致。 */
    val nFFT: Int,
    val nMels: Int = 80,
    val hopLength: Int = 160,
    /** 输出帧数；`<= 0` 表示跟随实际帧数 */
    val nFrames: Int = 0,
    /** 是否转置成 `[frames][mels]`（Parakeet-CTC 需要） */
    val transpose: Boolean = false,
    /** 预加重系数；`<= 0` 表示不做 */
    val preemphasis: Float = 0f,
    /** `"whisper"` 走 whisper 的 log10→ln 缩放 + 裁剪归一化，其余走逐 mel 的 z-score */
    val normType: String = "standard",
)

/**
 * 官方 `MelSpectroProcessor` 的等价实现。
 *
 * 与官方唯一的功能性差异是 mel 计算本身：官方调 jlibrosa（构建期联网下载的外部 JAR），
 * 本模块改为 [LogMel]（librosa 语义、纯 Kotlin、用真 librosa 的 golden 向量校验过）。
 * **预处理链路本身逐行为对齐官方** —— 预加重 → 取 2 的幂 FFT → log-Mel →
 * 按 `normType` 归一化 → 按 `transpose` 打包成 `[nFrames × nMels]`。
 */
internal class MelSpectroProcessor(
    private val samplingRate: Int,
    private val config: LogMelSpectroConfig,
) : AudioPreprocessor {

    override fun close() = Unit

    override fun process(rawSpeech: FloatArray): FloatArray {
        if (rawSpeech.isEmpty()) return FloatArray(0)
        val audio =
            if (config.preemphasis > 0.0f) {
                applyPreemphasis(rawSpeech, config.preemphasis)
            } else {
                rawSpeech
            }

        val paddedNFFT = LogMel.nextPowerOfTwo(config.nFFT)
        val melSpec = LogMel.spectrogram(audio, samplingRate, paddedNFFT, config.nMels, config.hopLength)
        val validFrames = rawSpeech.size / config.hopLength

        if (config.normType == "whisper") {
            normalizeWhisper(melSpec, validFrames)
        } else {
            normalizeStandard(melSpec, validFrames)
        }

        val nFrames = if (config.nFrames > 0) config.nFrames else melSpec[0].size
        val result = FloatArray(nFrames * config.nMels)
        for (m in melSpec.indices) {
            for (f in melSpec[m].indices) {
                if (f >= nFrames) break
                val value = if (f < validFrames) melSpec[m][f] else 0.0f
                if (config.transpose) {
                    result[f * config.nMels + m] = value
                } else {
                    result[m * nFrames + f] = value
                }
            }
        }
        return result
    }

    /**
     * Whisper 的归一化：先把 ln 值除以 `ln(10)` 换回 log10 口径，
     * 再在**全局**最大值下裁剪到 `max-8`，最后线性映射到 `[0, 1]`（`(x-clip+4)/4`）。
     */
    private fun normalizeWhisper(melSpec: Array<FloatArray>, validFrames: Int) {
        var maxVal = Float.NEGATIVE_INFINITY
        for (m in melSpec.indices) {
            for (f in 0 until validFrames) {
                melSpec[m][f] = melSpec[m][f] / LN_10
                if (melSpec[m][f] > maxVal) maxVal = melSpec[m][f]
            }
        }
        val clipVal = maxVal - 8.0f
        for (m in melSpec.indices) {
            for (f in 0 until validFrames) {
                melSpec[m][f] = (max(melSpec[m][f], clipVal) + 4.0f) / 4.0f
            }
        }
    }

    /** 逐 mel 的 z-score，统计量只覆盖有效帧（样本方差，除以 `n-1`）。 */
    private fun normalizeStandard(melSpec: Array<FloatArray>, validFrames: Int) {
        val means = computeMean(melSpec, validFrames)
        val stds = computeStd(melSpec, means, validFrames)
        for (m in melSpec.indices) {
            val mean = means[m]
            val std = stds[m] + EPSILON
            for (f in 0 until validFrames) {
                melSpec[m][f] = (melSpec[m][f] - mean) / std
            }
        }
    }

    /** `y[n] = x[n] - coeff * x[n-1]`，首元素保持原值 */
    fun applyPreemphasis(audio: FloatArray, coeff: Float): FloatArray {
        if (audio.isEmpty()) return FloatArray(0)
        val result = FloatArray(audio.size)
        result[0] = audio[0]
        for (i in 1 until audio.size) {
            result[i] = audio[i] - coeff * audio[i - 1]
        }
        return result
    }

    private fun computeMean(melSpec: Array<FloatArray>, nFrames: Int): FloatArray {
        val result = FloatArray(melSpec.size)
        for (m in melSpec.indices) {
            var sum = 0.0f
            for (f in 0 until nFrames) {
                sum += melSpec[m][f]
            }
            result[m] = sum / nFrames
        }
        return result
    }

    private fun computeStd(melSpec: Array<FloatArray>, mean: FloatArray, nFrames: Int): FloatArray {
        val result = FloatArray(melSpec.size)
        for (m in melSpec.indices) {
            var sumSq = 0.0f
            for (f in 0 until nFrames) {
                val diff = melSpec[m][f] - mean[m]
                sumSq += diff * diff
            }
            result[m] = sqrt(sumSq / max(1, nFrames - 1))
        }
        return result
    }

    private companion object {
        val LN_10 = ln(10.0f)
        const val EPSILON = 1e-5f
    }
}
