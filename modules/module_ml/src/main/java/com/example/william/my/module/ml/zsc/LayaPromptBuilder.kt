/*
 * Copyright 2026 The Google AI Edge Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// laya 0.3.4 common.py 与 LayaHost._to_internal 的移植版（Convai Innovations）。
package com.example.william.my.module.ml.zsc

/** 归一化后的上游题目结构；criteria 的顺序决定 marker 顺序。 */
data class LayaQuestion(val type: String, val instructions: String, val criteria: Any?) {
    /** 图通道：choice=0、score=1、noul=2。 */
    val qtype: Int
        get() =
            when (type) {
                "choice" -> 0
                "score" -> 1
                "noul" -> 2
                else -> throw IllegalArgumentException("Unknown question type: $type")
            }

    /** 解码器需要的 marker logit 数量。 */
    val optionCount: Int
        get() =
            when (type) {
                "choice" -> LayaJson.asObject(criteria).size
                "score" -> LayaJson.asArray(criteria).size
                else -> 2
            }

    /** 按宿主契约的插入顺序序列化归一化字段。 */
    fun asMap(): Map<String, Any?> = linkedMapOf("type" to type, "instructions" to instructions, "criteria" to criteria)
}

/** 未填充的 token id 与存活的选项 marker 位置，供一次图调用使用。 */
data class LayaSequence(val ids: IntArray, val markers: IntArray, val question: LayaQuestion) {
    /** 未填充的 id；图包装器会追加 PAD 行与注意力掩码。 */
    val sequenceIds: IntArray
        get() = ids

    /** 从 token_logits 聚合的位置。 */
    val markerPositions: IntArray
        get() = markers

    /** 上游题目类型字符串。 */
    val qtype: String
        get() = question.type

    /** 与 marker 同序渲染的 criteria。 */
    val options: List<String>
        get() = LayaPromptBuilder.renderOptions(question)
}

