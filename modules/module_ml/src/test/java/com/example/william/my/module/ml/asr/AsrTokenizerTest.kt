package com.example.william.my.module.ml.asr

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [HfTokenizer] 的正确性与覆盖面。
 *
 * 主力用例跑的是**真分词器**：`tokenizer_golden.json` 里的「id 序列 → 文本」由真
 * HF `tokenizers` 库产出，配套的 `tokenizer_<模型>.json` 是从真实
 * `tokenizer.json` 里**原样搬运 decoder 链**、只裁剪了词表的夹具（几百字节）。
 * 也就是说这里验的不是我自己的理解，而是上游实现的口径。
 *
 * 合成用例的 JSON 一律用 `$变量` 把真实字符插进去：Kotlin 的原始字符串
 * （`"""…"""`）**不做 Unicode 转义**，直接写「反斜杠 + u + 四位十六进制」只会得到字面反斜杠。
 * （顺带记一条工程约定：**注释里也不要写这个转义**，kapt 会把 KDoc 抄进生成的 Java stub，
 * javac 再把它当成非法的 Unicode 转义而编译失败。）
 */
class AsrTokenizerTest {

    @Test
    fun decode_matchesHfForSixRealTokenizers() {
        val golden = JSONObject(readResource("/asr/tokenizer_golden.json"))
        var checkedCases = 0
        for (key in golden.keys()) {
            val entry = golden.getJSONObject(key)
            val tokenizer = HfTokenizer.fromJson(readResource("/asr/tokenizer_$key.json"))
            val cases = entry.getJSONArray("cases")
            assertTrue("$key 没有用例", cases.length() > 0)
            for (i in 0 until cases.length()) {
                val item = cases.getJSONObject(i)
                val ids = item.getJSONArray("ids").let { IntArray(it.length()) { index -> it.getInt(index) } }
                assertEquals("$key 第 ${i + 1} 条解码不一致", item.getString("text"), tokenizer.decode(ids))
                checkedCases++
            }
        }
        assertTrue("用例太少，说明 golden 没被真正读取：$checkedCases", checkedCases >= 30)
    }

    @Test
    fun vocabSize_maxIdPlusOne() {
        val tokenizer = HfTokenizer.fromJson(
            """{"model":{"type":"BPE","vocab":{"a":0,"b":5}},"added_tokens":[{"id":9,"content":"<s>","special":true}]}""",
        )
        assertEquals(10, tokenizer.vocabSize)
    }

    @Test
    fun vocab_unigramArrayIndexAsId() {
        val tokenizer = HfTokenizer.fromJson(
            """{"model":{"type":"Unigram","vocab":[["<unk>",0.0],["a",-1.0],["b",-2.5]]},"decoder":{"type":"Fuse"}}""",
        )
        assertEquals(3, tokenizer.vocabSize)
        assertEquals("ab", tokenizer.decode(intArrayOf(1, 2)))
        // 0 是 <unk>，但没被标成 special，因此仍会出现在结果里
        assertEquals("<unk>ab", tokenizer.decode(intArrayOf(0, 1, 2)))
    }

    @Test
    fun vocab_specialTokensSkipped() {
        val tokenizer = HfTokenizer.fromJson(
            """{"model":{"type":"BPE","vocab":{"a":0,"b":1,"<s>":2,"</s>":3}},""" +
                """"added_tokens":[{"id":2,"content":"<s>","special":true},""" +
                """{"id":3,"content":"</s>","special":false}]}""",
        )
        // special=true 的 <s> 被丢弃；special=false 的 </s> 保留
        assertEquals("ab</s>", tokenizer.decode(intArrayOf(2, 0, 1, 3)))
    }

    @Test
    fun byteLevel_gRestoresSpace() {
        // GPT-2 字节表里空格 0x20 不在可打印区间，被映射成 U+0120
        val space = '\u0120'
        val tokenizer = HfTokenizer.fromJson(
            """{"model":{"type":"BPE","vocab":{"hello":0,"$space":1,"world":2}},"decoder":{"type":"ByteLevel"}}""",
        )
        assertEquals("hello world", tokenizer.decode(intArrayOf(0, 1, 2)))
    }

