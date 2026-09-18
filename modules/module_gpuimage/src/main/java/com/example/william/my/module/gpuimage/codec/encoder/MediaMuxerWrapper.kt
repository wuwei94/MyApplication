package com.example.william.my.module.gpuimage.codec.encoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import com.example.william.my.basic.basic_shared.utils.Utils
import java.io.File
import java.nio.ByteBuffer

/**
 * 媒体复用器包装类（MediaMuxer）
 *
 * 核心设计：
 * 1. 协调视频轨道（H.264）与音频轨道（AAC）的双轨注册；
 * 2. 严格遵循 MediaMuxer 契约：必须等待两路轨道的 [MediaFormat] 全部输出且调用 [start] 后，
 *    才能开始写入采样数据，防止状态未就绪导致的 IllegalStateException；
 * 3. 线程安全地调度采样帧写入，并在结束时安全关闭并输出 MP4。
 */
class MediaMuxerWrapper(
    val outputFile: File,
    private val expectedTracks: Int = 2,
) {

    private val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    private val lock = Any()

    @Volatile
    private var started = false
    private var registeredTracks = 0

    fun addTrack(format: MediaFormat): Int {
        synchronized(lock) {
            if (started) {
                throw IllegalStateException("MediaMuxer 已启动，不可再添加新轨道")
            }
            val trackIndex = muxer.addTrack(format)
            registeredTracks++
            if (registeredTracks >= expectedTracks) {
                muxer.start()
                started = true
                Utils.logcat(TAG, "MediaMuxer 双轨就绪，启动混流")
            }
            return trackIndex
        }
    }

    fun isStarted(): Boolean = started

    fun writeSampleData(trackIndex: Int, byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        synchronized(lock) {
            if (!started) return
            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                return
            }
            if (bufferInfo.size == 0) {
                return
            }
            try {
                muxer.writeSampleData(trackIndex, byteBuffer, bufferInfo)
            } catch (e: Exception) {
                Utils.logcat(TAG, "writeSampleData error on track $trackIndex: ${e.message}")
            }
        }
    }

    fun stopAndRelease() {
        synchronized(lock) {
            if (started) {
                try {
                    muxer.stop()
                } catch (e: Exception) {
                    Utils.logcat(TAG, "muxer.stop error: ${e.message}")
                }
            }
            try {
                muxer.release()
            } catch (e: Exception) {
                Utils.logcat(TAG, "muxer.release error: ${e.message}")
            }
            started = false
        }
    }

    companion object {
        private const val TAG = "MediaMuxerWrapper"
    }
}
