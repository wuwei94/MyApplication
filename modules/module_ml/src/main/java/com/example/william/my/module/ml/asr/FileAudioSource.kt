package com.example.william.my.module.ml.asr

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * 从 **16-bit PCM WAV** 流里按「定长块 + 重叠」读取。
 *
 * 与官方 `FileAudioSource` 一致的三条行为：
 * 1. 首块读满 `interval`；其后每块只读 `interval - overlap` 个新采样，
 *    缓冲区左移后与上一块的重叠部分拼成完整一块。
 * 2. 读不满时**用 0 补齐**（当静音），最后一块因此可能比 interval 短 —— 这是有意的。
 * 3. 一旦读不到新数据就**直接结束**，不处理缓冲区里剩下的重叠采样：
 *    官方注释说明「那些采样已被上一块处理过，重跑只会更差」。
 *
 * 与官方的一处差异：官方只校验 `fmt ` 里的采样率与数据格式，**不支持非 16-bit／非单声道**，
 * 也不处理 chunk 的对齐补齐字节。本模块沿用这一限制（演示页面对输入的要求如实写在界面上），
 * 但对「块内越界的采样数」加了一次钳位 —— 官方在这种情况下会直接抛
 * `ArrayIndexOutOfBoundsException`，这里改为截断，坏文件不至于把页面打崩。
 */
internal class FileAudioSource(
    private val inputStream: InputStream,
    override val samplingRate: Int,
    override val interval: Duration,
    override val overlap: Duration,
) : AudioSource {

    init {
        require(interval > overlap) { "interval 必须严格长于 overlap" }
    }

    private val samplesPerInterval = getNumSamples(interval)
    private val overlapSamples = getNumSamples(overlap)
    private val step = samplesPerInterval - overlapSamples

    override fun close() {
        runCatching { inputStream.close() }
    }

    override fun getAudioData(): Sequence<FloatArray> = sequence {
        val dataStream = validateHeaderAndGetDataStream(inputStream)

        val maxChunkSizeBytes = samplesPerInterval * BYTES_PER_SAMPLE
        val stepSizeBytes = step * BYTES_PER_SAMPLE

        val bufferForSamples = FloatArray(samplesPerInterval)
        val rawBytes = ByteArray(maxChunkSizeBytes)
        var isFirstChunk = true

        while (true) {
            val bytesToRead = if (isFirstChunk) maxChunkSizeBytes else stepSizeBytes
            val readBytes = dataStream.read(rawBytes, 0, bytesToRead)
            if (readBytes <= 0) break

            val newSamples = readBytes / BYTES_PER_SAMPLE
            val byteBuffer = ByteBuffer.wrap(rawBytes, 0, readBytes).order(ByteOrder.LITTLE_ENDIAN)

            val sampleIndexStart = if (isFirstChunk) 0 else overlapSamples
            val writable = (samplesPerInterval - sampleIndexStart).coerceAtLeast(0)
            val copyCount = newSamples.coerceAtMost(writable)
            for (i in 0 until copyCount) {
                bufferForSamples[sampleIndexStart + i] = byteBuffer.short / MAX_PCM_VALUE
            }
            for (i in sampleIndexStart + copyCount until bufferForSamples.size) {
                bufferForSamples[i] = 0.0f
            }
            isFirstChunk = false

            yield(bufferForSamples)

            System.arraycopy(bufferForSamples, step, bufferForSamples, 0, overlapSamples)
        }
    }

    /** 跳过除 `fmt ` / `data` 之外的 chunk，返回时流正停在 `data` 块的数据起点 */
    private fun validateHeaderAndGetDataStream(inputStream: InputStream): InputStream {
        val riffHeader = ByteArray(12)
        require(inputStream.read(riffHeader) == 12) { "不是合法 WAV：文件过短" }
        require(String(riffHeader, 0, 4) == "RIFF" && String(riffHeader, 8, 4) == "WAVE") {
            "不是合法的 RIFF/WAVE 文件"
        }

        while (true) {
            val chunkHeader = ByteArray(8)
            require(inputStream.read(chunkHeader) == 8) { "不是合法 WAV：chunk 头被截断" }

            val headerBuffer = ByteBuffer.wrap(chunkHeader).order(ByteOrder.LITTLE_ENDIAN)
            val chunkId = String(chunkHeader, 0, 4)
            headerBuffer.position(4)
            val chunkSize = headerBuffer.int

            when (chunkId) {
                FMT_CHUNK_ID -> {
                    val fmtData = ByteArray(chunkSize)
                    readFully(inputStream, fmtData)

                    val fmtBuffer = ByteBuffer.wrap(fmtData).order(ByteOrder.LITTLE_ENDIAN)
                    val audioFormat = fmtBuffer.short.toInt()
                    require(audioFormat == AUDIO_FORMAT_PCM) {
                        "仅支持 PCM（1）音频，实际为 $audioFormat"
                    }

                    fmtBuffer.position(4) // 跳过 channels(2) + blockAlign(2)
                    val fileSampleRate = fmtBuffer.int
                    require(fileSampleRate == samplingRate) {
                        "采样率不符：要求 $samplingRate，文件为 $fileSampleRate"
                    }
                }
                DATA_CHUNK_ID -> return inputStream
                else -> skipFully(inputStream, chunkSize.toLong())
            }
        }
    }

    private fun readFully(inputStream: InputStream, buffer: ByteArray) {
        var totalRead = 0
        while (totalRead < buffer.size) {
            val readBytes = inputStream.read(buffer, totalRead, buffer.size - totalRead)
            require(readBytes >= 0) { "不是合法 WAV：chunk 被截断" }
            totalRead += readBytes
        }
    }

    private fun skipFully(inputStream: InputStream, byteCount: Long) {
        var skipped = 0L
        while (skipped < byteCount) {
            val skippedBytes = inputStream.skip(byteCount - skipped)
            require(skippedBytes > 0) { "不是合法 WAV：chunk 被截断" }
            skipped += skippedBytes
        }
    }

    private fun getNumSamples(duration: Duration): Int = (samplingRate * duration.toDouble(DurationUnit.SECONDS)).toInt()

    private companion object {
        const val FMT_CHUNK_ID = "fmt "
        const val DATA_CHUNK_ID = "data"
        const val AUDIO_FORMAT_PCM = 1

        /** 16-bit PCM 每个采样 2 字节 */
        const val BYTES_PER_SAMPLE = 2

        /** 16-bit PCM 归一化系数 */
        const val MAX_PCM_VALUE = 32768.0f
    }
}
