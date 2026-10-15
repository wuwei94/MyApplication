package com.example.william.my.module.ml.asr

/** 不做任何预处理，直接返回输入（Moonshine 这类「吃原始波形」的图用）。 */
internal class DummyAudioProcessor : AudioPreprocessor {

    override fun close() = Unit

    override fun process(rawSpeech: FloatArray): FloatArray = rawSpeech
}
