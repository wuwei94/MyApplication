package com.example.william.my.core.base.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.william.my.core.base.app.BaseApp
import java.io.File

/**
 * 音频录制与播放封装类（录音 + 播放双重能力）
 */
object AudioRecordPlayer {

    private val TAG = this.javaClass.simpleName

    private const val DEFAULT_AUDIO_RECORD_MAX_TIME = 60
    private const val MAGIC_NUMBER = 500
    private const val MIN_RECORD_DURATION = 1000

    var audioRecordPath: String? = null
        private set

    private var recorder: MediaRecorder? = null

    private var player: MediaPlayer? = null

    var isRecorded: Boolean = false

    val isPlaying: Boolean
        get() = player != null && player?.isPlaying == true

    private val handler: Handler = Handler(Looper.getMainLooper())

    // 录音

    fun startRecord(context: Context, callback: Callback) {
        recordCallback = callback
        try {
            audioRecordPath =
                context.applicationContext.externalCacheDir.toString() + File.separator +
                "auto_" + System.currentTimeMillis() + ".m4a"
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            // 使用mp4容器并且后缀改为.m4a，来兼容小程序的播放
            recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder?.setOutputFile(audioRecordPath)
            recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder?.prepare()
            recorder?.start()
            // 最大录制时间之后需要停止录制
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                show("已达到最大语音长度")
                stopRecord(true)
            }, (DEFAULT_AUDIO_RECORD_MAX_TIME * 1000).toLong())
        } catch (e: Exception) {
            println("startRecord failed")
            stopRecord(false)
        }
    }

    fun stopRecord(completed: Boolean = true) {
        stopInternalRecord()
        onRecordCompleted(completed)
        recordCallback = null
    }

    private fun stopInternalRecord() {
        handler.removeCallbacksAndMessages(null)
        recorder?.release()
        recorder = null
    }

    private fun onRecordCompleted(success: Boolean) {
        recordCallback?.onCompletion(success)
        recorder = null
        isRecorded = false
    }

    // 播放相关

    fun startPlay(filePath: String?, callback: Callback) {
        audioRecordPath = filePath
        playCallback = callback
        try {
            player = MediaPlayer()
            player?.setDataSource(audioRecordPath)
            player?.setOnCompletionListener {
                stopPlay(true)
            }
            player?.setOnPreparedListener {
                playCallback?.onStart()
                player?.start()
            }
            player?.prepareAsync()
        } catch (e: Exception) {
            println("startPlay failed")
            show("语音文件已损坏或不存在")
            stopPlay(false)
        }
    }

    fun stopPlay(completed: Boolean = false) {
        stopInternalPlay()
        onPlayCompleted(completed)
        playCallback = null
    }

    private fun stopInternalPlay() {
        player?.release()
        player = null
    }

    private fun onPlayCompleted(success: Boolean) {
        playCallback?.onCompletion(success)
        player = null
    }

    // 语音长度如果是59s多，因为外部会/1000取整，会一直显示59'，所以这里对长度进行处理，达到四舍五入的效果

    fun getDuration(): Int {
        if (TextUtils.isEmpty(audioRecordPath)) {
            return 0
        }
        var duration = 0
        var mp: MediaPlayer? = null
        try {
            mp = MediaPlayer()
            mp.setDataSource(audioRecordPath)
            mp.prepare()
            duration = mp.duration
            // 语音长度如果是59s多，因为外部会/1000取整，会一直显示59'，所以这里对长度进行处理，达到四舍五入的效果
            duration = if (duration < MIN_RECORD_DURATION) {
                0
            } else {
                duration + MAGIC_NUMBER
            }
        } catch (e: Exception) {
            println("getDuration failed")
        } finally {
            try {
                mp?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (duration < 0) {
            duration = 0
        }
        return duration
    }

    private var recordCallback: Callback? = null
    private var playCallback: Callback? = null

    interface Callback {
        fun onStart()
        fun onCompletion(success: Boolean?)
    }

    private fun show(msg: String) {
        Toast.makeText(BaseApp.app, msg, Toast.LENGTH_SHORT).show()
    }

    private fun println(msg: String) {
        Log.e(TAG, msg)
    }
}
