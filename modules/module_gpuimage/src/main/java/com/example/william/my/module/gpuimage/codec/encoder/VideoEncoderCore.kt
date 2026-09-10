package com.example.william.my.module.gpuimage.codec.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import com.example.william.my.basic.basic_shared.utils.Utils

/**
 * 硬件视频编码核心类（基于 MediaCodec InputSurface）
 *
 * 核心机制：
 * 1. 配置 H.264 (video/avc) 编码器，颜色格式为 [MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface]；
 * 2. 导出 [inputSurface]，由 OpenGL 渲染线程将其包裹为 EGLWindowSurface 直接绘制，
 *    实现显存到编码器的 GPU 零拷贝流水线；
 * 3. 消费编码器输出缓冲，向 [MediaMuxerWrapper] 写入视频压缩采样包。
 */
class VideoEncoderCore(
    val width: Int,
    val height: Int,
    val bitRate: Int = 3_500_000,
    val frameRate: Int = 30,
    private val muxerWrapper: MediaMuxerWrapper,
) {

    private val codec: MediaCodec
    val inputSurface: Surface

    private var videoTrackIndex = -1
    private val bufferInfo = MediaCodec.BufferInfo()

    init {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = codec.createInputSurface()
        codec.start()
    }

    /**
     * 抽取并消费编码器中的输出帧
     */
    fun drainEncoder(endOfStream: Boolean) {
        if (endOfStream) {
            try {
                codec.signalEndOfInputStream()
            } catch (e: Exception) {
                Utils.logcat(TAG, "signalEndOfInputStream error: ${e.message}")
            }
        }

        val timeoutUs = 10_000L
        while (true) {
            val status = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            when {
                status == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!endOfStream) break
                }
                status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (videoTrackIndex >= 0) {
                        Utils.logcat(TAG, "video format changed twice")
                    } else {
                        val newFormat = codec.outputFormat
                        videoTrackIndex = muxerWrapper.addTrack(newFormat)
                        Utils.logcat(TAG, "Video encoder output format changed, trackIndex=$videoTrackIndex")
                    }
                }
                status >= 0 -> {
                    val encodedData = codec.getOutputBuffer(status)
                    if (encodedData != null) {
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            bufferInfo.size = 0
                        }

                        if (bufferInfo.size != 0 && muxerWrapper.isStarted()) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            muxerWrapper.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                        }

                        codec.releaseOutputBuffer(status, false)
                    }

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                }
            }
        }
    }

    fun release() {
        try {
            codec.stop()
        } catch (e: Exception) {
            Utils.logcat(TAG, "codec.stop error: ${e.message}")
        }
        try {
            codec.release()
        } catch (e: Exception) {
            Utils.logcat(TAG, "codec.release error: ${e.message}")
        }
        try {
            inputSurface.release()
        } catch (e: Exception) {
            Utils.logcat(TAG, "inputSurface.release error: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "VideoEncoderCore"
    }
}
