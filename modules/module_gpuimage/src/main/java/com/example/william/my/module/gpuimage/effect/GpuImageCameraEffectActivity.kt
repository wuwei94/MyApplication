package com.example.william.my.module.gpuimage.effect

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
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.gpuimage.R
import com.example.william.my.module.gpuimage.databinding.GpuimageActivityCameraEffectBinding
import com.example.william.my.module.gpuimage.helper.GpuImageChipHelper
import java.io.File

/**
 * CameraX 1.3+ 官方 CameraEffect 特效滤镜录像示例
 *
 * 核心技术链路：
 * 1. 使用 [CameraEffect] + [GpuImageSurfaceProcessor] 将 OpenGL 滤镜挂载在
 *    相机硬件输出流与 [PreviewView] / [VideoCapture] 之间，实现 GPU 纹理级零拷贝实时着色。
 * 2. 视频录制直接交由 CameraX VideoCapture 与 Recorder，自动实现高质量音频采集、
 *    硬件 H.264 编码与音画同步混流。
 * 3. 录像回放：内嵌卡片式浮层，支持无缝重放生成的 MP4 视频。
 */
@Route(path = RouterPath.GpuImage.CameraEffect)
class GpuImageCameraEffectActivity :
    BaseVBActivity<GpuimageActivityCameraEffectBinding>(),
    View.OnClickListener {

    private val cameraEffectHelper by lazy {
        GpuImageCameraEffectHelper(this, mBinding.previewView)
    }

    private var mediaPlayer: MediaPlayer? = null
    private var currentVideoFile: File? = null

    private val filterSpecs = listOf(
        "原图" to GpuImageSurfaceProcessor.FILTER_NONE,
        "黑白" to GpuImageSurfaceProcessor.FILTER_GRAYSCALE,
        "复古" to GpuImageSurfaceProcessor.FILTER_SEPIA,
        "反色" to GpuImageSurfaceProcessor.FILTER_INVERT,
        "暖色" to GpuImageSurfaceProcessor.FILTER_WARM,
        "冷色" to GpuImageSurfaceProcessor.FILTER_COOL,
        "高对比" to GpuImageSurfaceProcessor.FILTER_CONTRAST,
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (cameraGranted) {
            cameraEffectHelper.setupCamera()
        } else {
            Utils.toast("未授予相机权限，无法使用滤镜录像")
        }
        if (!audioGranted) {
            Utils.toast("未授予麦克风权限，录像将无声")
        }
    }

    override fun getViewBinding(): GpuimageActivityCameraEffectBinding = GpuimageActivityCameraEffectBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        GpuImageChipHelper.populate(
            container = mBinding.chipContainer,
            names = filterSpecs.map { it.first },
            initialIndex = 0,
        ) { index ->
            val spec = filterSpecs[index]
            cameraEffectHelper.setFilterType(spec.second)
            mBinding.tvCurrentFilter.text = spec.first
        }

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
                currentVideoFile?.let { adjustTextureTransform(it) }
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                stopTexturePlayer()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (mBinding.layoutPreview.isVisible) {
                        closePreview()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )

        checkAndRequestPermissions()
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.btnRecord -> {
                if (cameraEffectHelper.isRecording()) {
                    updateRecordButton(false)
                    Utils.toast("正在停止录像并处理视频...")
                    cameraEffectHelper.stopRecording()
                } else {
                    updateRecordButton(true)
                    Utils.toast("开始录制带滤镜视频...")
                    cameraEffectHelper.startRecording { videoFile ->
                        updateRecordButton(false)
                        showVideoPreview(videoFile)
                    }
                }
            }

            mBinding.btnClosePreview -> {
                closePreview()
            }
        }
    }

    private fun updateRecordButton(recording: Boolean) {
        if (recording) {
            mBinding.btnRecord.setImageResource(R.drawable.gpuimage_ic_record_stop)
            mBinding.btnRecord.contentDescription = "停止录像"
        } else {
            mBinding.btnRecord.setImageResource(R.drawable.gpuimage_ic_record_start)
            mBinding.btnRecord.contentDescription = "开始录像"
        }
    }

    private fun checkAndRequestPermissions() {
        val hasCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val hasAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        if (hasCamera) {
            cameraEffectHelper.setupCamera()
        }
        if (!hasCamera || !hasAudio) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            )
        }
    }

    private fun showVideoPreview(videoFile: File) {
        mBinding.layoutPreview.visibility = View.VISIBLE

        currentVideoFile = videoFile
        if (mBinding.previewTexture.isAvailable) {
            mBinding.previewTexture.surfaceTexture?.let { st ->
                startTexturePlayer(Surface(st), videoFile)
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
                    Utils.logcat("CameraEffectActivity", "MediaPlayer error: what=$what extra=$extra")
                    Utils.toast("视频回放失败")
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Utils.logcat("CameraEffectActivity", "startTexturePlayer error: ${e.message}")
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
                Utils.logcat("CameraEffectActivity", "metadata error: ${e.message}")
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
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            Utils.logcat("CameraEffectActivity", "stopTexturePlayer error: ${e.message}")
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

    override fun fitsSystemWindows(): Boolean = false

    override fun onDestroy() {
        closePreview()
        cameraEffectHelper.release()
        super.onDestroy()
    }
}
