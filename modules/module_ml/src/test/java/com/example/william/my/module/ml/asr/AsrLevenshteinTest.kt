package com.example.william.my.module.ml.asr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [LevenshteinTokenMerger] 的行为规格测试
 *
 * 逐条移植官方 `LevenshteinTokenMergerTest`（含真实文本用例）。这一块是整个 ASR 示例里
 * 唯一「宿主侧算法」而非「模型调用」的部分，也最容易在重构中被改坏，因此保留官方全部用例 ——
 * 重叠窗口的合并结果对「多一个空格 / 少确认一个词」都极其敏感。
 *
 * 与官方测试的唯一差异：`MockTokenizer` 实现的是本模块的 [Tokenizer] 接口
 * （只有 `vocabSize` + `decode`，没有官方的 JNI 依赖）。
 */
class AsrLevenshteinTest {

    /** 逐字符建词表：每个字符一个 id，`addSpace` 造一个空格 token */
    class MockTokenizer(override val vocabSize: Int = 100) : Tokenizer {

        private val tokenMap = mutableMapOf<Int, String>()
        private var nextId = 1

        fun addWord(word: String): IntArray {
            val ids = mutableListOf<Int>()
            for (char in word) {
                val id = nextId++
                tokenMap[id] = char.toString()
                ids.add(id)
            }
            return ids.toIntArray()
        }

        fun addSpace(): IntArray {
            val id = nextId++
            tokenMap[id] = " "
            return intArrayOf(id)
        }

        override fun decode(tokenIds: IntArray): String = tokenIds.joinToString("") { tokenMap[it] ?: "" }

        override fun close() = Unit
    }

    /** 把整条 token 序列喂进去，返回「确认 + 未确认」的最后状态 */
    private fun decodeAll(
        merger: LevenshteinTokenMerger,
        tokens: IntArray,
        decodeEndOfSequence: Boolean = true,
        noTimestamps: Boolean = true,
        startTimestamp: Int = 0,
    ): Postprocessor.DecodedText {
        val confirmed = StringBuilder()
        var lastUnconfirmed = ""
        for ((i, t) in tokens.withIndex()) {
            val result =
                merger.decode(t, if (noTimestamps) SpeechRecognizer.NO_TIMESTAMP else startTimestamp + i)
            if (result != null) {
                if (result.confirmedText.isNotEmpty()) {
                    if (confirmed.isNotEmpty()) confirmed.append(" ")
                    confirmed.append(result.confirmedText)
                }
                lastUnconfirmed = result.unconfirmedText
            }
        }
        if (decodeEndOfSequence) {
            val finalResult =
                merger.decode(
                    SpeechRecognizer.END_OF_SEQUENCE,
                    if (noTimestamps) SpeechRecognizer.NO_TIMESTAMP else startTimestamp + tokens.size,
                )
            if (finalResult != null) {
                if (finalResult.confirmedText.isNotEmpty()) {
                    if (confirmed.isNotEmpty()) confirmed.append(" ")
                    confirmed.append(finalResult.confirmedText)
                }
                if (finalResult.unconfirmedText.isNotEmpty()) {
                    lastUnconfirmed = finalResult.unconfirmedText
                }
            }
        }
        return Postprocessor.DecodedText(confirmed.toString(), lastUnconfirmed)
    }

    private fun addWords(tokenizer: MockTokenizer, text: String): IntArray {
        val tokens = mutableListOf<Int>()
        val words = text.split(" ")
        for ((index, word) in words.withIndex()) {
            tokens.addAll(tokenizer.addWord(word).toList())
            if (index < words.size - 1) {
                tokens.addAll(tokenizer.addSpace().toList())
            }
        }
        return tokens.toIntArray()
    }

    // ─────────────────── decode：窗口合并 ───────────────────

