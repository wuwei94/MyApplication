package com.example.william.my.module.media.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.media.R
import com.example.william.my.module.media.databinding.MediaActivityVideoBinding
import com.example.william.my.module.media.utils.VideoCaptureHelper
import java.io.File

/**
 * 录像示例 — 基于 CameraX 的 VideoCapture 用例，演示预览取景、录像与视频回放。
 */
@Route(path = RouterPath.Media.Video)
class MediaVideoActivity : BaseVBActivity<MediaActivityVideoBinding>(), View.OnClickListener {

    private var mediaPlayer: MediaPlayer? = null
    private var currentVideoFile: File? = null

    private val videoCaptureHelper by lazy {
        VideoCaptureHelper(this, mBinding.previewView)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        if (cameraGranted) {
            videoCaptureHelper.setupCamera()
        } else {
            Utils.toast("未授予相机权限，无法使用录像功能")
        }
    }

    override fun getViewBinding(): MediaActivityVideoBinding {
        return MediaActivityVideoBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        checkAndRequestPermissions()

        mBinding.btnRecord.setOnClickListener(this)
        mBinding.btnClosePreview.setOnClickListener(this)

        mBinding.previewTexture.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                currentVideoFile?.let {
                    adjustTextureTransform(it)
                    startTexturePlayer(Surface(surface), it)
                }
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                currentVideoFile?.let {
                    adjustTextureTransform(it)
                }
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                stopTexturePlayer()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mBinding.layoutPreview.isVisible) {
                    closePreview()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.btnRecord -> {
                if (videoCaptureHelper.isRecording()) {
                    Utils.toast("录像已停止，正在生成预览...")
                    updateRecordButtonState(false)
                    videoCaptureHelper.stopRecording()
                } else {
                    Utils.toast("开始录像...")
                    updateRecordButtonState(true)
                    videoCaptureHelper.startRecording { videoFile ->
                        updateRecordButtonState(false)
                        showVideoPreview(videoFile)
                    }
                }
            }

            mBinding.btnClosePreview -> {
                closePreview()
            }
        }
    }

    private fun updateRecordButtonState(isRecording: Boolean) {
        if (isRecording) {
            mBinding.btnRecord.setImageResource(R.drawable.media_ic_record_stop)
            mBinding.btnRecord.contentDescription = "停止录像"
        } else {
            mBinding.btnRecord.setImageResource(R.drawable.media_ic_record_start)
            mBinding.btnRecord.contentDescription = "开始录像"
        }
    }

    private fun checkAndRequestPermissions() {
        val hasCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val hasAudio = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCamera && hasAudio) {
            videoCaptureHelper.setupCamera()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    private fun showVideoPreview(videoFile: File) {
        if (!videoFile.exists() || videoFile.length() == 0L) {
            Utils.toast("录像文件无效")
            return
        }

        mBinding.layoutPreview.visibility = View.VISIBLE

        currentVideoFile = videoFile
        if (mBinding.previewTexture.isAvailable) {
            mBinding.previewTexture.surfaceTexture?.let { surfaceTexture ->
                startTexturePlayer(Surface(surfaceTexture), videoFile)
            }
        }
    }

    private fun startTexturePlayer(surface: Surface, videoFile: File) {
        try {
            stopTexturePlayer()
            mediaPlayer = MediaPlayer().apply {
                setSurface(surface)
                setDataSource(videoFile.absolutePath)
                isLooping = true
                setOnPreparedListener { mp ->
                    adjustTextureTransform(videoFile)
                    mp.start()
                }
                setOnErrorListener { _, what, extra ->
                    Utils.logcat("MediaVideoActivity", "MediaPlayer playback error: what=$what extra=$extra")
                    Utils.toast("视频播放失败")
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Utils.logcat("MediaVideoActivity", "startTexturePlayer error: ${e.message}")
        }
    }

    private fun adjustTextureTransform(videoFile: File) {
        mBinding.previewTexture.post {
            val viewWidth = mBinding.previewTexture.width
            val viewHeight = mBinding.previewTexture.height
            if (viewWidth <= 0 || viewHeight <= 0) return@post

            var videoWidth = 0.0
            var videoHeight = 0.0

            try {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(videoFile.absolutePath)
                    val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
                    val rawWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toDoubleOrNull() ?: 0.0
                    val rawHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toDoubleOrNull() ?: 0.0

                    if (rotation == 90 || rotation == 270) {
                        videoWidth = rawHeight
                        videoHeight = rawWidth
                    } else {
                        videoWidth = rawWidth
                        videoHeight = rawHeight
                    }
                } finally {
                    retriever.release()
                }
            } catch (e: Exception) {
                Utils.logcat("MediaVideoActivity", "extract video metadata error: ${e.message}")
            }

            if (videoWidth <= 0 || videoHeight <= 0) {
                videoWidth = (mediaPlayer?.videoWidth ?: 0).toDouble()
                videoHeight = (mediaPlayer?.videoHeight ?: 0).toDouble()
            }

            if (videoWidth <= 0 || videoHeight <= 0) return@post

            val viewRatio = viewHeight.toDouble() / viewWidth.toDouble()
            val videoRatio = videoHeight / videoWidth

            val scaleX: Float
            val scaleY: Float

            if (videoRatio < viewRatio) {
                scaleX = (viewRatio / videoRatio).toFloat()
                scaleY = 1f
            } else {
                scaleX = 1f
                scaleY = (videoRatio / viewRatio).toFloat()
            }

            val matrix = Matrix()
            matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
            mBinding.previewTexture.setTransform(matrix)
        }
    }

    private fun stopTexturePlayer() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Utils.logcat("MediaVideoActivity", "stopTexturePlayer error: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    private fun closePreview() {
        stopTexturePlayer()
        currentVideoFile = null
        mBinding.layoutPreview.visibility = View.GONE
        mBinding.previewTexture.setTransform(Matrix())
    }

    override fun fitsSystemWindows(): Boolean {
        return false
    }

    override fun onDestroy() {
        closePreview()
        updateRecordButtonState(false)
        videoCaptureHelper.release()
        super.onDestroy()
    }
}
