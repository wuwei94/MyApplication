package com.example.william.my.module.ml.asr

/**
 * 从已预处理的特征里识别语音。
 *
 * 返回的是 `(tokenId, 相对时间戳)` 序列，末尾以 [END_OF_SEQUENCE] 收尾；
 * 模型不产出时间戳时用 [NO_TIMESTAMP]（此时后续的词对齐只能靠编辑距离启发式）。
 */
internal interface SpeechRecognizer : AutoCloseable {

    fun recognize(features: FloatArray): Sequence<Pair<Int, Int>>

    companion object {
        /** 序列结束哨兵 */
        const val END_OF_SEQUENCE = -1

        /** 该模型不提供时间戳 */
        const val NO_TIMESTAMP = -1
    }
}
