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

import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/** 只读 token 表；图接收聚合后的 float32 嵌入，含 PAD 行。 */
class LayaEmbeddings(tableFile: File, metadataFile: File) : Closeable {
    /** 表布局与 checkpoint 来源，加载时不复制表本身。 */
    val metadata: Map<String, Any?> = LayaJson.asObject(LayaJson.parse(metadataFile))

    /** 元数据中声明的表哈希；由安装器校验文件内容。 */
    val sha256: String
    private var table: ByteBuffer?

    init {
        val shape = LayaJson.asArray(metadata["shape"]).map { (it as Number).toInt() }
        require(shape == listOf(VOCABULARY_SIZE, WIDTH)) { "Expected token table shape [256000, 768]" }
        require(metadata["dtype"] == "float16") { "Expected float16 token table" }
        require(metadata["byte_order"] == "little") { "Expected little-endian token table" }
        require(tableFile.isFile && tableFile.length() == SIZE_BYTES) {
            "Invalid token table size: ${tableFile.length()} B; expected $SIZE_BYTES B"
        }
        (metadata["size_bytes"] as? Number)?.let {
            require(it.toLong() == SIZE_BYTES) { "Token table metadata size mismatch" }
        }
        sha256 = metadata["sha256"] as? String ?: error("Missing token table SHA256")
        require(sha256.length == 64 && sha256.all { it in '0'..'9' || it in 'a'..'f' }) {
            "Invalid token table SHA256"
        }
        // 关闭 channel 后只读映射仍有效；不复制这张 393 MB 的表。
        table =
            FileInputStream(tableFile).channel.use {
                it.map(FileChannel.MapMode.READ_ONLY, 0, SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
            }
    }

    /** 填充行主序 [1, window, 768]；ids 之后的位置用 token id 0，而非零向量。 */
    fun gather(
        ids: IntArray,
        window: Int = ids.size,
        destination: FloatArray = FloatArray(window * WIDTH),
    ): FloatArray {
        val mapped = checkNotNull(table) { "LayaEmbeddings is closed" }
        require(window >= ids.size) { "Sequence exceeds embedding window $window" }
        require(window <= Int.MAX_VALUE / WIDTH && destination.size == window * WIDTH) {
            "Embedding destination must contain window * 768 float32 values"
        }
        ids.forEach { require(it in 0 until VOCABULARY_SIZE) { "Token id out of range: $it" } }
        var output = 0
        for (position in 0 until window) {
            val token = if (position < ids.size) ids[position] else PAD_ID
            var offset = token * WIDTH * 2
            repeat(WIDTH) {
                destination[output++] = halfToFloat(mapped.getShort(offset).toInt())
                offset += 2
            }
        }
        return destination
    }

    /** 取消映射由 JVM 负责；释放该引用可避免引擎关闭后仍持有它。 */
    override fun close() {
        table = null
    }

    companion object {
        /** 多语 checkpoint 中的词表行数。 */
        const val VOCABULARY_SIZE = 256000

        /** 每个 token 行的 float16 值个数。 */
        const val WIDTH = 768

        /** 填充位置聚合的是真实嵌入行，而非零向量。 */
        const val PAD_ID = 0

        /** 完整的行主序 binary16 表大小。 */
        const val SIZE_BYTES = 393_216_000L

        /** 精确的 IEEE-754 binary16 → binary32 展开，含符号零、次正规数与 NaN。 */
        fun halfToFloat(bits: Int): Float {
            val sign = (bits and 0x8000) shl 16
            val exponent = (bits ushr 10) and 0x1f
            val fraction = bits and 0x3ff
            val expanded =
                when (exponent) {
                    0 ->
                        if (fraction == 0) {
                            sign
                        } else {
                            val shift = Integer.numberOfLeadingZeros(fraction) - 21
                            sign or ((113 - shift) shl 23) or (((fraction shl shift) and 0x3ff) shl 13)
                        }
                    // IEEE 转换会让信令 NaN 静默化，同时保留符号位与载荷，与 NumPy 一致。
                    31 -> sign or 0x7f800000 or (fraction shl 13) or (if (fraction == 0) 0 else 0x400000)
                    else -> sign or ((exponent + 112) shl 23) or (fraction shl 13)
                }
            return Float.fromBits(expanded)
        }
    }
}
