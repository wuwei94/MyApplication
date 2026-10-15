package com.example.william.my.module.ml.asr

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.DurationUnit

/** 可替换的录音读取器：抽出接口只为让 [MicrophoneAudioSource] 的窗口逻辑能落进纯 JVM 单测。 */
internal interface AudioReader : AutoCloseable {
    /** @return 读到的采样数；负数表示错误码 */
    fun read(audioData: ShortArray, offsetInShorts: Int, sizeInShorts: Int): Int
}

/** [AudioReader] 的默认实现，底层是 `AudioRecord`（VOICE_RECOGNITION 源、单声道、16-bit）。 */
internal class AudioRecordReader(samplingRate: Int, minBufferSizeInBytes: Int) : AudioReader {

    @SuppressLint("MissingPermission")
    private val audioRecord =
        AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            samplingRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(
                minBufferSizeInBytes,
                AudioRecord.getMinBufferSize(
                    samplingRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                ),
            ),
        )

    init {
        check(audioRecord.state == AudioRecord.STATE_INITIALIZED) { "AudioRecord 初始化失败" }
        audioRecord.startRecording()
    }

    override fun close() {
        runCatching { audioRecord.stop() }
        runCatching { audioRecord.release() }
    }

    override fun read(audioData: ShortArray, offsetInShorts: Int, sizeInShorts: Int): Int = audioRecord.read(audioData, offsetInShorts, sizeInShorts)
}

/**
 * 麦克风实时音频源。
 *
 * 与官方 `MicrophoneAudioSource` 一致：固定按 `interval - overlap` 步进读入，
 * 缓冲区左移后产出完整一块；**每步做一次 RMS 静音判定**（阈值 0.005），
 * 判定为静音时整块写 0 —— 官方用这一招抑制底噪被识别成幻听文本。
 *
 * 与官方的一处差异：官方把「瞬时错误」（`ERROR` / `ERROR_BAD_VALUE`）当作
 * 可跳过的坏帧继续重试，本模块沿用；但官方对 `ERROR_DEAD_OBJECT` /
 * `ERROR_INVALID_OPERATION` 直接结束序列，这里也一样。除此之外重新组织成
 * `while` + 显式返回值，可读性更好但语义不变。
 */
internal class MicrophoneAudioSource(
    override val samplingRate: Int,
    override val interval: Duration,
    override val overlap: Duration,
    private val getAudioReader: (Int) -> AudioReader = { minBufferSize ->
        AudioRecordReader(samplingRate, minBufferSize)
    },
) : AudioSource {

    init {
        require(interval > overlap) { "interval 必须严格长于 overlap" }
    }

    @SuppressLint("MissingPermission")
    override fun getAudioData(): Sequence<FloatArray> = sequence {
        val numOfSamplesPerInterval = getNumSamples(interval, samplingRate)
        val numOfSamplesOverlapped = getNumSamples(overlap, samplingRate)
        val step = numOfSamplesPerInterval - numOfSamplesOverlapped

        getAudioReader(step * BYTES_PER_SAMPLE).use { audioReader ->
            // 多留一个 step，产出时新采样从尾部写入、随后整体左移
            val bufferForSamples = FloatArray(numOfSamplesPerInterval + step)
            val rawBuffer = ShortArray(step)
            var numOfSamplesRead = 0

            while (true) {
                val readBytes = audioReader.read(rawBuffer, numOfSamplesRead, step - numOfSamplesRead)
                when {
                    readBytes == 0 ||
                        readBytes == AudioRecord.ERROR_DEAD_OBJECT ||
                        readBytes == AudioRecord.ERROR_INVALID_OPERATION -> return@sequence
                    readBytes < 0 -> continue // 瞬时错误，丢掉这一帧重试
                    else -> {
                        numOfSamplesRead += readBytes
                        if (numOfSamplesRead < step) continue
                        numOfSamplesRead = 0
                    }
                }

                var sumSq = 0.0f
                for (i in 0 until step) {
                    val sample = rawBuffer[i] / MAX_16BIT_PCM_VALUE
                    sumSq += sample * sample
                }

                val rms = sqrt((sumSq / step).toDouble()).toFloat()
                val isSilent = rms < SILENCE_THRESHOLD
                val sampleStartIndex = bufferForSamples.size - step
                for (i in 0 until step) {
                    bufferForSamples[sampleStartIndex + i] =
                        if (isSilent) 0.0f else rawBuffer[i] / MAX_16BIT_PCM_VALUE
                }

                yield(bufferForSamples)

                System.arraycopy(bufferForSamples, step, bufferForSamples, 0, bufferForSamples.size - step)
            }
        }
    }

    override fun close() = Unit

    private companion object {
        const val MAX_16BIT_PCM_VALUE = 32768.0f
        const val SILENCE_THRESHOLD = 0.005f

        /** `AudioRecord` 的 minBufferSize 提示值：官方按每个采样 4 字节预留 */
        const val BYTES_PER_SAMPLE = 4

        fun getNumSamples(duration: Duration, samplingRate: Int): Int = (samplingRate * duration.toDouble(DurationUnit.SECONDS)).toInt()
    }
}
