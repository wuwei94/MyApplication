package com.example.william.my.module.gpuimage.codec.encoder

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder
import com.example.william.my.basic.basic_shared.utils.Utils

/**
 * 硬件音频编码核心类（基于 AudioRecord + AAC MediaCodec）
 *
 * 核心机制：
 * 1. 启动 [AudioRecord] 线程实时采集麦克风 16-bit PCM 音频流；
 * 2. 投递到 AAC (audio/mp4a-latm) [MediaCodec] 编码器；
 * 3. 将编码后的 AAC 音频包写入 [MediaMuxerWrapper]，协同实现音视频同步录制。
 */
class AudioEncoderCore(
    private val muxerWrapper: MediaMuxerWrapper,
    private val sampleRate: Int = 44100,
    private val channelCount: Int = 1,
    private val bitRate: Int = 64000,
) {

    private val codec: MediaCodec
    private var audioRecord: AudioRecord? = null

    @Volatile
    private var isRecording = false
    private var recordingThread: Thread? = null

    private var audioTrackIndex = -1
    private val bufferInfo = MediaCodec.BufferInfo()

    init {
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
        }

        codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()
    }

    @SuppressLint("MissingPermission")
    fun start() {
        val channelConfig = if (channelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT)
        val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
            )
            audioRecord?.startRecording()
        } catch (e: Exception) {
            Utils.logcat(TAG, "AudioRecord init failed: ${e.message}")
            return
        }

        isRecording = true
        recordingThread = Thread({
            val audioBuffer = ByteArray(bufferSize)
            var presentationTimeUs = 0L

            while (isRecording) {
                val readBytes = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: -1
                if (readBytes > 0) {
                    feedAudioData(audioBuffer, readBytes, presentationTimeUs)
                    // 每采样帧时长（微秒）：(samples / sampleRate) * 1_000_000
                    val samples = readBytes / (2 * channelCount)
                    presentationTimeUs += (samples * 1_000_000L) / sampleRate
                    drainAudio(false)
                }
            }

            drainAudio(true)
        }, "GpuImageAudioRecordThread").also { it.start() }
    }

    private fun feedAudioData(data: ByteArray, length: Int, pts: Long) {
        val inputIndex = codec.dequeueInputBuffer(10_000L)
        if (inputIndex >= 0) {
            val inputBuffer = codec.getInputBuffer(inputIndex)
            inputBuffer?.clear()
            inputBuffer?.put(data, 0, length)
            codec.queueInputBuffer(inputIndex, 0, length, pts, 0)
        }
    }

    private fun drainAudio(endOfStream: Boolean) {
        if (endOfStream) {
            val inputIndex = codec.dequeueInputBuffer(10_000L)
            if (inputIndex >= 0) {
                codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            }
        }

        while (true) {
            val status = codec.dequeueOutputBuffer(bufferInfo, 10_000L)
            when {
                status == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!endOfStream) break
                }
                status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (audioTrackIndex >= 0) {
                        Utils.logcat(TAG, "audio format changed twice")
                    } else {
                        audioTrackIndex = muxerWrapper.addTrack(codec.outputFormat)
                        Utils.logcat(TAG, "Audio encoder format changed, trackIndex=$audioTrackIndex")
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
                            muxerWrapper.writeSampleData(audioTrackIndex, encodedData, bufferInfo)
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

    fun stop() {
        isRecording = false
        try {
            recordingThread?.join(1000)
        } catch (e: Exception) {
            Utils.logcat(TAG, "thread join error: ${e.message}")
        }
        recordingThread = null

        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            Utils.logcat(TAG, "audioRecord.stop error: ${e.message}")
        }
        try {
            audioRecord?.release()
        } catch (e: Exception) {
            Utils.logcat(TAG, "audioRecord.release error: ${e.message}")
        }
        audioRecord = null
    }

    fun release() {
        stop()
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
    }

    companion object {
        private const val TAG = "AudioEncoderCore"
    }
}