/** 每次图调用一题。每次编码都关闭分词器后处理。 */
class LayaPromptBuilder(
    private val tokenizer: LayaTokenizer,
    val maxLen: Int = 256,
    val headMaxLen: Int = 256,
) {
    init {
        require(maxLen > 0 && headMaxLen > 0)
    }

    /** 在构建 prompt 之前先接收上游 map 结构。 */
    fun normalize(question: Map<String, Any?>): LayaQuestion = Companion.normalize(question)

    /** 用精确的选项上限、压缩规则与右侧截断构建一行。 */
    fun build(state: Any?, question: Map<String, Any?>, questionId: String = ""): LayaSequence = build(state, normalize(question), questionId)

    /** 用精确的选项上限、压缩规则与右侧截断构建一行。 */
    fun build(state: Any?, question: LayaQuestion, questionId: String = ""): LayaSequence {
        val options = renderOptions(question)
        val instructions = question.instructions.replace(MASK_LITERAL, " ")
        val head =
            tokenizer.encode("${question.type} question: $instructions", addSpecialTokens = false)
        var encodedOptions =
            options.map { option ->
                val text =
                    tokenizer.encode(" " + option.replace(MASK_LITERAL, " "), addSpecialTokens = false)
                intArrayOf(MASK) + text.copyOf(minOf(text.size, 48))
            }
        var optionBudget = headMaxLen - encodedOptions.sumOf { it.size }
        if (optionBudget < 16) {
            val per = maxOf(4, Math.floorDiv(headMaxLen - 16, maxOf(1, encodedOptions.size)))
            encodedOptions = encodedOptions.map { it.copyOf(minOf(it.size, per)) }
            optionBudget = headMaxLen - encodedOptions.sumOf { it.size }
        }
        val ids = ArrayList<Int>()
        ids.add(CLS)
        ids.addAll(head.take(maxOf(8, optionBudget)))
        ids.add(SEP)
        val markers = ArrayList<Int>()
        for (option in encodedOptions) {
            markers.add(ids.size)
            ids.addAll(option.asList())
        }
        ids.add(SEP)
        val room = maxOf(0, maxLen - ids.size - 1)
        val stateIds =
            tokenizer.encode(serializeState(state).replace(MASK_LITERAL, " "), addSpecialTokens = false)
        ids.addAll(stateIds.take(room))
        ids.add(SEP)
        val retainedMarkers = markers.filter { it < maxLen }.toIntArray()
        // 这是上游真正的拒绝规则：被压缩的选项只要其 marker 存活即有效。
        require(retainedMarkers.size == options.size) {
            "question '$questionId' options exceed head_max_len=$headMaxLen"
        }
        return LayaSequence(ids.take(maxLen).toIntArray(), retainedMarkers, question)
    }

    companion object {
        const val PAD = 0
        const val SEP = 1
        const val CLS = 2
        const val MASK = 4
        const val MASK_LITERAL = "<mask>"

        /** 按 Python 宿主的方式转换 choice 列表与非字符串 instructions。 */
        fun normalize(question: Map<String, Any?>): LayaQuestion {
            val type = question["type"] as? String ?: error("Question requires string type")
            require(type == "choice" || type == "score" || type == "noul") {
                "Unknown question type: $type"
            }
            var criteria = question["criteria"]
            if (type == "choice" && criteria is List<*>) {
                val labels = criteria
                criteria =
                    LinkedHashMap<String, Any?>().apply {
                        labels.forEach { label ->
                            put(label as? String ?: error("Choice label must be a string"), null)
                        }
                    }
            }
            require(question.containsKey("instructions")) { "Question requires instructions" }
            val instructions =
                question["instructions"].let {
                    if (it is String) it else LayaJson.stringify(it, ensureAscii = true)
                }
            return LayaQuestion(type, instructions, criteria)
        }

        /** 字符串保持字面量；其它状态使用 Python 兼容的 JSON 序列化。 */
        fun serializeState(state: Any?): String = if (state is String) state else LayaJson.stringify(state)

        /** 用宿主的 JSON 与回退字符串行为渲染单个 criterion。 */
        fun renderCriterion(value: Any?): String = when (value) {
            is String -> value
            null,
            is Number,
            is Boolean,
            is Map<*, *>,
            is List<*>,
            -> LayaJson.stringify(value)
            else -> LayaJson.stringify(value.toString()) // json.dumps(default=str)
        }

        /** 保留 choice 标签、score 层级与 false/true 的 noul 顺序。 */
        fun renderOptions(question: LayaQuestion): List<String> = when (question.type) {
            "choice" ->
                LayaJson.asObject(question.criteria).map { (label, value) ->
                    if (value == null || value == "") label else "$label: ${renderCriterion(value)}"
                }
            "score" ->
                LayaJson.asArray(question.criteria).mapIndexed { index, value ->
                    "level $index: ${renderCriterion(value)}"
                }
            "noul" -> {
                // 源码在查找两个具名 criteria 之前先做 `crit = crit or {}`。
                val criteria =
                    if (pythonFalsey(question.criteria)) {
                        emptyMap()
                    } else {
                        LayaJson.asObject(question.criteria)
                    }
                val falseValue = criteria["false"]
                val trueValue = criteria["true"]
                listOf(
                    "false: " +
                        if (falseValue == null || falseValue == "") {
                            "no, the statement does not hold"
                        } else {
                            renderCriterion(falseValue)
                        },
                    "true: " +
                        if (trueValue == null || trueValue == "") {
                            "yes, the statement holds"
                        } else {
                            renderCriterion(trueValue)
                        },
                )
            }
            else -> error("Unknown question type: ${question.type}")
        }

        private fun pythonFalsey(value: Any?): Boolean = when (value) {
            null -> true
            is Boolean -> !value
            is Number -> value.toDouble() == 0.0
            is String -> value.isEmpty()
            is Collection<*> -> value.isEmpty()
            is Map<*, *> -> value.isEmpty()
            else -> false
        }
    }
}