    @Test
    fun byteLevel_multiByteRoundTrip() {
        // U+00E9（é）的 UTF-8 是 C3 A9，对应字节表里的 U+00C3 / U+00A9。
        // 与 tokenizers 逐词解码不同，两个词合起来这里仍能还原成 é（刻意差异，见实现 KDoc）。
        val first = '\u00C3'
        val second = '\u00A9'
        val tokenizer = HfTokenizer.fromJson(
            """{"model":{"type":"BPE","vocab":{"$first":0,"$second":1}},"decoder":{"type":"ByteLevel"}}""",
        )
        assertEquals("\u00E9", tokenizer.decode(intArrayOf(0, 1)))
    }

    @Test
    fun metaspace_underscoreToSpace() {
        val marker = '\u2581'
        val tokenizer = HfTokenizer.fromJson(
            """{"model":{"type":"BPE","vocab":{"${marker}hello":0,"${marker}world":1,"!":2}},""" +
                """"decoder":{"type":"Metaspace","replacement":"$marker","prepend_scheme":"always"}}""",
        )
        assertEquals("hello world!", tokenizer.decode(intArrayOf(0, 1, 2)))
    }

    @Test
    fun metaspace_prependScheme() {
        // 直接对解码链环节断言：走到 decode() 的话，结尾的 trim() 会把两种 scheme 抹成一样，
        // 这个用例就变成恒真的了。
        val marker = '\u2581'
        val token = "${marker}hi"
        assertEquals(
            listOf("hi"),
            HfTokenizer.Step.Metaspace(marker.toString(), "always").apply(listOf(token)),
        )
        assertEquals(
            listOf(" hi"),
            HfTokenizer.Step.Metaspace(marker.toString(), "never").apply(listOf(token)),
        )
    }

    @Test
    fun strip_directional() {
        val headOnly = HfTokenizer.Step.Strip(" ", start = true, stop = false)
        assertEquals(listOf("x  "), headOnly.apply(listOf("  x  ")))
        val tailOnly = HfTokenizer.Step.Strip(" ", start = false, stop = true)
        assertEquals(listOf("  x"), tailOnly.apply(listOf("  x  ")))
    }

    @Test
    fun byteFallback_restoresRawBytes() {
        // 「你」的 UTF-8 是 E4 BD A0
        val marker = '\u2581'
        val tokenizer = HfTokenizer.fromJson(
            """{"model":{"type":"BPE","vocab":{"$marker":0,"<0xE4>":1,"<0xBD>":2,"<0xA0>":3}},""" +
                """"decoder":{"type":"Sequence","decoders":[{"type":"Replace","pattern":{"String":"$marker"},""" +
                """"content":" "},{"type":"ByteFallback"},{"type":"Fuse"},""" +
                """{"type":"Strip","content":" ","start":1,"stop":0}]}}""",
        )
        assertEquals("\u4F60", tokenizer.decode(intArrayOf(0, 1, 2, 3)))
    }

    @Test
    fun decode_unknownTypeThrows() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            HfTokenizer.fromJson("""{"model":{"type":"BPE","vocab":{"a":0}},"decoder":{"type":"WordPiece"}}""")
        }
        assertTrue("异常信息应带上类型名：${error.message}", error.message.orEmpty().contains("WordPiece"))
    }

    @Test
    fun decode_missingIdIgnored() {
        val tokenizer = HfTokenizer.fromJson("""{"model":{"type":"BPE","vocab":{"a":0}},"decoder":{"type":"Fuse"}}""")
        assertEquals("a", tokenizer.decode(intArrayOf(0, 99, -1)))
    }

    private fun readResource(path: String): String = checkNotNull(javaClass.getResourceAsStream(path)) { "缺少测试资源：$path" }
        .bufferedReader()
        .use { it.readText() }
}
