package com.example.william.my.module.ml.tts

import java.io.Closeable
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.ShortBuffer
import java.nio.channels.FileChannel
import java.util.zip.ZipFile

/**
 * 极简 NumPy `.npy` / `.npz` 读取器
 *
 * 对应官方 `text_to_speech_lm` 样例里的 `Npy.kt`。KittenTTS 页当时只需要解 `voices.npz`，
 * 逻辑内联在 [KittenVoiceTable] 里；Qwen3-TTS 用到的宿主侧张量既有 `.npy` 又有 `.npz`、
 * 既有 fp32 又有 fp16，于是把解析逻辑收敛到这里一份，两个页面共用。
 *
 * 只支持 Qwen3-TTS 宿主表实际用到的两种 dtype：小端 float32（`'<f4'`）与 float16（`'<f2'`）。
 * 大表（`text_embedding_fp16.npy` 622 MB）走 [mmapHalf] 内存映射、按需转 float，
 * 不进 JVM 堆；小表走 [load]，直接把 `readBytes()` 出来的数组当后备。
 */
internal object Npy {

    /** 解析出的 `.npy` 头：元素 dtype 是否为 fp16、元素个数、裸数据起始偏移 */
    data class Header(val isHalf: Boolean, val count: Int, val dataOffset: Int)

    /** 从文件头读到足够解析出的字节数即可：header 本身只有几十字节，512 足够任何合法头 */
    private const val HEADER_PROBE_BYTES = 512L

    /**
     * 解析 `.npy` 头
     *
     * 布局：magic `\x93NUMPY`（6 B）+ 版本（2 B）+ header 长度（v1.0 为 uint16、v2.0 起为 uint32）
     * + header（ASCII，形如 `{'descr': '<f4', 'fortran_order': False, 'shape': (400, 256), }`）
     * + 裸数据。**元素个数从 `shape` 推出**，而不是用 `(文件长度 - 偏移) / 元素字节数` 反算 ——
     * 前者能查出「文件被截断」，后者会把截断后的长度当成合法形状静默读歪。
     */
    fun header(bytes: ByteArray): Header {
        require(bytes.size > MAGIC.size + 4) { ".npy 过短（${bytes.size} 字节）" }
        require(MAGIC.indices.all { bytes[it] == MAGIC[it] }) { ".npy magic 不符" }

        val major = bytes[6].toInt() and 0xFF
        val headerLength: Int
        val headerStart: Int
        if (major >= 2) {
            headerLength = littleEndianInt(bytes, 8)
            headerStart = 12
        } else {
            headerLength = littleEndianShort(bytes, 8)
            headerStart = 10
        }
        require(headerLength > 0 && headerStart + headerLength <= bytes.size) {
            ".npy header 越界（长度 $headerLength，可用 ${bytes.size - headerStart}）"
        }

        val text = String(bytes, headerStart, headerLength, Charsets.US_ASCII)
        val isHalf = when {
            "'<f2'" in text -> true
            "'<f4'" in text -> false
            else -> throw IllegalArgumentException(".npy dtype 非浮点：$text")
        }
        require("'fortran_order': True" !in text) { ".npy 是 fortran order，未支持" }

        val shape = SHAPE.find(text)?.groupValues?.get(1)
            ?: throw IllegalArgumentException(".npy header 缺少 shape：$text")
        val count = shape
            .split(',')
            .mapNotNull { it.trim().toIntOrNull() }
            .fold(1) { product, dimension -> product * dimension }
        return Header(isHalf, count, headerStart + headerLength)
    }

    /** 把整份 `.npy` 读成 `FloatArray`（fp16 逐元素上转） */
    fun load(file: File): FloatArray = floats(file.readBytes())

    /** 解析已在内存里的 `.npy` 内容（也用于 `.npz` 里的单个条目） */
    fun floats(bytes: ByteArray): FloatArray {
        val parsed = header(bytes)
        val data = ByteBuffer.wrap(bytes, parsed.dataOffset, bytes.size - parsed.dataOffset)
            .order(ByteOrder.LITTLE_ENDIAN)
        val out = FloatArray(parsed.count)
        if (parsed.isHalf) {
            val halves = data.asShortBuffer()
            require(halves.remaining() >= parsed.count) {
                ".npy 数据不足：需 ${parsed.count} 个 fp16，实际 ${halves.remaining()}"
            }
            for (i in 0 until parsed.count) {
                out[i] = halfToFloat(halves.get(i))
            }
        } else {
            require(data.remaining() >= parsed.count * 4) {
                ".npy 数据不足：需 ${parsed.count} 个 fp32，实际 ${data.remaining() / 4}"
            }
            data.asFloatBuffer().get(out)
        }
        return out
    }

