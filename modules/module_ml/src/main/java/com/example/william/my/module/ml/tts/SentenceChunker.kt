package com.example.william.my.module.ml.tts

/**
 * 把输入文本切成「逐句」的合成块
 *
 * 这是上游 pip 包 `chunk_text` 的忠实移植，同时也是**流式粒度**：每句一次完整合成，
 * 合成完就丢给播放器，播放第 N 句的同时合成第 N+1 句，因此首音延迟只有一句。
 *
 * 两处上游怪癖是**故意保留**的（模型的韵律就是照着这个前端调的）：
 * 1. 句末的 `.!?` 会被切分吃掉；
 * 2. 切完后再给每块补一个逗号，于是模型看到的块末 token 永远是 `,`。
 */
object SentenceChunker {

    private val SENTENCE_END = Regex("[.!?]+")
    private const val MAX_CHUNK_CHARS = 400
    private const val PUNCTUATION = ".!?,;:"

    fun chunk(text: String): List<String> {
        val chunks = ArrayList<String>()
        for (sentence in SENTENCE_END.split(text)) {
            val trimmed = sentence.trim()
            if (trimmed.isEmpty()) continue
            if (trimmed.length <= MAX_CHUNK_CHARS) {
                chunks.add(ensurePunctuation(trimmed))
            } else {
                // 过长句：按词边界再切
                val builder = StringBuilder()
                for (word in trimmed.split(Regex("\\s+"))) {
                    if (builder.length + word.length + 1 > MAX_CHUNK_CHARS && builder.isNotEmpty()) {
                        chunks.add(ensurePunctuation(builder.toString()))
                        builder.setLength(0)
                    }
                    if (builder.isNotEmpty()) {
                        builder.append(' ')
                    }
                    builder.append(word)
                }
                if (builder.isNotEmpty()) {
                    chunks.add(ensurePunctuation(builder.toString()))
                }
            }
        }
        return chunks
    }

    private fun ensurePunctuation(sentence: String): String = if (sentence.last() in PUNCTUATION) sentence else "$sentence,"
}
