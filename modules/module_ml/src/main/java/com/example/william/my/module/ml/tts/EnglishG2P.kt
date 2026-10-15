package com.example.william.my.module.ml.tts

import android.content.Context
import com.example.william.my.module.ml.download.ModelStore
import com.example.william.my.module.ml.download.RemoteModels
import com.example.william.my.module.ml.helper.TFLiteModelHelper
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import org.json.JSONObject
import java.io.BufferedReader
import java.io.Closeable
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

/**
 * 英文 G2P（字素转音素），运行期不依赖 espeak
 *
 * 混合式，与 kokoro 同构：
 * 1. 一张 27.5 万条的 espeak-IPA 词典（主力，覆盖绝大多数常用词的正确读音）；
 * 2. 一个 DeepPhonemizer ForwardTransformer（OpenPhonemizer 的 espeak-IPA checkpoint，
 *    Clear BSD / MIT），负责词典外的词。它被转成**固定形状**的 LiteRT 图，跑在 CompiledModel
 *    的 CPU 上——该图用到 EQUAL / SELECT_V2 / 大于 4 维的注意力张量，GPU delegate 会拒绝，
 *    但 CPU 加速器可以正常执行。
 *
 * ```
 * 词 -> 词典[词] 或 (字符 id × charRepeats, <en_us>…<end>, 0 补齐到 maxTokens)
 *       -> g2p.tflite (CPU) -> argmax -> 合并重复 -> espeak 风格 IPA
 * 整句 IPA -> 符号 id（178 符号 IPA 集）
 * ```
 *
 * 产出的音素集合与模型训练时使用的（espeak en-us IPA）一致，因此能 1:1 映射到符号表。
 *
 * 两个 TTS 示例共用本类；官方也是同一套前端，**唯一差别在标点**：
 * Matcha 把标点直接贴在前一个词后面（`"wˈɜːld,"`），而 KittenTTS 的时长预测器是对着
 * 上游 pip 包的 `basic_english_tokenize` 训练的，标点必须**单独成词**（`"wˈɜːld ,"`）。
 * 因此用 [punctuationSpaced] 区分，而不是把这份 300 行的前端复制两遍。
 *
 * @param punctuationSpaced 标点是否作为独立 token 输出（KittenTTS 页为 `true`，Matcha 页为 `false`）
 */
