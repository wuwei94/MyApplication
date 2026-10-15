package com.example.william.my.module.ml.asr

import kotlin.time.Duration

/**
 * 原始音频源。
 *
 * 与官方 `AudioSource` 一一对应：把「从哪来」（文件 / 麦克风）与「怎么切窗口」解耦，
 * 上层拿到的永远是**带重叠的定长块** —— 因为官方支持的所有模型都不是流式模型，
 * 只能靠重叠窗口拼接（见 [FileAudioSource] / [MicrophoneAudioSource]）。
 */
internal interface AudioSource : AutoCloseable {

    val samplingRate: Int

    /** 每块的时长 */
    val interval: Duration

    /** 与上一块的重叠时长，必须严格小于 [interval] */
    val overlap: Duration

    /** 依次产出音频块（每块长度均为 `interval × samplingRate` 个采样） */
    fun getAudioData(): Sequence<FloatArray>
}
