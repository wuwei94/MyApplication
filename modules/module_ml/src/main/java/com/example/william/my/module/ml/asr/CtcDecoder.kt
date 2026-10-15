package com.example.william.my.module.ml.asr

import com.google.ai.edge.litert.TensorBuffer

/**
 * CTC 解码：逐帧取最大 logit 再把**连续重复**压掉（官方 `CtcDecoder` 的等价实现）。
 *
 * 用于 `parakeet-ctc-0.6b` —— 它是官方支持列表里唯一**没有**独立 decoder 子图的模型，
 * 主图直接输出 `[T, vocabSize]` 的帧级 logits，所以解码完全在宿主侧做。
 *
 * ⚠️ 两处「与官方逐字一致、但不要去『修』」的地方：
 * 1. 官方 KDoc 写的是「collapse repeats -> remove blanks」，但代码里**只做了压重复**，
 *    并没有把 blank 类剔除。这里保持原样 —— 换个模型/换个转换方式，blank 的下标就会变，
 *    擅自「修正」反而会引入上游没有的分支。
 * 2. 时间戳就是该 token 在 logits 里的**帧下标**（CTC 每帧都有输出，天然带时间）。
 */
internal class CtcDecoder(private val vocabSize: Int) : LiteRtRunner.Decoder {

    override fun close() = Unit

    override fun decode(encodeOutputBuffers: List<TensorBuffer>): Sequence<Pair<Int, Int>> = sequence {
        val logits = encodeOutputBuffers[0].readFloat()
        if (logits.isEmpty()) {
            yield(Pair(SpeechRecognizer.END_OF_SEQUENCE, SpeechRecognizer.NO_TIMESTAMP))
            return@sequence
        }
        yieldAll(collapseDuplicates(sampleMaximum(logits)))
    }

    /** 压掉连续重复的 token，时间戳取首次出现的位置 */
    internal fun collapseDuplicates(tokenIds: IntArray): Sequence<Pair<Int, Int>> = sequence {
        var previousId = -1
        var timestamp = 0
        for (id in tokenIds) {
            if (id != previousId) {
                yield(Pair(id, timestamp))
                previousId = id
            }
            timestamp++
        }
        yield(Pair(SpeechRecognizer.END_OF_SEQUENCE, timestamp))
    }

    /** 逐帧 argmax；`logits` 是 `[frames × vocabSize]` 的平坦数组 */
    internal fun sampleMaximum(logits: FloatArray): IntArray {
        val numFrames = logits.size / vocabSize
        val tokenIds = IntArray(numFrames)
        for (t in 0 until numFrames) {
            val start = t * vocabSize
            val end = start + vocabSize
            tokenIds[t] = (start until end).maxByOrNull { logits[it] }?.minus(start) ?: 0
        }
        return tokenIds
    }
}
