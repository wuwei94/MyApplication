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

package com.example.william.my.module.ml.zsc

import java.io.File

/**
 * 只用到温度（temperature）字段；标定的来源信息仍保留在源 JSON 里。
 */
class LayaCalibration(
    private val temperatures: DoubleArray = doubleArrayOf(1.0, 1.0, 1.0),
    private val byOptions: Map<String, Double> = emptyMap(),
) {
    init {
        require(temperatures.size == 3) { "Expected choice/score/noul temperatures" }
    }

    /** 有选项数分桶时用分桶温度，否则用题型温度。 */
    fun temperature(qtype: String, k: Int): Double = byOptions[bucket(qtype, k)]
        ?: temperatures[
            when (qtype) {
                "choice" -> 0
                "score" -> 1
                "noul" -> 2
                else -> error("Unknown question type: $qtype")
            },
        ]

    companion object {
        /** 恒等标定：不校准 logits，用于捕获的 T=1 参考门禁。 */
        fun identity(): LayaCalibration = LayaCalibration()

        /** 从已安装的 JSON 文件加载标定。 */
        fun load(file: File): LayaCalibration = fromMap(LayaJson.asObject(LayaJson.parse(file)))

        /** 只读温度字段；无关的来源字段保持未用。 */
        fun fromMap(config: Map<String, Any?>): LayaCalibration {
            val defaults =
                config["temperature"]?.let { value ->
                    LayaJson.asArray(value).map { (it as Number).toDouble() }.toDoubleArray()
                } ?: doubleArrayOf(1.0, 1.0, 1.0)
            val buckets =
                config["temperature_by_options"]?.let { value ->
                    LayaJson.asObject(value).mapValues { (_, number) -> (number as Number).toDouble() }
                } ?: emptyMap()
            return LayaCalibration(defaults, buckets)
        }

        /** 匹配宿主契约的两/五/十选项分桶边界。 */
        fun bucket(qtype: String, k: Int): String = qtype +
            ":" +
            when {
                k <= 2 -> "2"
                k <= 5 -> "3-5"
                k <= 10 -> "6-10"
                else -> "11+"
            }
    }
}
