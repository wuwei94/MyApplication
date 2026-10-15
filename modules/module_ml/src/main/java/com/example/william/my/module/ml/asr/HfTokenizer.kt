package com.example.william.my.module.ml.asr

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 纯 Kotlin 的 HuggingFace `tokenizer.json` 解码器。
 *
 * **为什么不用官方那套**：官方 `HuggingfaceTokenizer` 走 JNI 调
 * [mlc-ai/tokenizers-cpp](https://github.com/mlc-ai/tokenizers-cpp)，需要三样东西同时到位 ——
 * `src/main/cpp/tokenizer_jni.cc`、预编译好的 `libtokenizers_c.a`（按 ABI 各一份）、
 * 以及 `externalNativeBuild` 的 NDK 工具链。而**前两样在官方仓库里根本不存在**
 * （`cpp/` 目录下只有一份 CMakeLists，README 也只写着「应由使用者自行提供」），
 * 也就是说公开仓的 ASR 样例**按原样是编译不过的**。本模块因此改为纯 Kotlin 实现，
 * 顺带避免了在库模块引入 NDK。
 *
 * **覆盖范围**：只实现**解码**方向（id → 文本），这正是接口 [Tokenizer] 需要的全部
 * —— 6 个模型的 `tokenizer.json` 实测只有这一处被用到（官方 JNI 也只暴露 `decode`）。
 * 词表形态、解码链类型都按实测的真实文件实现：
 *
 * | 模型 | 词表 | 解码链 |
 * |---|---|---|
 * | `whisper-tiny` | BPE map（50258 项，id 0–51864） | `ByteLevel` |
 * | `qwen3-asr-0.6b` | BPE map（151643 项） | `ByteLevel` |
 * | `moonshine-tiny` | BPE map（32000 项） | `Sequence[Replace, ByteFallback, Fuse, Strip]` |
 * | `parakeet-ctc-0.6b` | BPE map（1024 项） | `Metaspace` |
 * | `parakeet-tdt-0.6b-v3` | BPE map（8192 项） | `Metaspace` |
 * | `parakeet-tdt_ctc-0.6b-ja` | **Unigram list**（3072 项） | `Metaspace` |
 *
 * 遇到未实现的解码器类型会**直接抛异常并报出类型名** —— 免得静默产出错字。
 */
internal class HfTokenizer private constructor(
    private val idToToken: Map<Int, String>,
    private val specialIds: Set<Int>,
    private val steps: List<Step>,
    override val vocabSize: Int,
) : Tokenizer {

    override fun close() = Unit

    override fun decode(tokenIds: IntArray): String {
        var tokens = ArrayList<String>(tokenIds.size)
        for (id in tokenIds) {
            if (id in specialIds) continue
            val token = idToToken[id] ?: continue
            tokens.add(token)
        }
        for (step in steps) {
            tokens = ArrayList(step.apply(tokens))
        }
        // 官方 `HuggingfaceTokenizer.decode` 结尾也调了 trim()，这里保持一致：
        // 词首空格（whisper 的 ByteLevel 会带出来）不该出现在结果里。
        return tokens.joinToString("").trim()
    }

    /** 解码链上的一个环节，语义对齐 `tokenizers` 库的 `Decoder` */
    internal sealed interface Step {
        fun apply(tokens: List<String>): List<String>

        /** `Replace`：把 pattern 替换成 content（String 或 Regex 两种 pattern） */
        class Replace(private val pattern: String, private val replacement: String, private val isRegex: Boolean) : Step {
            private val regex: Regex? = if (isRegex) Regex(pattern) else null

            override fun apply(tokens: List<String>): List<String> = tokens.map { token ->
                regex?.replace(token, replacement) ?: token.replace(pattern, replacement)
            }
        }

        /** `Strip`：按需从每个词的头部/尾部剥掉指定字符 */
        class Strip(private val content: String, private val start: Boolean, private val stop: Boolean) : Step {
            override fun apply(tokens: List<String>): List<String> = tokens.map { token ->
                var chars = token
                if (start) {
                    var index = 0
                    while (index < chars.length && content.indexOf(chars[index]) >= 0) index++
                    chars = chars.substring(index)
                }
                if (stop) {
                    var index = chars.length
                    while (index > 0 && content.indexOf(chars[index - 1]) >= 0) index--
                    chars = chars.substring(0, index)
                }
                chars
            }
        }

        class Fuse : Step {
            override fun apply(tokens: List<String>): List<String> = listOf(tokens.joinToString(""))
        }

        /** `Metaspace`：`▁` 还原成空格；`prepend_scheme` 为 `always` 时吃掉词首那一个空格 */
        class Metaspace(private val replacement: String, private val prependScheme: String) : Step {
            override fun apply(tokens: List<String>): List<String> {
                val mapped = tokens.map { it.replace(replacement, " ") }.toMutableList()
                if (mapped.isEmpty()) return mapped
                if (prependScheme == "always" || prependScheme == "first") {
                    val first = mapped.first()
                    if (first.startsWith(" ")) mapped[0] = first.substring(1)
                }
                return mapped
            }
        }

        /**
         * `ByteFallback`：把 `<0xXX>` 形式的词还原成原始字节，遇到普通词时把攒下的字节
         * 先按 UTF-8 落地。moonshine 的词表就是这样处理中文等多字节字符的。
         */
        class ByteFallback : Step {
            override fun apply(tokens: List<String>): List<String> {
                val result = ArrayList<String>(tokens.size)
                val pending = ArrayList<Byte>()
                fun flush() {
                    if (pending.isNotEmpty()) {
                        result.add(String(pending.toByteArray(), Charsets.UTF_8))
                        pending.clear()
                    }
                }
                for (token in tokens) {
                    val match = BYTE_TOKEN.matchEntire(token)
                    if (match != null) {
                        pending.add(match.groupValues[1].toInt(16).toByte())
                    } else {
                        flush()
                        result.add(token)
                    }
                }
                flush()
                return result
            }
        }

        /**
         * `ByteLevel`：GPT-2 的 byte-to-unicode 反向映射。
         *
         * 与 `tokenizers` 的一处刻意差异：那边是**逐词**做字节→UTF-8 解码，一个多字节字符
         * 若被拆到两个词里就会各吐出替换符；这里把整段词的字节**攒齐后一次性** UTF-8 解码，
         * 同样的输入能还原出正确文字。
         */
        class ByteLevel : Step {
            override fun apply(tokens: List<String>): List<String> {
                val bytes = ArrayList<Byte>(tokens.size * 4)
                for (token in tokens) {
                    for (char in token) bytes.add(charToByte(char))
                }
                return listOf(String(bytes.toByteArray(), Charsets.UTF_8))
            }
        }
    }

    companion object {

        fun fromJson(json: String): HfTokenizer {
            val root = JSONObject(json)
            val model = root.optJSONObject("model") ?: JSONObject()
            val idToToken = LinkedHashMap<Int, String>()
            readVocab(model, idToToken)

            val specialIds = HashSet<Int>()
            val addedTokens = root.optJSONArray("added_tokens")
            if (addedTokens != null) {
                for (i in 0 until addedTokens.length()) {
                    val entry = addedTokens.optJSONObject(i) ?: continue
                    val id = entry.optInt("id", -1)
                    val content = entry.optString("content", "")
                    if (id < 0 || content.isEmpty()) continue
                    idToToken[id] = content
                    if (entry.optBoolean("special", false)) specialIds.add(id)
                }
            }

            val steps = ArrayList<Step>()
            readDecoder(root.optJSONObject("decoder"), steps)

            val vocabSize = (idToToken.keys.maxOrNull() ?: -1) + 1
            return HfTokenizer(idToToken, specialIds, steps, vocabSize)
        }

        fun fromFile(file: File): HfTokenizer = fromJson(file.readText(Charsets.UTF_8))

        /**
         * 解析 `model.vocab`，两种实测形态都支持：
         * - BPE：`{"token": id}`
         * - Unigram：`[["token", score], ...]` —— **id 就是数组下标**，不是那个 score
         */
        private fun readVocab(model: JSONObject, idToToken: MutableMap<Int, String>) {
            model.optJSONObject("vocab")?.let { vocab ->
                for (token in vocab.keys()) {
                    idToToken[vocab.getInt(token)] = token
                }
                return
            }
            val array = model.optJSONArray("vocab") ?: return
            for (id in 0 until array.length()) {
                val entry = array.optJSONArray(id) ?: continue
                val token = entry.optString(0, "")
                if (token.isNotEmpty()) idToToken[id] = token
            }
        }

        private fun readDecoder(decoder: JSONObject?, steps: MutableList<Step>) {
            if (decoder == null) return
            when (val type = decoder.optString("type", "")) {
                "" -> Unit
                "Sequence" -> {
                    val children = decoder.optJSONArray("decoders") ?: JSONArray()
                    for (i in 0 until children.length()) {
                        readDecoder(children.optJSONObject(i), steps)
                    }
                }
                "ByteLevel" -> steps.add(Step.ByteLevel())
                "ByteFallback" -> steps.add(Step.ByteFallback())
                "Fuse" -> steps.add(Step.Fuse())
                "Metaspace" -> steps.add(
                    Step.Metaspace(
                        replacement = decoder.optString("replacement", "\u2581"),
                        prependScheme = decoder.optString("prepend_scheme", "always"),
                    ),
                )
                "Strip" -> steps.add(
                    Step.Strip(
                        content = decoder.optString("content", ""),
                        start = decoder.optInt("start", 0) == 1,
                        stop = decoder.optInt("stop", 0) == 1,
                    ),
                )
                "Replace" -> {
                    val pattern = decoder.optJSONObject("pattern")
                    if (pattern != null && pattern.has("Regex")) {
                        steps.add(
                            Step.Replace(
                                pattern = pattern.getString("Regex"),
                                replacement = decoder.optString("content", ""),
                                isRegex = true,
                            ),
                        )
                    } else {
                        steps.add(
                            Step.Replace(
                                pattern = pattern?.optString("String", "") ?: "",
                                replacement = decoder.optString("content", ""),
                                isRegex = false,
                            ),
                        )
                    }
                }
                else -> throw IllegalArgumentException("尚未实现的 tokenizer 解码器类型：$type")
            }
        }

        private val BYTE_TOKEN = Regex("^<0x([0-9A-Fa-f]{2})>$")

        /**
         * GPT-2 byte-to-unicode 表里「不可打印字节」的排列顺序：
         * `0..32`、`127..160`、`173` 依次映射到 `U+0100` 起。
         */
        private val NON_PRINTABLE_BYTES: IntArray = run {
            val values = ArrayList<Int>(68)
            for (b in 0..32) values.add(b)
            for (b in 127..160) values.add(b)
            values.add(173)
            values.toIntArray()
        }

        private fun charToByte(char: Char): Byte {
            val code = char.code
            if (code in 0x100..0x143) return NON_PRINTABLE_BYTES[code - 0x100].toByte()
            return (code and 0xFF).toByte()
        }
    }
}
