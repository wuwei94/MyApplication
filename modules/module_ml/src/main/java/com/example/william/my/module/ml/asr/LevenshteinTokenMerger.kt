package com.example.william.my.module.ml.asr

import kotlin.math.ceil

/**
 * 用编辑距离对齐相邻重叠窗口、把文本合并成「已确认 + 未确认」两段。
 *
 * 官方 `LevenshteinTokenMerger` 的等价实现。这是整个 ASR 示例里最绕的一块，核心矛盾是：
 * 相邻两个 5 秒窗口有 2 秒重叠，重叠区必然被识别两遍，而**两次结果不会完全一样**。
 * 于是需要拿后一次的窗口去和上一次的文本对齐，判断「上一次的哪些词已经稳定下来」。
 *
 * 对齐结果切成三点：
 * - `[0, prevWordIndexOfUnconfirmed)` —— 已确认，可以被下一次输出替换掉（[lastConfirmedWords]）；
 * - `[prevWordIndexOfUnconfirmed, prevWordIndexOfPivot)` —— 未确认区里来自**上一次**的部分；
 * - 未确认区剩下的 —— 来自**当前**窗口，通常更准（新窗口对同一段音频看得更全）。
 *
 * 有两条对齐路径，按模型是否给时间戳二选一：
 * - 给时间戳（Parakeet-TDT / CTC）：直接按时间戳找重叠起点，点得很准；
 * - 不给（Moonshine / Whisper / Qwen3-ASR）：退化成「按比例猜一个中点 ± [searchWindow]，
 *   在这几个候选起点里挑编辑距离最小的」。
 *
 * 与官方的唯一差异：**去掉了逐 token 的 `Log.d`**，本模块其余页面也不打这类日志，
 * 免得一次转写把 logcat 刷满。逻辑本身逐行对齐。
 */
