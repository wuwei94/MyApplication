package com.example.william.my.module.ml.asr

/**
 * 原始波形 → 模型输入特征。
 *
 * 官方有两个实现：真算 log-Mel 的 [MelSpectroProcessor]，以及**原样返回**的
 * [DummyAudioProcessor]。后者不是摆设 —— Moonshine 系列的图直接吃原始波形
 * （官方 README：「Except moonshine which gets raw audio as input」）。
 */
internal interface AudioPreprocessor : AutoCloseable {

    fun process(rawSpeech: FloatArray): FloatArray
}
