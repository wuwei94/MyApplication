package com.example.william.my.module.ml.tts

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * 语音合成纯函数单元测试
 *
 * 覆盖两块不依赖 Android 运行时、但静默出错代价很高的逻辑：
 * - [SentenceChunker] 决定**流式粒度**，切错会让韵律走样或把整段塞进一句；
 * - [KittenVoiceTable] 解的是 `.npz`（zip + npy 头），解析偏移错了不会抛异常，
 *   只会把音色表整体读歪。
 */
class TtsSynthesisTest {

    // ─────────────────── SentenceChunker ───────────────────

    @Test
    fun chunker_splitByPunctuationAppendComma() {
        val chunks = SentenceChunker.chunk("Hi there! I am tiny. Are you there?")

        assertArrayEquals(arrayOf("Hi there,", "I am tiny,", "Are you there,"), chunks.toTypedArray())
    }

    @Test
    fun chunker_noDuplicateComma() {
        val chunks = SentenceChunker.chunk("Hello, world;")

        assertArrayEquals(arrayOf("Hello, world;"), chunks.toTypedArray())
    }

    @Test
    fun chunker_emptyAndBlankNoChunks() {
        assertTrue(SentenceChunker.chunk("").isEmpty())
        assertTrue(SentenceChunker.chunk("   \n\t ").isEmpty())
        assertTrue(SentenceChunker.chunk("...").isEmpty())
    }

    @Test
    fun chunker_longSentenceSplitByWordBoundary() {
        val word = "abcdefghij"
        val text = (1..100).joinToString(" ") { word }

        val chunks = SentenceChunker.chunk(text)

        assertTrue("应切成多块，实际 ${chunks.size}", chunks.size > 1)
        chunks.forEach {
            assertTrue("块长 ${it.length} 超过 400", it.length <= MAX_CHUNK_CHARS)
            assertTrue("块应以逗号收尾: $it", it.endsWith(","))
        }
        // 切分只丢分隔空格，不丢任何字母
        assertEquals(100 * word.length, chunks.joinToString(" ").count { it.isLetter() })
    }

    // ─────────────────── KittenVoiceTable ───────────────────

    @Test
    fun voiceTable_orderByVoices() {
        val table = KittenVoiceTable.read(ByteArrayInputStream(voiceNpz()))

        assertEquals(KittenVoiceTable.VOICES.size * KittenVoiceTable.STYLE_ROWS * KittenVoiceTable.STYLE_DIM, table.size)
        // 第 i 个音色的整块都填 i，用来确认块序与块起点
        KittenVoiceTable.VOICES.indices.forEach { voice ->
            val offset = voice * KittenVoiceTable.STYLE_ROWS * KittenVoiceTable.STYLE_DIM
            assertEquals(voice.toFloat(), table[offset], 0f)
            assertEquals(voice.toFloat(), table[offset + KittenVoiceTable.STYLE_ROWS * KittenVoiceTable.STYLE_DIM - 1], 0f)
        }
    }

    @Test
    fun styleFor_rowByTextLengthClamp() {
        val table = FloatArray(KittenVoiceTable.VOICES.size * KittenVoiceTable.STYLE_ROWS * KittenVoiceTable.STYLE_DIM) {
            it.toFloat()
        }
        val out = FloatArray(KittenVoiceTable.STYLE_DIM)

        KittenVoiceTable.styleFor(table, voice = 0, textLength = 3, out = out)
        assertEquals(3 * KittenVoiceTable.STYLE_DIM.toFloat(), out[0], 0f)

        KittenVoiceTable.styleFor(table, voice = 2, textLength = 7, out = out)
        val base = 2 * KittenVoiceTable.STYLE_ROWS * KittenVoiceTable.STYLE_DIM + 7 * KittenVoiceTable.STYLE_DIM
        assertEquals(base.toFloat(), out[0], 0f)

        // 文本长度超过表长 -> 取最后一行
        KittenVoiceTable.styleFor(table, voice = 0, textLength = 10_000, out = out)
        assertEquals(((KittenVoiceTable.STYLE_ROWS - 1) * KittenVoiceTable.STYLE_DIM).toFloat(), out[0], 0f)
    }

    @Test
    fun voiceTable_missingEntryThrows() {
        val incomplete = voiceNpz(skip = "expr-voice-5-m")

        val failure = runCatching { KittenVoiceTable.read(ByteArrayInputStream(incomplete)) }.exceptionOrNull()

        assertTrue("应抛异常，实际 $failure", failure is IllegalStateException)
        assertTrue(failure!!.message.orEmpty().contains("expr-voice-5-m"))
    }

    // ─────────────────── 测试数据 ───────────────────

    /** 造一份 `voices.npz`：每个 `npy` 都是 `[400, 256]` 小端 float32，整块填成该音色的下标 */
    private fun voiceNpz(skip: String? = null): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            KittenVoiceTable.VOICES.forEachIndexed { index, voice ->
                if (voice == skip) return@forEachIndexed
                val values =
                    FloatArray(KittenVoiceTable.STYLE_ROWS * KittenVoiceTable.STYLE_DIM) { index.toFloat() }
                zip.putNextEntry(ZipEntry("$voice.npy"))
                zip.write(npyBytes(values))
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    /** 按 `.npy` v1.0 规范打包：magic + 版本 + uint16 头长 + 头 + 裸数据 */
    private fun npyBytes(values: FloatArray): ByteArray {
        var header = "{'descr': '<f4', 'fortran_order': False, 'shape': " +
            "(${KittenVoiceTable.STYLE_ROWS}, ${KittenVoiceTable.STYLE_DIM}), }"
        // 规范要求 (magic + version + 头长字段 + 头) 补齐到 64 字节，且头以 '\n' 结尾
        val padding = (64 - (10 + header.length + 1) % 64) % 64
        header = header + " ".repeat(padding) + "\n"
        val headerBytes = header.toByteArray(Charsets.US_ASCII)

        val bytes = ByteArray(10 + headerBytes.size + values.size * 4)
        bytes[0] = 0x93.toByte()
        "NUMPY".toByteArray(Charsets.US_ASCII).copyInto(bytes, 1)
        bytes[6] = 1
        bytes[7] = 0
        bytes[8] = (headerBytes.size and 0xFF).toByte()
        bytes[9] = ((headerBytes.size shr 8) and 0xFF).toByte()
        headerBytes.copyInto(bytes, 10)
        val buffer = ByteBuffer.wrap(bytes, 10 + headerBytes.size, values.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        values.forEach { buffer.putFloat(it) }
        return bytes
    }

    private companion object {
        const val MAX_CHUNK_CHARS = 400
    }
}
