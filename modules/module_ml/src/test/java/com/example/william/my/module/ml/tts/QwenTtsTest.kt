package com.example.william.my.module.ml.tts

import com.example.william.my.module.ml.download.RemoteModels
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Qwen3-TTS 纯函数单元测试
 *
 * 覆盖两块「静默出错代价极高」的逻辑：
 * - [Npy] 解的是宿主侧张量（`text_embedding_fp16.npy` 有 622 MB），偏移或 dtype 判错不会抛异常，
 *   只会让整条流水线算出一段听起来没问题、实际全错的音频；
 * - [QwenBpeTokenizer] 的预分词与合并规则来自 Qwen2 的 `tokenizer.json`，改错一处只会让
 *   个别文本悄悄编出错误 id。
 *
 * 分词器用**合成词表**测：真实的 `vocab.json` 有 2.7 MB、`text_embedding` 有 622 MB，都不该进单测；
 * 真词表由页面启动时的自检（`assets/tokenizer_test_vectors.json`）在设备上核对。
 */
class QwenTtsTest {

    // ─────────────────── Npy：.npy / .npz 解析 ───────────────────

    @Test
    fun npy_fp32ElementCountFromShape() {
        val bytes = npy(floatArrayOf(1.5f, -2.5f, 3.25f, 0f), "<f4", "(4,)")

        val values = Npy.floats(bytes)

        assertArrayEquals(floatArrayOf(1.5f, -2.5f, 3.25f, 0f), values, 0f)
    }

    @Test
    fun npy_fp16UpcastPerElement() {
        // 1.0 / -2.5 / 0.5 / 0 / 最小非规格化数 2^-24 / -inf
        val halves = shortArrayOf(
            0x3C00,
            0xC100.toShort(),
            0x3800,
            0x0000,
            0x0001,
            0xFC00.toShort(),
        )

        val values = Npy.floats(npyHalf(halves, "(6,)"))

        assertEquals(1.0f, values[0], 0f)
        assertEquals(-2.5f, values[1], 0f)
        assertEquals(0.5f, values[2], 0f)
        assertEquals(0.0f, values[3], 0f)
        assertEquals(5.9604645e-8f, values[4], 0f)
        assertEquals(Float.NEGATIVE_INFINITY, values[5], 0f)
    }

    @Test
    fun npy_truncatedDataThrows() {
        val full = npy(floatArrayOf(1f, 2f, 3f, 4f), "<f4", "(4,)")
        val truncated = full.copyOf(full.size - 8)

        val failure = runCatching { Npy.floats(truncated) }.exceptionOrNull()

        assertTrue("应抛异常，实际 $failure", failure is IllegalArgumentException)
        assertTrue(failure!!.message.orEmpty().contains("数据不足"))
    }