internal class LevenshteinTokenMerger(
    private val tokenizer: Tokenizer,
    private val overlapRatio: Float,
    private val searchWindow: Int = DEFAULT_SEARCH_WINDOW,
    private val maxLevenshteinDistance: Int = DEFAULT_MAX_DISTANCE,
    private val pivotFactor: Float = DEFAULT_PIVOT_FACTOR,
) : Postprocessor {

    private var prevWords = listOf<String>()

    /** 与 [prevWords] 同下标的时间戳（已按当前窗口的时间轴对齐） */
    private var prevWordTimestamps = listOf<Int>()

    /** [prevWords] 中第一个属于「未确认」的下标 */
    private var prevWordIndexOfUnconfirmed = -1

    /** 未确认区里「上一次」与「当前」的分界点 */
    private var prevWordIndexOfPivot = -1

    private val currentTokens = mutableListOf<Int>()
    private val currentWordTimestamps = mutableListOf<Int>()

    private var lastConfirmedWords = listOf<String>()

    override fun close() = Unit

    override fun decode(tokenId: Int, timestamp: Int): Postprocessor.DecodedText? {
        val isEndOfSequence = tokenId == SpeechRecognizer.END_OF_SEQUENCE
        if (!isEndOfSequence) {
            currentTokens.add(tokenId)
        }

        val currentWords = detokenize(currentTokens.toIntArray())
        if (timestamp != SpeechRecognizer.NO_TIMESTAMP) {
            // 新出现的词沿用当前 token 的时间戳（粗粒度近似，官方亦如此）
            while (currentWords.size > currentWordTimestamps.size) {
                currentWordTimestamps.add(timestamp)
            }
        }

        if (isEndOfSequence) {
            val result = getConfirmedAndUnconfirmedTextAtEndOfSequence(currentWords)
            if (timestamp != SpeechRecognizer.NO_TIMESTAMP) {
                // 把上一次的时间戳平移到下一个窗口的时间轴上
                val timestampStep = (timestamp * (1 - overlapRatio)).toInt()
                prevWordTimestamps = prevWordTimestamps.map { it - timestampStep }
            }
            currentTokens.clear()
            currentWordTimestamps.clear()
            prevWordIndexOfUnconfirmed = -1
            prevWordIndexOfPivot = -1
            return result
        }

        // 第一个窗口：整段都算未确认
        if (prevWords.isEmpty()) {
            return Postprocessor.DecodedText("", currentWords.joinToString(" "))
        }

        // 当前窗口还不够长，先不改动上一次的输出
        if (
            (timestamp != SpeechRecognizer.NO_TIMESTAMP && timestamp < prevWordTimestamps.last()) ||
            (timestamp == SpeechRecognizer.NO_TIMESTAMP && currentWords.size < prevWords.size * overlapRatio)
        ) {
            return null
        }

        // 分界点上一次已经算过，只需把新 token 接进未确认文本
        if (prevWordIndexOfUnconfirmed != -1) {
            return Postprocessor.DecodedText("", mergeIntoUnconfirmedText(currentWords).joinToString(" "))
        }

        return getConfirmedAndUnconfirmedTextFromPrevWordsAndCurrentWords(currentWords)
    }

    /** 收尾：把上一次的文本和当前窗口合并，必要时把整段升级为「已确认」 */
    private fun getConfirmedAndUnconfirmedTextAtEndOfSequence(currentWords: List<String>): Postprocessor.DecodedText? {
        val mergedWords = mergeIntoUnconfirmedText(currentWords)
        val mergedTimestamps = mergeTimestamps(mergedWords.size)
        if (mergedWords.isEmpty() && prevWords.isNotEmpty()) {
            // 当前窗口是静音 → 上一次的词全部确认
            lastConfirmedWords = prevWords
            prevWords = mergedWords
            prevWordTimestamps = mergedTimestamps
            return Postprocessor.DecodedText(lastConfirmedWords.joinToString(" "), "")
        }

        // 当前窗口里静音比平时长时，会和「已确认」的尾部重复，这里去一次重
        prevWords = dedupWords(lastConfirmedWords, mergedWords, mergedWords.size)
        prevWordTimestamps = mergedTimestamps.drop((mergedTimestamps.size - prevWords.size).coerceAtLeast(0))
        if (prevWords.isNotEmpty() && prevWordIndexOfUnconfirmed == -1) {
            return Postprocessor.DecodedText("", prevWords.joinToString(" "))
        }

        return null
    }

    /**
     * 在上一次的文本里找两个点：① 与当前窗口对齐的起点（之前的算已确认）；
     * ② 未确认区内部「上一次 / 当前」的分界（[pivotFactor] 决定切多少给当前窗口）。
     */
    private fun getConfirmedAndUnconfirmedTextFromPrevWordsAndCurrentWords(
        currentWords: List<String>,
    ): Postprocessor.DecodedText {
        val middleIndexToSearch =
            if (prevWordTimestamps.isNotEmpty() && currentWordTimestamps.isNotEmpty()) {
                val targetTimestamp = currentWordTimestamps.first()
                val index = prevWordTimestamps.indexOfFirst { it >= targetTimestamp }
                if (index != -1) index else (prevWords.size * (1 - overlapRatio)).toInt()
            } else {
                (prevWords.size * (1 - overlapRatio)).toInt()
            }

        var minDistance = maxLevenshteinDistance
        // 一处重叠都没找到时，退化成「上一次全部算未确认」
        prevWordIndexOfUnconfirmed = (middleIndexToSearch + searchWindow + 1).coerceAtMost(prevWords.size)
        for (i in -searchWindow..searchWindow) {
            val startIndex = (middleIndexToSearch + i).coerceIn(prevWords.indices)
            val endIndex = (startIndex + currentWords.size).coerceAtMost(prevWords.size)
            val distance = levenshtein(
                canonicalize(prevWords.subList(startIndex, endIndex)),
                canonicalize(currentWords.subList(0, (endIndex - startIndex).coerceAtMost(currentWords.size))),
            )
            if (distance < minDistance) {
                minDistance = distance
                prevWordIndexOfUnconfirmed = startIndex
            }
        }

        val unconfirmedWordsUntilPivot = ceil((prevWords.size - prevWordIndexOfUnconfirmed) * pivotFactor).toInt()
        prevWordIndexOfPivot = (prevWordIndexOfUnconfirmed + unconfirmedWordsUntilPivot).coerceAtMost(prevWords.size)
        lastConfirmedWords = prevWords.subList(0, prevWordIndexOfUnconfirmed)
        return Postprocessor.DecodedText(
            lastConfirmedWords.joinToString(" "),
            mergeIntoUnconfirmedText(currentWords).joinToString(" "),
        )
    }

    private fun detokenize(tokens: IntArray): List<String> = tokenizer.decode(tokens).split(" ").map { it.trim() }.filter { it.isNotEmpty() }

    private fun mergeIntoUnconfirmedText(currentWords: List<String>) = if (prevWordIndexOfUnconfirmed == -1) {
        currentWords
    } else {
        mergeIntoUnconfirmedText(
            prevWords,
            prevWordTimestamps,
            currentWords,
            currentWordTimestamps,
            overlapRatio,
            pivotFactor,
            prevWordIndexOfUnconfirmed,
            prevWordIndexOfPivot,
        )
    }

    private fun mergeTimestamps(numMergedWords: Int): List<Int> {
        if (numMergedWords == 0) return emptyList()
        if (
            prevWordTimestamps.isEmpty() ||
            prevWordIndexOfUnconfirmed == -1 ||
            prevWordIndexOfUnconfirmed == prevWordIndexOfPivot
        ) {
            return currentWordTimestamps
        }
        val numWordsFromPrevWords = prevWordIndexOfPivot - prevWordIndexOfUnconfirmed
        val timestampsFromPrevWords = prevWordTimestamps.subList(prevWordIndexOfUnconfirmed, prevWordIndexOfPivot)
        val numWordsFromCurrentWords = numMergedWords - numWordsFromPrevWords
        val timestampsFromCurrentWords = currentWordTimestamps.drop(currentWordTimestamps.size - numWordsFromCurrentWords)
        return timestampsFromPrevWords + timestampsFromCurrentWords
    }

    companion object {

        internal const val DEFAULT_SEARCH_WINDOW = 2
        internal const val DEFAULT_MAX_DISTANCE = 5
        internal const val DEFAULT_PIVOT_FACTOR = 0.6f

        /** 文件转写允许的编辑距离容差（官方常量） */
        const val FILE_MAX_LEVENSHTEIN_DISTANCE = 5

        /** 实时录音允许更大的容差（官方常量） */
        const val LIVE_MAX_LEVENSHTEIN_DISTANCE = 20

        /** 标准 Levenshtein 编辑距离（插入/删除/替换代价均为 1） */
        internal fun levenshtein(s1: String, s2: String): Int {
            val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
            for (i in 0..s1.length) dp[i][0] = i
            for (j in 0..s2.length) dp[0][j] = j
            for (i in 1..s1.length) {
                for (j in 1..s2.length) {
                    val deletionCost = dp[i - 1][j] + 1
                    val insertionCost = dp[i][j - 1] + 1
                    val matchOrSubstitutionCost = dp[i - 1][j - 1] + if (s1[i - 1] == s2[j - 1]) 0 else 1
                    dp[i][j] = minOf(deletionCost, insertionCost, matchOrSubstitutionCost)
                }
            }
            return dp[s1.length][s2.length]
        }

        /**
         * 未确认文本 = 上一次 `[已确认, pivot)` 的词 + 当前窗口 pivot 之后的词。
         * 若当前窗口 pivot 之后还不够长，就整段沿用上一次（避免未确认区忽长忽短地跳）。
         */
        internal fun mergeIntoUnconfirmedText(
            prevWords: List<String>,
            prevWordTimestamps: List<Int>,
            currentWords: List<String>,
            currentWordTimestamps: List<Int>,
            overlapRatio: Float,
            pivotFactor: Float,
            prevWordIndexOfUnconfirmed: Int,
            prevWordIndexOfPivot: Int,
        ): List<String> {
            val useTimestamps = currentWordTimestamps.isNotEmpty() && prevWordTimestamps.isNotEmpty()
            val numWordsBeforePivotInCurrent =
                if (useTimestamps) {
                    val timestampAtPivot =
                        if (prevWordIndexOfPivot >= prevWordTimestamps.size) {
                            prevWordTimestamps.last() + 1
                        } else {
                            prevWordTimestamps[prevWordIndexOfPivot]
                        }
                    currentWordTimestamps.count { it < timestampAtPivot }
                } else {
                    val numWordsOverlapInCurrent =
                        ceil(currentWords.size * overlapRatio)
                            .toInt()
                            .coerceAtLeast(prevWords.size - prevWordIndexOfUnconfirmed)
                    (numWordsOverlapInCurrent * pivotFactor).toInt().coerceAtMost(currentWords.size)
                }
            val numWordsAfterPivotInCurrent = currentWords.size - numWordsBeforePivotInCurrent
            if (numWordsAfterPivotInCurrent < prevWords.size - prevWordIndexOfPivot) {
                return prevWords.subList(prevWordIndexOfUnconfirmed, prevWords.size)
            }

            val unconfirmedTextInPrev = prevWords.subList(prevWordIndexOfUnconfirmed, prevWordIndexOfPivot)
            val unconfirmedTextInCurrent = currentWords.subList(numWordsBeforePivotInCurrent, currentWords.size)
            return unconfirmedTextInPrev + dedupWords(unconfirmedTextInPrev, unconfirmedTextInCurrent)
        }

        /**
         * 去掉 [currentWords] 开头与 [prevWords] 尾部重复的词（最多看 [searchWindow] 个）
         *
         * 用「前缀近似相等」而不是严格相等，因为重叠区两次识别的结果常有个别字母差异
         * （官方 [closeEnough] 的判据：长度 ≤3 要求前缀完全相同，否则编辑距离 ≤1）。
         */
        internal fun dedupWords(
            prevWords: List<String>,
            currentWords: List<String>,
            searchWindow: Int = DEFAULT_SEARCH_WINDOW,
        ): List<String> {
            val maxSearchRange = searchWindow.coerceAtMost(minOf(prevWords.size, currentWords.size))
            for (i in maxSearchRange downTo 1) {
                if (
                    closeEnough(
                        canonicalize(prevWords.subList(prevWords.size - i, prevWords.size)),
                        canonicalize(currentWords.subList(0, i)),
                    )
                ) {
                    return currentWords.subList(i, currentWords.size)
                }
            }
            return currentWords
        }

        /** 比较用的规范化形式：转小写 + 去标点 */
        internal fun canonicalize(words: List<String>): String = words.joinToString(" ") { it.lowercase() }.replace(Regex("[\\p{Punct}]"), "")

        internal fun closeEnough(l1: List<String>, l2: List<String>): Boolean = l1.size == l2.size && l1.zip(l2).all { closeEnough(it.first, it.second) }

        /** 逐词近似判等：先截到等长，长度 ≤3 时要求完全相同，否则允许 1 个编辑距离 */
        internal fun closeEnough(s1: String, s2: String): Boolean {
            val minLen = minOf(s1.length, s2.length)
            val prefix1 = s1.substring(0, minLen)
            val prefix2 = s2.substring(0, minLen)
            return if (minLen <= SHORT_WORD_LENGTH) prefix1 == prefix2 else levenshtein(prefix1, prefix2) <= 1
        }

        /** 短词阈值：3 个字符以内不允许容错，否则 "a"/"i" 这种会互相误判成重复 */
        private const val SHORT_WORD_LENGTH = 3
    }
}