    @Test
    fun testDecode_noOverlap() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.0f)

        val tokens1 = tokenizer.addWord("hello") + tokenizer.addSpace() + tokenizer.addWord("world")
        val (confirmed, unconfirmed) = decodeAll(merger, tokens1)

        assertEquals("", confirmed)
        assertEquals("hello world", unconfirmed)
    }

    @Test
    fun testDecode_withOverlap_perfectAlign() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.5f)

        val tokens1 =
            tokenizer.addWord("hello") +
                tokenizer.addSpace() +
                tokenizer.addWord("world") +
                tokenizer.addSpace() +
                tokenizer.addWord("how")
        val (conf1, unconf1) = decodeAll(merger, tokens1)
        assertEquals("", conf1)
        assertEquals("hello world how", unconf1)

        val tokens2 =
            tokenizer.addWord("world") +
                tokenizer.addSpace() +
                tokenizer.addWord("how") +
                tokenizer.addSpace() +
                tokenizer.addWord("are")
        val (conf2, unconf2) = decodeAll(merger, tokens2)
        assertEquals("hello", conf2)
        assertEquals("world how are", unconf2)
    }

    @Test
    fun testDecode_withOverlap_customPivotFactor() {
        val tokenizer = MockTokenizer()
        val merger =
            LevenshteinTokenMerger(tokenizer, overlapRatio = 0.5f, searchWindow = 2, pivotFactor = 0.2f)

        decodeAll(
            merger,
            tokenizer.addWord("hello") +
                tokenizer.addSpace() +
                tokenizer.addWord("world") +
                tokenizer.addSpace() +
                tokenizer.addWord("how"),
        )
        val (conf2, unconf2) =
            decodeAll(
                merger,
                tokenizer.addWord("world") +
                    tokenizer.addSpace() +
                    tokenizer.addWord("how") +
                    tokenizer.addSpace() +
                    tokenizer.addWord("are"),
            )
        assertEquals("hello", conf2)
        assertEquals("world how are", unconf2)
    }

    @Test
    fun testDecode_withOverlap_customSearchWindow() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.5f, searchWindow = 5)

        decodeAll(
            merger,
            tokenizer.addWord("hello") +
                tokenizer.addSpace() +
                tokenizer.addWord("world") +
                tokenizer.addSpace() +
                tokenizer.addWord("how"),
        )
        val (conf2, unconf2) =
            decodeAll(
                merger,
                tokenizer.addWord("world") +
                    tokenizer.addSpace() +
                    tokenizer.addWord("how") +
                    tokenizer.addSpace() +
                    tokenizer.addWord("are"),
            )
        assertEquals("hello", conf2)
        assertEquals("world how are", unconf2)
    }

    @Test
    fun testDecode_actualRepeatedWord_notDeduped() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.5f, searchWindow = 2)

        decodeAll(
            merger,
            tokenizer.addWord("hello") +
                tokenizer.addSpace() +
                tokenizer.addWord("world") +
                tokenizer.addSpace() +
                tokenizer.addWord("how"),
        )
        val (conf2, unconf2) =
            decodeAll(
                merger,
                tokenizer.addWord("world") +
                    tokenizer.addSpace() +
                    tokenizer.addWord("how") +
                    tokenizer.addSpace() +
                    tokenizer.addWord("how") +
                    tokenizer.addSpace() +
                    tokenizer.addWord("are"),
            )
        assertEquals("hello", conf2)
        assertEquals("world how how are", unconf2)
    }

    @Test
    fun testDecode_realWorldExampleBeforeEndOfSequence() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.8f)

        val text1 = "Well, I don't wish to see it any more, observed Phoebe, turning away her eyes."
        val text2 = "Wish to see it anymore, observed Phoebe, turning"

        val (conf1, unconf1) = decodeAll(merger, addWords(tokenizer, text1))
        val (conf2, unconf2) = decodeAll(merger, addWords(tokenizer, text2), decodeEndOfSequence = false)

        val ideal = "Well, I don't wish to see it any more, observed Phoebe, turning away her eyes."
        val actual = listOf(conf1, unconf1, conf2, unconf2).filter { it.isNotEmpty() }.joinToString(" ")
        assertEquals(ideal, actual)
    }

    @Test
    fun testDecode_realWorldExampleWithEndOfSequence() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.8f)

        val text1 = "Well, I don't wish to see it any more, observed Phoebe, turning away her eyes."
        val text2 =
            "Wish to see it anymore, observed Phoebe, turning away her eyes. It is certainly very, very."
        val text3 =
            "Or observe Phoebe, turning away her eyes. It is certainly very like the old portrait."

        val (conf1, unconf1) = decodeAll(merger, addWords(tokenizer, text1))
        val (conf2, unconf2) = decodeAll(merger, addWords(tokenizer, text2))

        assertEquals(
            "Well, I don't wish to see it any more, observed Phoebe, turning away her eyes. It is " +
                "certainly very, very.",
            listOf(conf1, conf2, unconf2).filter { it.isNotEmpty() }.joinToString(" "),
        )

        val (conf3, unconf3) = decodeAll(merger, addWords(tokenizer, text3))
        assertEquals(
            "Well, I don't wish to see it any more, observed Phoebe, turning away her eyes. It is " +
                "certainly very like the old portrait.",
            listOf(conf1, conf2, conf3, unconf3).filter { it.isNotEmpty() }.joinToString(" "),
        )
    }

    @Test
    fun testDecode_incrementalOutput() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.5f)

        val (conf1, unconf1) =
            decodeAll(
                merger,
                tokenizer.addWord("hello") +
                    tokenizer.addSpace() +
                    tokenizer.addWord("world") +
                    tokenizer.addSpace() +
                    tokenizer.addWord("how"),
            )
        assertEquals("", conf1)
        assertEquals("hello world how", unconf1)

        val part1 =
            tokenizer.addWord("world") +
                tokenizer.addSpace() +
                tokenizer.addWord("how") +
                tokenizer.addSpace()
        var confirmed = ""
        for (t in part1) {
            val res = merger.decode(t, SpeechRecognizer.NO_TIMESTAMP)
            if (res != null && res.confirmedText.isNotEmpty()) {
                confirmed = res.confirmedText
            }
        }
        assertEquals("hello", confirmed)

        for (t in tokenizer.addWord("are")) {
            val res = merger.decode(t, SpeechRecognizer.NO_TIMESTAMP)
            if (res != null && res.confirmedText.isNotEmpty()) {
                confirmed = res.confirmedText
            }
        }
        assertEquals("hello", confirmed)

        assertTrue(merger.decode(SpeechRecognizer.END_OF_SEQUENCE, SpeechRecognizer.NO_TIMESTAMP) == null)
    }

    @Test
    fun testDecode_pivotReachesPrevWordsSize() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.5f)

        decodeAll(merger, tokenizer.addWord("hello") + tokenizer.addSpace() + tokenizer.addWord("world"))
        val (conf2, unconf2) =
            decodeAll(merger, tokenizer.addWord("world") + tokenizer.addSpace() + tokenizer.addWord("are"))

        assertEquals("hello", conf2)
        assertEquals("world are", unconf2)
    }

    @Test
    fun testDecode_silenceConfirmAllPreviousWords() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.5f)

        decodeAll(merger, tokenizer.addWord("hello") + tokenizer.addSpace() + tokenizer.addWord("world"))
        assertNotNull(merger.decode(SpeechRecognizer.END_OF_SEQUENCE, SpeechRecognizer.NO_TIMESTAMP))
    }

    @Test
    fun testDecode_removeDuplicatesWithAlreadyConfirmedText() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.5f)

        decodeAll(merger, tokenizer.addWord("hello") + tokenizer.addSpace() + tokenizer.addWord("world"))
        merger.decode(SpeechRecognizer.END_OF_SEQUENCE, SpeechRecognizer.NO_TIMESTAMP)

        val (conf, unconf) =
            decodeAll(merger, tokenizer.addWord("world") + tokenizer.addSpace() + tokenizer.addWord("how"))
        assertEquals("", conf)
        assertEquals("how", unconf)
    }

    // ─────────────────── decode：编辑距离阈值 ───────────────────

    @Test
    fun testDecode_levenshteinDistance_belowThreshold() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.5f)

        decodeAll(
            merger,
            tokenizer.addWord("hello") +
                tokenizer.addSpace() +
                tokenizer.addWord("world") +
                tokenizer.addSpace() +
                tokenizer.addWord("how"),
        )
        val (conf2, unconf2) =
            decodeAll(merger, tokenizer.addWord("word") + tokenizer.addSpace() + tokenizer.addWord("are"))
        assertEquals("hello world", conf2)
        assertEquals("how word are", unconf2)
    }

    @Test
    fun testDecode_levenshteinDistance_aboveOrEqualThreshold() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.5f)

        decodeAll(
            merger,
            tokenizer.addWord("hello") +
                tokenizer.addSpace() +
                tokenizer.addWord("world") +
                tokenizer.addSpace() +
                tokenizer.addWord("how"),
        )
        val (conf2, unconf2) =
            decodeAll(merger, tokenizer.addWord("abcde") + tokenizer.addSpace() + tokenizer.addWord("are"))
        assertEquals("hello world how", conf2)
        assertEquals("abcde are", unconf2)
    }

    // ─────────────────── decode：带时间戳（TDT / CTC 路径） ───────────────────

    @Test
    fun testDecode_withTimestamps() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.7f, searchWindow = 0)

        val (conf1, unconf1) =
            decodeAll(
                merger,
                tokenizer.addWord("hello") + tokenizer.addSpace() + tokenizer.addWord("world"),
                noTimestamps = false,
            )
        assertEquals("", conf1)
        assertEquals("hello world", unconf1)

        val (conf2, unconf2) =
            decodeAll(
                merger,
                tokenizer.addWord("world") + tokenizer.addSpace() + tokenizer.addWord("how"),
                noTimestamps = false,
            )
        assertEquals("hello", conf2)
        assertEquals("world how", unconf2)
    }

    @Test
    fun testDecode_withTimestamps_dropUnmatchedTokens() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.7f, searchWindow = 0)

        decodeAll(
            merger,
            tokenizer.addWord("hello") + tokenizer.addSpace() + tokenizer.addWord("world"),
            noTimestamps = false,
        )
        val (conf2, unconf2) =
            decodeAll(
                merger,
                tokenizer.addWord("unmatched") + tokenizer.addSpace() + tokenizer.addWord("how"),
                noTimestamps = false,
            )
        assertEquals("hello world", conf2)
        assertEquals("how", unconf2)
    }

    @Test
    fun testDecode_withTimestampsWithLessOverlap_keepUnmatchedTokens() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.3f, searchWindow = 0)

        decodeAll(
            merger,
            tokenizer.addWord("hello") + tokenizer.addSpace() + tokenizer.addWord("world"),
            noTimestamps = false,
        )
        val (conf2, unconf2) =
            decodeAll(
                merger,
                tokenizer.addWord("unmatched") + tokenizer.addSpace() + tokenizer.addWord("how"),
                noTimestamps = false,
            )
        assertEquals("hello world", conf2)
        assertEquals("unmatched how", unconf2)
    }

    @Test
    fun testDecode_withTimestamps_searchWindowFindsShiftedWords() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.5f, searchWindow = 1)

        decodeAll(
            merger,
            tokenizer.addWord("hello") + tokenizer.addSpace() + tokenizer.addWord("world"),
            noTimestamps = false,
        )
        val (conf2, unconf2) =
            decodeAll(
                merger,
                tokenizer.addWord("world") + tokenizer.addSpace() + tokenizer.addWord("how"),
                noTimestamps = false,
            )
        assertEquals("hello", conf2)
        assertEquals("world how", unconf2)
    }

    @Test
    fun testDecode_withTimestamps_multipleConsecutiveSequences() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.5f, searchWindow = 0)

        val (conf1, unconf1) =
            decodeAll(
                merger,
                tokenizer.addWord("one") + tokenizer.addSpace() + tokenizer.addWord("two"),
                noTimestamps = false,
            )
        assertEquals("", conf1)
        assertEquals("one two", unconf1)

        val (conf2, unconf2) =
            decodeAll(
                merger,
                tokenizer.addWord("two") + tokenizer.addSpace() + tokenizer.addWord("three"),
                noTimestamps = false,
            )
        assertEquals("one", conf2)
        assertEquals("two three", unconf2)

        val (conf3, unconf3) =
            decodeAll(
                merger,
                tokenizer.addWord("three") + tokenizer.addSpace() + tokenizer.addWord("four"),
                noTimestamps = false,
            )
        assertEquals("two", conf3)
        assertEquals("three four", unconf3)
    }

    @Test
    fun testDecode_withTimestamps_shortCurrentSequence_returnsNull() {
        val tokenizer = MockTokenizer()
        val merger = LevenshteinTokenMerger(tokenizer, overlapRatio = 0.5f)

        decodeAll(
            merger,
            tokenizer.addWord("hello") + tokenizer.addSpace() + tokenizer.addWord("world"),
            noTimestamps = false,
            startTimestamp = 0,
        )
        // 时间戳落后于上一窗口 → 本窗口整段被丢弃
        val (conf2, unconf2) =
            decodeAll(
                merger,
                tokenizer.addWord("world") + tokenizer.addSpace() + tokenizer.addWord("how"),
                decodeEndOfSequence = false,
                noTimestamps = false,
                startTimestamp = -20,
            )
        assertEquals("", conf2)
        assertEquals("", unconf2)
    }

    // ─────────────────── companion 纯函数 ───────────────────

    @Test
    fun testMergeIntoUnconfirmedText_noOverlap() {
        val result =
            LevenshteinTokenMerger.mergeIntoUnconfirmedText(
                prevWords = listOf("hello"),
                prevWordTimestamps = emptyList(),
                currentWords = listOf("world"),
                currentWordTimestamps = emptyList(),
                overlapRatio = 0.5f,
                pivotFactor = 0.5f,
                prevWordIndexOfUnconfirmed = 0,
                prevWordIndexOfPivot = 0,
            )
        assertEquals(listOf("world"), result)
    }

    @Test
    fun testMergeIntoUnconfirmedText_perfectOverlap() {
        val result =
            LevenshteinTokenMerger.mergeIntoUnconfirmedText(
                prevWords = listOf("hello", "world"),
                prevWordTimestamps = emptyList(),
                currentWords = listOf("world", "how"),
                currentWordTimestamps = emptyList(),
                overlapRatio = 0.5f,
                pivotFactor = 0.5f,
                prevWordIndexOfUnconfirmed = 1,
                prevWordIndexOfPivot = 1,
            )
        assertEquals(listOf("world", "how"), result)
    }

    @Test
    fun testMergeIntoUnconfirmedText_shortCurrentWords() {
        val result =
            LevenshteinTokenMerger.mergeIntoUnconfirmedText(
                prevWords = "Well, I don't wish to see it any more, observed Phoebe, turning away her eyes.".split(" "),
                prevWordTimestamps = emptyList(),
                currentWords = "Wish to see it anymore, observed Phoebe, turning".split(" "),
                currentWordTimestamps = emptyList(),
                overlapRatio = 0.8f,
                pivotFactor = 0.6f,
                prevWordIndexOfUnconfirmed = 3,
                prevWordIndexOfPivot = 10,
            )
        assertEquals(
            "wish to see it any more, observed Phoebe, turning away her eyes.".split(" "),
            result,
        )
    }

    @Test
    fun testMergeIntoUnconfirmedText_fullCurrentWords() {
        val result =
            LevenshteinTokenMerger.mergeIntoUnconfirmedText(
                prevWords = "Well, I don't wish to see it any more, observed Phoebe, turning away her eyes.".split(" "),
                currentWords =
                "Wish to see it anymore, observed Phoebe, turning away her eyes. It is certainly very, very."
                    .split(" "),
                prevWordTimestamps = emptyList(),
                currentWordTimestamps = emptyList(),
                overlapRatio = 0.8f,
                pivotFactor = 0.6f,
                prevWordIndexOfUnconfirmed = 3,
                prevWordIndexOfPivot = 10,
            )
        assertEquals(
            "wish to see it any more, observed turning away her eyes. It is certainly very, very."
                .split(" "),
            result,
        )
    }

    @Test
    fun testMergeIntoUnconfirmedText_withTimestamps_shiftsPivotWordsDynamically() {
        val result =
            LevenshteinTokenMerger.mergeIntoUnconfirmedText(
                prevWords = listOf("a", "b", "c", "d", "e"),
                prevWordTimestamps = listOf(10, 20, 30, 40, 50),
                currentWords = listOf("c", "d", "e", "f", "g", "h"),
                currentWordTimestamps = listOf(25, 26, 27, 28, 45, 55),
                overlapRatio = 0.5f,
                pivotFactor = 0.5f,
                prevWordIndexOfUnconfirmed = 2,
                prevWordIndexOfPivot = 3,
            )
        assertEquals(listOf("c", "g", "h"), result)
    }

    @Test
    fun testMergeIntoUnconfirmedText_withTimestamps_pivotExceedsPrevWordsSize() {
        val result =
            LevenshteinTokenMerger.mergeIntoUnconfirmedText(
                prevWords = listOf("foo", "bar"),
                prevWordTimestamps = listOf(0, 100),
                currentWords = listOf("bar", "baz", "qux"),
                currentWordTimestamps = listOf(100, 101, 150),
                overlapRatio = 0.5f,
                pivotFactor = 0.5f,
                prevWordIndexOfUnconfirmed = 1,
                prevWordIndexOfPivot = 2,
            )
        assertEquals(listOf("bar", "baz", "qux"), result)
    }

    @Test
    fun testDedupWords() {
        assertEquals(listOf("world"), LevenshteinTokenMerger.dedupWords(listOf("hello"), listOf("world")))
        assertEquals(
            listOf("how"),
            LevenshteinTokenMerger.dedupWords(listOf("hello", "world"), listOf("world", "how")),
        )
        assertEquals(
            listOf("are"),
            LevenshteinTokenMerger.dedupWords(listOf("hello", "world", "how"), listOf("world", "how", "are")),
        )
    }

    @Test
    fun testCanonicalize() {
        assertEquals("", LevenshteinTokenMerger.canonicalize(emptyList()))
        assertEquals("hello world", LevenshteinTokenMerger.canonicalize(listOf("Hello", "World")))
        assertEquals("hello world", LevenshteinTokenMerger.canonicalize(listOf("Hello,", "world!")))
    }

    @Test
    fun testCloseEnough() {
        assertTrue(LevenshteinTokenMerger.closeEnough("the", "the"))
        assertTrue(LevenshteinTokenMerger.closeEnough("hello", "hello"))
        assertFalse(LevenshteinTokenMerger.closeEnough("the", "thy"))
        assertTrue(LevenshteinTokenMerger.closeEnough("hello", "hellp"))
        assertFalse(LevenshteinTokenMerger.closeEnough("hello", "helpp"))
        assertTrue(LevenshteinTokenMerger.closeEnough("hello", "hello world"))

        assertTrue(LevenshteinTokenMerger.closeEnough(listOf("hello", "world"), listOf("hello", "world")))
        assertFalse(LevenshteinTokenMerger.closeEnough(listOf("hello"), listOf("hello", "world")))
        assertTrue(LevenshteinTokenMerger.closeEnough(listOf("hello", "world"), listOf("hellp", "world")))
    }

    @Test
    fun testLevenshtein() {
        assertEquals(0, LevenshteinTokenMerger.levenshtein("abc", "abc"))
        assertEquals(3, LevenshteinTokenMerger.levenshtein("", "abc"))
        assertEquals(1, LevenshteinTokenMerger.levenshtein("cat", "cats"))
        assertEquals(1, LevenshteinTokenMerger.levenshtein("kitten", "sitten"))
        assertEquals(3, LevenshteinTokenMerger.levenshtein("kitten", "sitting"))
    }
}