    @Test
    fun npy_unsupportedDtypeAndFortranThrow() {
        val int8 = npy(floatArrayOf(1f), "<i1", "(1,)")
        val fortran = npy(floatArrayOf(1f), "<f4", "(1,)", fortranOrder = true)

        assertTrue(runCatching { Npy.floats(int8) }.exceptionOrNull() is IllegalArgumentException)
        assertTrue(runCatching { Npy.floats(fortran) }.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun npz_getByNameMissingThrows() {
        val npz = npz(mapOf("w1" to floatArrayOf(1f, 2f), "b1" to floatArrayOf(3f)))

        val loaded = Npy.loadNpz(writeToTemp(npz), listOf("w1", "b1"))

        assertArrayEquals(floatArrayOf(1f, 2f), loaded.getValue("w1"), 0f)
        assertArrayEquals(floatArrayOf(3f), loaded.getValue("b1"), 0f)
        val failure = runCatching { Npy.loadNpz(writeToTemp(npz), listOf("w2")) }.exceptionOrNull()
        assertTrue("应抛异常，实际 $failure", failure is IllegalArgumentException)
    }

    // ─────────────────── QwenBpeTokenizer ───────────────────

    @Test
    fun bpe_byteMappingAndMergeChain() {
        // h+e -> he, l+l -> ll, he+ll -> hell, hell+o -> hello
        assertEquals(listOf(HELLO), tokenizer().encode("hello").toList())
    }

    @Test
    fun bpe_leadingSpaceMergesAsBytes() {
        // 预分词把 " hello" 整体交给 BPE（正则里 `[^\r\n\p{L}\p{N}]?\p{L}+` 吃掉前导空格），
        // 于是"空格"与 hello 各自成 piece。注意空格字节 0x20 不在 GPT-2 的可打印区间里，
        // 按 byte-level 字母表映射为 U+0120（Ġ），而不是字符 ' '。
        assertEquals(listOf(SPACE, HELLO, SPACE, HELLO), tokenizer().encode(" hello hello").toList())
    }

    @Test
    fun bpe_digitsSplitPerChar() {
        // Qwen2 预分词正则是 `\p{N}`（单字符）而非 `\p{N}+`
        assertEquals(listOf(ONE, TWO), tokenizer().encode("12").toList())
    }

    @Test
    fun bpe_nfcNormalizationSameIds() {
        val composed = tokenizer().encode("\u00E9") // é
        val decomposed = tokenizer().encode("e\u0301") // e + U+0301

        assertEquals(listOf(E_ACUTE), composed.toList())
        assertArrayEquals(composed, decomposed)
    }

    @Test
    fun bpe_unknownPieceInMergesThrows() {
        val failure = runCatching {
            QwenBpeTokenizer(VOCAB, listOf("h e", "l z"))
        }.exceptionOrNull()

        assertTrue("应抛异常，实际 $failure", failure is IllegalStateException)
        assertTrue(failure!!.message.orEmpty().contains("词表"))
        assertTrue(failure.message.orEmpty().contains("z"))
    }

    @Test
    fun selfCheck_compareReferenceVectors() {
        val correct = listOf("hello" to intArrayOf(HELLO), "it's" to intArrayOf(IT, APOS_S))
        assertTrue(tokenizer().findMismatches(correct).isEmpty())

        val wrong = correct + ("hello hello" to intArrayOf(SPACE, HELLO, SPACE, HELLO, HELLO))
        val mismatches = tokenizer().findMismatches(wrong)

        assertEquals(1, mismatches.size)
        assertTrue(mismatches.first().contains("hello hello"))
        assertNotEquals(0, tokenizer().vocabularySize)
    }

    // ─────────────────── Result 的派生指标 ───────────────────

    @Test
    fun rtf_pixel8aMeasured() {
        // 官方 README 的端到端实测：4.16 s 音频、prefill 585 ms、talker 3.1 s、MTP 19.2 s、codec 5.1 s
        val result = Qwen3TtsEngine.Result(
            audio = FloatArray((Qwen3TtsEngine.SAMPLE_RATE * 4.16).toInt()),
            frames = 52,
            prefillMs = 585,
            talkerMs = 3_100,
            mtpMs = 19_200,
            codecMs = 5_100,
        )

        assertEquals(27_985L, result.graphMs)
        assertEquals(4.16, result.audioSeconds, 1e-6)
        assertEquals(27.985 / 4.16, result.realTimeFactor, 1e-6)
        assertTrue("三图耗时明显大于音频时长 -> 慢于实时", result.realTimeFactor > 1.0)
    }

    // ─────────────────── 下载清单与引擎的一致性 ───────────────────

    @Test
    fun downloadManifest_matchesEngineFiles() {
        // 引擎按文件名拼路径，清单按文件名落盘；两边一旦漂移，页面只会在运行时才报「模型未就绪」
        val required = setOf(
            Qwen3TtsEngine.TALKER_FILE,
            Qwen3TtsEngine.MTP_FILE,
            Qwen3TtsEngine.CODEC_PART_A_FILE,
            Qwen3TtsEngine.CODEC_PART_B_FILE,
            Qwen3TtsEngine.CODEC_EMBEDDING_FILE,
            Qwen3TtsEngine.MTP_EMBEDDINGS_FILE,
            Qwen3TtsEngine.TEXT_EMBEDDING_FILE,
            Qwen3TtsEngine.TEXT_PROJECTION_FILE,
            Qwen3TtsEngine.SPEAKER_FILE,
            Qwen3TtsEngine.VOCAB_FILE,
            Qwen3TtsEngine.MERGES_FILE,
        )

        val downloaded = RemoteModels.QWEN.map { it.fileName }.toSet()

        assertEquals(required, downloaded)
        assertEquals(
            "官方脚本把 tables/ 与 voices/ 拍平到 filesDir，落盘名里不该再出现目录",
            emptyList<String>(),
            downloaded.filter { it.contains('/') },
        )
    }

    // ─────────────────── 测试数据 ───────────────────

    /** 合成词表：只含本文件用到的字节字母与合并后的 piece */
    private fun tokenizer(): QwenBpeTokenizer = QwenBpeTokenizer(
        VOCAB,
        listOf(
            "h e", // rank 0
            "l l", // rank 1
            "he ll", // rank 2
            "hell o", // rank 3
            "i t", // rank 4
            "' s", // rank 5
            "it 's", // rank 6
            "1 2", // rank 7：故意给出，用来验证预分词不会把 "12" 整体交给 BPE
            "\u00C3 \u00A9", // rank 8：é 的两个 UTF-8 字节
        ),
    )

    /** 按 `.npy` v1.0 规范打包 fp32：magic + 版本 + uint16 头长 + 头（补齐 64 字节）+ 裸数据 */
    private fun npy(values: FloatArray, descr: String, shape: String, fortranOrder: Boolean = false): ByteArray {
        // 头是 Python 的 repr，布尔要大写 True/False —— Kotlin 的 Boolean.toString() 是小写，不能直接用
        val fortran = if (fortranOrder) "True" else "False"
        val header = "{'descr': '$descr', 'fortran_order': $fortran, 'shape': $shape, }"
        val headerBytes = pad(header)
        val bytes = ByteArray(HEADER_START + headerBytes.size + values.size * 4)
        bytes[0] = 0x93.toByte()
        "NUMPY".toByteArray(Charsets.US_ASCII).copyInto(bytes, 1)
        bytes[6] = 1
        bytes[7] = 0
        bytes[8] = (headerBytes.size and 0xFF).toByte()
        bytes[9] = ((headerBytes.size shr 8) and 0xFF).toByte()
        headerBytes.copyInto(bytes, HEADER_START)
        val buffer = ByteBuffer.wrap(bytes, HEADER_START + headerBytes.size, values.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        values.forEach { buffer.putFloat(it) }
        return bytes
    }

    /** 同上，但裸数据是 fp16 */
    private fun npyHalf(values: ShortArray, shape: String): ByteArray {
        val headerBytes = pad("{'descr': '<f2', 'fortran_order': False, 'shape': $shape, }")
        val bytes = ByteArray(HEADER_START + headerBytes.size + values.size * 2)
        bytes[0] = 0x93.toByte()
        "NUMPY".toByteArray(Charsets.US_ASCII).copyInto(bytes, 1)
        bytes[6] = 1
        bytes[7] = 0
        bytes[8] = (headerBytes.size and 0xFF).toByte()
        bytes[9] = ((headerBytes.size shr 8) and 0xFF).toByte()
        headerBytes.copyInto(bytes, HEADER_START)
        val buffer = ByteBuffer.wrap(bytes, HEADER_START + headerBytes.size, values.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        values.forEach { buffer.putShort(it) }
        return bytes
    }

    private fun npz(entries: Map<String, FloatArray>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            entries.forEach { (name, values) ->
                zip.putNextEntry(ZipEntry("$name.npy"))
                zip.write(npy(values, "<f4", "(${values.size},)"))
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    private fun writeToTemp(bytes: ByteArray): java.io.File = java.io.File.createTempFile("qwen-test", ".npz").apply {
        deleteOnExit()
        writeBytes(bytes)
    }

    /** 规范要求 (magic + 版本 + 头长字段 + 头) 补齐到 64 字节，且头以换行收尾 */
    private fun pad(header: String): ByteArray {
        val padding = (64 - (HEADER_START + header.length + 1) % 64) % 64
        return (header + " ".repeat(padding) + "\n").toByteArray(Charsets.US_ASCII)
    }

    private companion object {
        const val HEADER_START = 10

        const val HELLO = 8
        const val SPACE = 4
        const val ONE = 40
        const val TWO = 41
        const val IT = 13
        const val APOS_S = 14
        const val E_ACUTE = 22

        /**
         * 合成词表
         *
         * 值为本文件用到的 piece。两处映射容易写错，值得记下来：
         * - 空格字节 `0x20` **不在** GPT-2 的可打印区间（`!`~`~` / `¡`~`¬` / `®`~`ÿ`）里，
         *   因此映射为 U+0120（Ġ）；
         * - `é` 的 UTF-8 字节是 `C3 A9`，两个字节都可打印，因此映射为它们自身 U+00C3 / U+00A9。
         */
        val VOCAB: Map<String, Int> = mapOf(
            "h" to 0,
            "e" to 1,
            "l" to 2,
            "o" to 3,
            "\u0120" to SPACE,
            "he" to 5,
            "ll" to 6,
            "hell" to 7,
            "hello" to HELLO,
            "i" to 9,
            "t" to 10,
            "'" to 11,
            "s" to 12,
            "it" to IT,
            "'s" to APOS_S,
            "\u00C3" to 20,
            "\u00A9" to 21,
            "\u00C3\u00A9" to E_ACUTE,
            "1" to ONE,
            "2" to TWO,
        )
    }
}
