package com.example.william.my.module.ml.tts

import org.json.JSONObject
import java.io.File
import java.text.Normalizer

/**
 * Qwen2 词表的 byte-level BPE 分词器
 *
 * 对应官方 `text_to_speech_lm` 样例的 `QwenBpeTokenizer.kt`，实现 GPT-2 系流水线三步：
 * 先按 Qwen2 预分词正则切段，再把 UTF-8 字节映射到 byte-level unicode 字母表，
 * 最后按 merge 排名贪心合并。这里只编码**纯文本**：TTS 提示词里的特殊 token
 * （`<|im_start|>` / `<|im_end|>` 等）id 固定，由 [Qwen3TtsEngine] 直接填入。
 *
 * **构造与词表解析是分开的**：核心只吃「词表映射 + merges 行」两个纯数据入参，
 * 因此分词逻辑（预分词、字节映射、merge 顺序、NFC 归一化）可以脱离 Android 与 2.7 MB
 * 的 `vocab.json` 做单测；[fromFiles] 才负责用 `org.json` 把文件读成这两个入参。
 */
internal class QwenBpeTokenizer(
    private val vocabulary: Map<String, Int>,
    merges: List<String>,
) {

    /** `(左 piece id, 右 piece id) -> merge 排名`，排名越小越先合并 */
    private val ranks = HashMap<Long, Int>(merges.size * 2)

    /** piece 字符串到 id 的缓存，避免每轮合并都查一次哈希表 */
    private val pieceIds = HashMap<String, Int>()

    init {
        // 官方同样跳过空行与 `#version` 头，且 rank 只在有效行上自增
        merges
            .filter { it.isNotBlank() && !it.startsWith("#version") }
            .forEachIndexed { rank, line ->
                val separator = line.indexOf(' ')
                require(separator > 0) { "merges 行格式异常：$line" }
                ranks[pairKey(pieceId(line.substring(0, separator)), pieceId(line.substring(separator + 1)))] = rank
            }
    }

    /** 词表条目数；仅用于界面展示，不参与推理 */
    val vocabularySize: Int
        get() = vocabulary.size

    /**
     * 编码纯文本（不含特殊 token）
     *
     * 参考实现（Rust tokenizers）先做 NFC 归一化，这里保持一致：少了这一步，
     * 「组合字符」写法（如 `e` + U+0301）会与预组合写法（`é`）编出不同的 id。
     */
    fun encode(text: String): IntArray {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFC)
        val out = ArrayList<Int>(normalized.length / 3 + 8)
        for (match in PRETOKENIZE.findAll(normalized)) {
            val bytes = match.value.toByteArray(Charsets.UTF_8)
            val mapped = StringBuilder(bytes.size)
            for (b in bytes) {
                mapped.append(BYTE_TO_CHAR[b.toInt() and 0xFF])
            }
            mergePieces(mapped.toString(), out)
        }
        return out.toIntArray()
    }

    /**
     * 与官方启动自检等价：逐条比对参考 token id
     *
     * 官方把比对写在 Activity 里并只往 logcat 打结果；这里改成返回不一致的描述，
     * 页面可以把它显示出来，测试也可以直接断言（见 `QwenTtsTest`）。
     *
     * @return 不一致的描述；**空列表表示全部通过**
     */
    fun findMismatches(vectors: List<Pair<String, IntArray>>): List<String> = vectors.mapNotNull { (text, expected) ->
        val actual = encode(text)
        if (actual.contentEquals(expected)) {
            null
        } else {
            "「$text」期望 ${expected.toList()}，实际 ${actual.toList()}"
        }
    }

    private fun mergePieces(token: String, out: ArrayList<Int>) {
        var pieces = token.map { it.toString() }
        if (pieces.size == 1) {
            out.add(vocabulary[token] ?: error("词表缺少单字节 piece：$token"))
            return
        }
        while (pieces.size > 1) {
            var bestRank = Int.MAX_VALUE
            var bestIndex = -1
            for (i in 0 until pieces.size - 1) {
                // 两侧 piece 任一不在词表里，这一对就不可能被合并过
                val left = vocabulary[pieces[i]] ?: continue
                val right = vocabulary[pieces[i + 1]] ?: continue
                val rank = ranks[pairKey(left, right)] ?: continue
                if (rank < bestRank) {
                    bestRank = rank
                    bestIndex = i
                }
            }
            if (bestIndex < 0) break
            val merged = pieces[bestIndex] + pieces[bestIndex + 1]
            pieces = pieces.subList(0, bestIndex) + merged + pieces.subList(bestIndex + 2, pieces.size)
        }
        for (piece in pieces) {
            out.add(vocabulary[piece] ?: error("词表缺少 piece：$piece"))
        }
    }

    private fun pieceId(piece: String): Int = pieceIds.getOrPut(piece) {
        vocabulary[piece] ?: throw IllegalStateException("merges 里的 piece 不在词表中：$piece")
    }

    private fun pairKey(left: Int, right: Int): Long = (left.toLong() shl 32) or right.toLong()

    companion object {

        /**
         * Qwen2 预分词正则（取自 `tokenizer.json`）
         *
         * 参考实现是 Rust 且 `\s` 是 Unicode 感知的；Android 的 ICU 正则默认就是 Unicode 感知
         * （反过来，桌面 JVM 的 `UNICODE_CHARACTER_CLASS` 开关在 Android 上会被拒绝，不能加）。
         * 官方用 `assets/tokenizer_test_vectors.json` 的参考向量逐条验证过这套正则，
         * 包括 U+3000 全角空格，本模块沿用同一份向量做启动自检。
         */
        private val PRETOKENIZE = Regex(
            "(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\\r\\n\\p{L}\\p{N}]?\\p{L}+|\\p{N}|" +
                " ?[^\\s\\p{L}\\p{N}]+[\\r\\n]*|\\s*[\\r\\n]+|\\s+(?!\\S)|\\s+",
        )

        /**
         * GPT-2 的 byte-to-unicode 表：可打印字节映射为自身，其余按顺序映射到 U+0100 起
         *
         * 目的是让「任意字节序列」都能用合法 unicode 字符串表示，从而能进 BPE 的词表。
         */
        private val BYTE_TO_CHAR: CharArray = CharArray(256).let { table ->
            val printable = (('!'.code..'~'.code) + ('¡'.code..'¬'.code) + ('®'.code..'ÿ'.code)).toHashSet()
            var next = 256
            for (b in 0 until 256) {
                table[b] = if (b in printable) b.toChar() else (next++).toChar()
            }
            table
        }

        /** 从 `vocab.json` + `merges.txt` 构建；两者都在模型下载清单里 */
        fun fromFiles(vocabFile: File, mergesFile: File): QwenBpeTokenizer {
            val json = JSONObject(vocabFile.readText())
            val vocabulary = HashMap<String, Int>(json.length() * 4 / 3 + 16)
            for (key in json.keys()) {
                vocabulary[key] = json.getInt(key)
            }
            return QwenBpeTokenizer(vocabulary, mergesFile.readLines())
        }
    }
}
