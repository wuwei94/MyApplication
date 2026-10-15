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

// NumPy float32 主机解码器的移植版；来源归属保留在 NOTICE 中。
package com.example.william.my.module.ml.zsc

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.exp
import kotlin.math.ln

/** float32 主机解码，复现 NumPy 的归约顺序与 Python 的舍入语义。 */
object LayaDecoder {
    /** 按构建器标记（marker）顺序读取各选项的 logit。 */
    fun gather(tokenLogits: FloatArray, markers: IntArray): FloatArray = FloatArray(markers.size) { tokenLogits[markers[it]] }

    /** 数值稳定的 softmax，使用参考实现的 float32 指数与归约。 */
    fun softmax(values: FloatArray): FloatArray {
        require(values.isNotEmpty()) { "At least one logit is required" }
        val max = values.maxOrNull()!!
        val exponents = FloatArray(values.size) { exp((values[it] - max).toDouble()).toFloat() }
        val sum = numpySum(exponents)
        return FloatArray(values.size) { exponents[it] / sum }
    }

    /** 复现 NumPy 的 float32 成对归约（含 8 累加器的顺序）。 */
    private fun numpySum(values: FloatArray, from: Int = 0, size: Int = values.size): Float {
        if (size < 8) {
            var sum = -0.0f
            for (i in from until from + size) sum += values[i]
            return sum
        }
        if (size <= 128) {
            val sums = FloatArray(8) { values[from + it] }
            var i = 8
            while (i < size - size % 8) {
                for (j in 0..7) sums[j] += values[from + i + j]
                i += 8
            }
            var sum =
                ((sums[0] + sums[1]) + (sums[2] + sums[3])) + ((sums[4] + sums[5]) + (sums[6] + sums[7]))
            while (i < size) {
                sum += values[from + i]
                i++
            }
            return sum
        }
        val left = (size / 2) - (size / 2) % 8
        return numpySum(values, from, left) + numpySum(values, from + left, size - left)
    }

    /** 未经标定的原始概率为动作图（act graph）提供四个特征。 */
    fun actFeatures(rawLogits: FloatArray): FloatArray {
        val p = softmax(rawLogits)
        val k = maxOf(p.size, 2)
        val sorted = p.sortedDescending()
        val top1 = sorted[0]
        val top2 = sorted.getOrElse(1) { 0.0f }
        val terms = FloatArray(p.size) { p[it] * ln(maxOf(p[it], 1e-9f).toDouble()).toFloat() }
        val entropy = -numpySum(terms) / ln(k.toFloat().toDouble()).toFloat()
        return floatArrayOf(top1, top1 - top2, entropy, k.toFloat() / 255.0f)
    }

    /** 在原始 softmax 之前应用选项分桶标定；温度下限为 1e-3。 */
    fun probabilities(
        rawLogits: FloatArray,
        question: LayaQuestion,
        calibration: LayaCalibration = LayaCalibration.identity(),
    ): FloatArray {
        // NumPy 2.x 的弱标量提升让这里的除法保持在 float32。
        val temperature = maxOf(1e-3, calibration.temperature(question.type, rawLogits.size)).toFloat()
        return softmax(FloatArray(rawLogits.size) { rawLogits[it] / temperature })
    }

    /** 返回归一化熵置信度，独立于动作头的概率。 */
    fun confidence(p: FloatArray): Double {
        if (p.size < 2) return 1.0
        val terms = FloatArray(p.size) { p[it] * ln(p[it].coerceIn(1e-12f, 1.0f).toDouble()).toFloat() }
        val entropy = -numpySum(terms)
        // 在 NumPy 2.x 中 np.float32/Python float 与 Python float - np.float32 仍为 float32。
        return (1.0f - entropy / ln(p.size.toDouble()).toFloat()).coerceIn(0.0f, 1.0f).toDouble()
    }

    /**
     * 用 BigDecimal(double) 而非 valueOf(double)，按 Python 的方式对二进制值四舍五入。
     */
    fun round4(value: Double): Double {
        if (!value.isFinite()) return value
        val rounded = BigDecimal(value).setScale(4, RoundingMode.HALF_EVEN).toDouble()
        return if (rounded == 0.0 && java.lang.Double.doubleToRawLongBits(value) < 0) -0.0 else rounded
    }

    /** 产出官方四位小数答案结构，且不改变题目顺序。 */
    fun decode(
        rawLogits: FloatArray,
        actLogits: FloatArray,
        question: Map<String, Any?>,
        calibration: LayaCalibration = LayaCalibration.identity(),
    ): Map<String, Any?> = decode(rawLogits, actLogits, LayaPromptBuilder.normalize(question), calibration)

    /** 产出官方四位小数答案结构，且不改变题目顺序。 */
    fun decode(
        rawLogits: FloatArray,
        actLogits: FloatArray,
        question: LayaQuestion,
        calibration: LayaCalibration = LayaCalibration.identity(),
    ): Map<String, Any?> {
        require(rawLogits.size == question.optionCount) { "Logits/criteria option count mismatch" }
        require(actLogits.size == 2) { "Act head must return two logits" }
        val p = probabilities(rawLogits, question, calibration)
        val action = linkedMapOf("act_probability" to round4(softmax(actLogits)[0].toDouble()))
        return when (question.type) {
            "choice" -> {
                val labels = LayaJson.asObject(question.criteria).keys.toList()
                var argmax = 0
                for (index in 1 until p.size) if (p[index] > p[argmax]) argmax = index
                linkedMapOf(
                    "type" to "choice",
                    "choice" to labels[argmax],
                    "probabilities" to
                        labels.indices.associateTo(LinkedHashMap()) { labels[it] to round4(p[it].toDouble()) },
                    "confidence" to round4(confidence(p)),
                    "action" to action,
                )
            }
            "score" -> {
                val criteria = LayaJson.asArray(question.criteria)
                // NumPy arange(int64)*float32 会把每次乘法与归约提升为 float64。
                val expectation = p.indices.sumOf { it.toDouble() * p[it].toDouble() }
                linkedMapOf(
                    "type" to "score",
                    "score" to round4(expectation),
                    "legend" to
                        criteria.indices.associateTo(LinkedHashMap()) { it.toString() to criteria[it] },
                    "probabilities" to
                        p.indices.associateTo(LinkedHashMap()) { it.toString() to round4(p[it].toDouble()) },
                    "confidence" to round4(confidence(p)),
                    "action" to action,
                )
            }
            "noul" -> {
                val trueProbability = p[1].toDouble()
                linkedMapOf(
                    "type" to "noul",
                    "noul" to round4(trueProbability),
                    "confidence" to round4(maxOf(trueProbability, 1.0 - trueProbability)),
                    "action" to action,
                )
            }
            else -> error("Unknown question type: ${question.type}")
        }
    }
}
