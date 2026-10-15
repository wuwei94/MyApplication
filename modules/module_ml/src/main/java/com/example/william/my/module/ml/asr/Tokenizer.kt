package com.example.william.my.module.ml.asr

/**
 * token id → 可读文本。
 *
 * 官方实现是 `HuggingfaceTokenizer`，底层走 **JNI + mlc-ai/tokenizers-cpp**。
 * 本模块改成纯 Kotlin 的 [HfTokenizer]，原因见该类的 KDoc。
 *
 * 注意接口只暴露 [decode]：[CtcDecoder] 需要 [vocabSize] 来切分 logits，
 * 除此之外全流程只用得到「把 id 串成文本」这一个方向。
 */
internal interface Tokenizer : AutoCloseable {

    val vocabSize: Int

    fun decode(tokenIds: IntArray): String
}