class EnglishG2P(
    context: Context,
    private val punctuationSpaced: Boolean = false,
) : Closeable {

    private val dictionary = HashMap<String, String>(300_000)

    private val g2pModel: CompiledModel
    private val g2pInputs: List<TensorBuffer>
    private val g2pOutputs: List<TensorBuffer>

    private val charToIndex = HashMap<Char, Int>()
    private val indexToPhoneme = HashMap<Int, String>()

    // 以下字段在 [loadMetadata] 里赋值（`init` 只建图）；默认值仅让编译器通过，
    // `init` 一定会调用 loadMetadata 覆盖它们，失败时则会抛异常、实例不会交付
    private var specialTokens: Set<String> = emptySet()
    private var charRepeats: Int = 0
    private var startId: Int = 0
    private var endId: Int = 0
    private var maxTokens: Int = 0
    private var numPhonemes: Int = 0
    private val symbolToId = HashMap<Char, Int>()

    init {
        val path = ModelStore.pathIn(context.filesDir, MODEL_FILE)
        check(ModelStore.isReady(context, remote)) {
            "模型尚未下载: $MODEL_FILE"
        }

        // 图与缓冲区建好后，元数据 / 词典解析仍可能失败（资产损坏、内存不足）；此时实例不会
        // 交给调用方，已建成的原生资源必须就地回收 —— 两者都持有原生内存，GC 兜不住
        val created = ArrayList<AutoCloseable>(3)
        try {
            g2pModel = CompiledModel.create(path, TFLiteModelHelper.createCpuOptions(), null)
            created += g2pModel
            g2pInputs = g2pModel.createInputBuffers()
            created.addAll(g2pInputs)
            g2pOutputs = g2pModel.createOutputBuffers()
            created.addAll(g2pOutputs)

            loadMetadata(context)
        } catch (e: Throwable) {
            // 逆序回收：TensorBuffer 先于所属 CompiledModel 关闭
            created.asReversed().forEach { runCatching { it.close() } }
            throw e
        }
    }

    /**
     * 解析模型元数据与 espeak-IPA 词典
     *
     * 构造期调用；失败时由 [init] 的 catch 回收已建成的图与缓冲区。
     */
    private fun loadMetadata(context: Context) {
        val meta = JSONObject(context.assets.open(META_ASSET).use { it.readBytes() }.decodeToString())
        val char2idx = meta.getJSONObject("char2idx")
        for (key in char2idx.keys()) {
            if (key.length == 1) {
                charToIndex[key[0]] = char2idx.getInt(key)
            }
        }
        val idx2ph = meta.getJSONObject("idx2ph")
        for (key in idx2ph.keys()) {
            indexToPhoneme[key.toInt()] = idx2ph.getString(key)
        }
        charRepeats = meta.getInt("char_repeats")
        startId = meta.getInt("start")
        endId = meta.getInt("end")
        maxTokens = meta.getInt("MAXT")
        numPhonemes = meta.getInt("n_phonemes")
        val special = meta.getJSONArray("special")
        specialTokens = (0 until special.length()).map { special.getString(it) }.toSet()

        // matcha 符号 -> id（keithito 符号表来自 config.json）
        val config = JSONObject(context.assets.open(CONFIG_ASSET).use { it.readBytes() }.decodeToString())
        val symbols = config.getJSONArray("symbols")
        for (i in 0 until symbols.length()) {
            val symbol = symbols.getString(i)
            if (symbol.length == 1) {
                symbolToId[symbol[0]] = i
            }
        }

        // espeak-IPA 词典（主力 G2P）：每行 `词<TAB>ipa`
        val dictStream = InputStreamReader(openDictionary(context), Charsets.UTF_8)
        BufferedReader(dictStream).use { reader ->
            reader.forEachLine { line ->
                val tab = line.indexOf('\t')
                if (tab > 0) {
                    dictionary[line.substring(0, tab)] = line.substring(tab + 1)
                }
            }
        }
    }

    /**
     * 打开词典资产
     *
     * aapt 会把 `.gz` 资产解压并去掉扩展名（除非该扩展名进了 `noCompress`）——
     * 已实测本模块产出的 AAR 内资产名就是 `g2p_dict.txt`（7.2 MB，非压缩）。
     * 这里仍按两种名字各试一次，避免将来构建配置改动导致运行期直接找不到文件。
     */
    private fun openDictionary(context: Context): InputStream = try {
        context.assets.open(DICT_ASSET)
    } catch (e: FileNotFoundException) {
        GZIPInputStream(context.assets.open("$DICT_ASSET.gz"))
    }

    /**
     * 整句文本 -> 符号 id（BOS/EOS 由各合成器自行补）
     *
     * host 侧文本规范化：全大写缩写逐字母拼读（GPU -> "gee pee you"），
     * 数字按词读出（4090 -> "four thousand ninety"）。
     */
    fun phonemize(text: String): IntArray {
        val ipa = StringBuilder()
        var first = true
        fun append(phonemes: String) {
            if (phonemes.isEmpty()) return
            if (!first) {
                ipa.append(' ')
            }
            ipa.append(phonemes)
            first = false
        }
        for (match in TOKEN.findAll(text)) {
            val token = match.value
            when {
                ACRONYM.matches(token) -> {
                    append(token.lowercase().mapNotNull { LETTER_IPA[it] }.joinToString(""))
                }
                token[0].isDigit() -> {
                    for (word in numberToWords(token)) {
                        append(dictionary[word] ?: phonemizeWord(word))
                    }
                }
                WORD.matches(token) -> {
                    // 词典优先；词典外才交给神经 G2P
                    append(dictionary[token.lowercase()] ?: phonemizeWord(token.lowercase()))
                }
                else -> {
                    // 标点：KittenTTS 要求独立成词（走 append 会插空格），Matcha 要求紧贴前词
                    if (punctuationSpaced) append(token) else ipa.append(token)
                }
            }
        }
        val ids = ArrayList<Int>(ipa.length)
        for (ch in ipa) {
            symbolToId[ch]?.let { ids.add(it) }
        }
        return ids.toIntArray()
    }

    /** "1,234.5" -> ["one","thousand","two","hundred","thirty","four","point","five"] */
    private fun numberToWords(raw: String): List<String> {
        val token = raw.replace(",", "")
        if (token.contains('.')) {
            val parts = token.split('.', limit = 2)
            val words = (
                if (parts[0].isNotEmpty()) {
                    integerToWords(parts[0].toLongOrNull() ?: 0L)
                } else {
                    listOf("zero")
                }
                ).toMutableList()
            words.add("point")
            for (digit in parts[1]) {
                if (digit.isDigit()) {
                    words.add(ONES[digit - '0'])
                }
            }
            return words
        }
        return integerToWords(token.toLongOrNull() ?: return emptyList())
    }

    private fun wordsUnderThousand(value: Int): List<String> {
        var n = value
        val words = ArrayList<String>(4)
        if (n >= 100) {
            words.add(ONES[n / 100])
            words.add("hundred")
            n %= 100
        }
        if (n >= 20) {
            words.add(TENS[n / 10])
            n %= 10
        }
        if (n > 0) {
            words.add(ONES[n])
        }
        return words
    }

    private fun integerToWords(value: Long): List<String> {
        if (value == 0L) return listOf("zero")
        if (value < 0) return listOf("minus") + integerToWords(-value)
        val groups = ArrayList<Int>()
        var n = value
        while (n > 0) {
            groups.add((n % 1000).toInt())
            n /= 1000
        }
        if (groups.size > SCALES.size) {
            // 太大读不出名字；逐位数字读
            return value.toString().map { ONES[it - '0'] }
        }
        val words = ArrayList<String>()
        for (i in groups.indices.reversed()) {
            if (groups[i] == 0) continue
            words.addAll(wordsUnderThousand(groups[i]))
            if (SCALES[i].isNotEmpty()) {
                words.add(SCALES[i])
            }
        }
        return words
    }

    /** 用神经 G2P 把单个词转成 espeak 风格 IPA 串 */
    fun phonemizeWord(word: String): String {
        val ids = ArrayList<Int>(maxTokens)
        ids.add(startId)
        for (ch in word) {
            charToIndex[ch]?.let { id -> repeat(charRepeats) { ids.add(id) } }
        }
        ids.add(endId)
        val length = minOf(ids.size, maxTokens)
        val input = FloatArray(maxTokens) { if (it < length) ids[it].toFloat() else 0f }

        g2pInputs[0].writeFloat(input)
        g2pModel.run(g2pInputs, g2pOutputs)
        val logits = g2pOutputs[0].readFloat() // [maxTokens * numPhonemes]

        val result = StringBuilder()
        var previous = -1
        for (t in 0 until length) {
            var best = 0
            var bestScore = logits[t * numPhonemes]
            for (k in 1 until numPhonemes) {
                val score = logits[t * numPhonemes + k]
                if (score > bestScore) {
                    bestScore = score
                    best = k
                }
            }
            if (best == previous) continue
            previous = best
            val phoneme = indexToPhoneme[best] ?: continue
            if (phoneme in specialTokens || best == 0) continue
            for (ch in phoneme) {
                if (ch != '-') { // 去掉缩写的连字符
                    result.append(ch)
                }
            }
        }
        return result.toString()
    }

    override fun close() {
        g2pInputs.forEach { it.close() }
        g2pOutputs.forEach { it.close() }
        g2pModel.close()
    }

    private val remote
        get() = RemoteModels.MATCHA.first { it.fileName == MODEL_FILE }

    companion object {
        private const val MODEL_FILE = "dp_g2p_matcha_fp16.tflite"

        // 词典 1.68 MB，随 assets 分发（官方同样如此）。
        // 注：官方在 APK 中存放的是 g2p_dict.txt.gz —— aapt 会自动解压 .gz 资产并去掉扩展名，
        // 因此打包后的资源名是 g2p_dict.txt，AssetManager 读取时对压缩方式透明。
        private const val DICT_ASSET = "g2p_dict.txt"
        private const val META_ASSET = "g2p_meta.json"
        private const val CONFIG_ASSET = "config.json"

        // 大小写敏感的分词：ACRONYM（2 个以上大写）| NUMBER | word | 标点
        private val TOKEN = Regex("[A-Z]{2,}|\\d[\\d,]*(?:\\.\\d+)?|[A-Za-z']+|[.,!?;:—…\"]")
        private val ACRONYM = Regex("[A-Z]{2,}")
        private val WORD = Regex("[A-Za-z']+")

        // espeak 字母名 IPA —— 缩写逐字母读出（如 "GPU" -> dʒˈiːpˈiːjˈuː）
        private val LETTER_IPA = mapOf(
            'a' to "ˈeɪ", 'b' to "bˈiː", 'c' to "sˈiː", 'd' to "dˈiː", 'e' to "ˈiː",
            'f' to "ˈɛf", 'g' to "dʒˈiː", 'h' to "ˈeɪtʃ", 'i' to "ˈaɪ", 'j' to "dʒˈeɪ",
            'k' to "kˈeɪ", 'l' to "ˈɛl", 'm' to "ˈɛm", 'n' to "ˈɛn", 'o' to "ˈoʊ",
            'p' to "pˈiː", 'q' to "kjˈuː", 'r' to "ˈɑːɹ", 's' to "ˈɛs", 't' to "tˈiː",
            'u' to "jˈuː", 'v' to "vˈiː", 'w' to "dˈʌbəljˌuː", 'x' to "ˈɛks",
            'y' to "wˈaɪ", 'z' to "zˈiː",
        )
        private val ONES = arrayOf(
            "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
            "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen",
            "eighteen", "nineteen",
        )
        private val TENS = arrayOf(
            "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety",
        )
        private val SCALES = arrayOf("", "thousand", "million", "billion", "trillion")
    }
}
