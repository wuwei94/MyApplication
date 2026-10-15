package com.example.william.my.module.ml.asr

/**
 * 后处理：把逐 token 的输出整理成「已确认 / 未确认」两段文本。
 *
 * 界面把未确认部分标灰 —— 这样用户能直接看出哪些字还可能被下一次重叠窗口改写。
 */
internal interface Postprocessor : AutoCloseable {

    data class DecodedText(val confirmedText: String, val unconfirmedText: String)

    /**
     * @param tokenId token id，或 [SpeechRecognizer.END_OF_SEQUENCE]
     * @param timestamp 该 token 的相对时间戳，或 [SpeechRecognizer.NO_TIMESTAMP]
     * @return `null` 表示「本次不更新界面」
     */
    fun decode(tokenId: Int, timestamp: Int): DecodedText?
}