    /**
     * 内存映射一份 fp16 大表
     *
     * 返回的 [HalfTable] 持有文件句柄，用完后必须 [HalfTable.close]：文件描述符是进程级资源，
     * 漏关会一路累积到进程结束。**关闭句柄不会让映射失效** —— `FileChannel.map` 的契约是
     * 「映射一旦建立即不依赖创建它的 channel，关闭 channel 不影响映射有效性」，因此这里
     * 只释放句柄，映射本身由 GC 回收。
     */
    fun mmapHalf(file: File): HalfTable {
        val probe = ByteArray(minOf(HEADER_PROBE_BYTES, file.length()).toInt())
        RandomAccessFile(file, "r").use { it.readFully(probe) }
        val parsed = header(probe)
        require(parsed.isHalf) { "期望 fp16 表：$file" }

        val handle = RandomAccessFile(file, "r")
        val map: MappedByteBuffer = handle.channel.map(
            FileChannel.MapMode.READ_ONLY,
            parsed.dataOffset.toLong(),
            file.length() - parsed.dataOffset,
        )
        return HalfTable(handle, map.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer(), parsed.count)
    }

    /** 从 `.npz`（zip 容器，每个条目是一个 `.npy`）里取出指定的若干 fp32/fp16 数组 */
    fun loadNpz(file: File, names: List<String>): Map<String, FloatArray> {
        val out = HashMap<String, FloatArray>(names.size * 4 / 3 + 1)
        ZipFile(file).use { zip ->
            for (name in names) {
                val entry = zip.getEntry("$name.npy")
                    ?: throw IllegalArgumentException("$file 中缺少 $name.npy")
                zip.getInputStream(entry).use { out[name] = floats(it.readBytes()) }
            }
        }
        return out
    }

    /**
     * IEEE 754 半精度转单精度
     *
     * 热路径是「每次查表 2048 次」，用标量实现；`Float16` 那类批量 API 在 Android 上不可用。
     */
    fun halfToFloat(half: Short): Float {
        val bits = half.toInt() and 0xFFFF
        val sign = (bits and 0x8000) shl 16
        val exponent = (bits ushr 10) and 0x1F
        val mantissa = bits and 0x3FF
        return when {
            exponent == 0 -> {
                if (mantissa == 0) {
                    Float.fromBits(sign)
                } else {
                    // 非规格化数：先规格化再按正常指数编码
                    var m = mantissa
                    var shift = -1
                    while (m and 0x400 == 0) {
                        m = m shl 1
                        shift++
                    }
                    m = m and 0x3FF
                    Float.fromBits(sign or ((127 - 15 - shift) shl 23) or (m shl 13))
                }
            }

            exponent == 0x1F -> Float.fromBits(sign or 0x7F800000 or (mantissa shl 13))
            else -> Float.fromBits(sign or ((exponent - 15 + 127) shl 23) or (mantissa shl 13))
        }
    }

    /** 内存映射的 fp16 表；[close] 只释放文件句柄，映射交给 GC */
    class HalfTable internal constructor(
        private val handle: RandomAccessFile,
        val values: ShortBuffer,
        val count: Int,
    ) : Closeable {

        /** 取第 [index] 个元素（fp16 上转为 float） */
        fun get(index: Int): Float = Npy.halfToFloat(values.get(index))

        override fun close() {
            runCatching { handle.close() }
        }
    }

    private fun littleEndianShort(bytes: ByteArray, offset: Int): Int = (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)

    private fun littleEndianInt(bytes: ByteArray, offset: Int): Int = littleEndianShort(bytes, offset) or (littleEndianShort(bytes, offset + 2) shl 16)

    private val MAGIC = byteArrayOf(
        0x93.toByte(),
        'N'.code.toByte(),
        'U'.code.toByte(),
        'M'.code.toByte(),
        'P'.code.toByte(),
        'Y'.code.toByte(),
    )

    private val SHAPE = Regex("'shape':\\s*\\(([^)]*)\\)")
}
