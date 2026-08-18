package com.example.william.my.module.feature.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Environment
import android.view.Surface
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.core.base.utils.AppExecutorsHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CameraX 相机功能辅助类
 *
 * 负责管理 CameraX 生命周期绑定、拍照取景、视频录制以及硬件回退策略。
 * 采用基于 Activity 生命周期的实例设计，避免在静态字段中持有 [ProcessCameraProvider] 或 Context 导致内存泄漏。
 */
class CameraHelper(
    private val activity: FragmentActivity,
    private val preview: PreviewView
) {

    private val main = AppExecutorsHelper.main()

    // 状态标志
    private var isRecording = false

    // 录像相关对象
    private var currentRecording: Recording? = null
    private var onRecordingStopped: ((file: File) -> Unit)? = null

    // 用例
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var imageCaptureUseCase: ImageCapture? = null
    private var videoCaptureUseCase: VideoCapture<Recorder>? = null

    fun isRecording(): Boolean = isRecording

    fun setupCamera() {
        preview.post {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

            cameraProviderFuture.addListener({
                try {
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider

                    val rotation = preview.display?.rotation ?: Surface.ROTATION_0

                    val resolutionSelector = ResolutionSelector.Builder()
                        .setAspectRatioStrategy(
                            AspectRatioStrategy(
                                AspectRatio.RATIO_16_9,
                                AspectRatioStrategy.FALLBACK_RULE_AUTO
                            )
                        )
                        .build()

                    // 预览配置
                    previewUseCase = Preview.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setTargetRotation(rotation)
                        .build()
                        .also {
                            it.setSurfaceProvider(preview.surfaceProvider)
                        }

                    // 图像捕获配置
                    imageCaptureUseCase = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setResolutionSelector(resolutionSelector)
                        .setTargetRotation(rotation)
                        .build()

                    // 录像配置：配置带回退的多级分辨率，防止部分机型/模拟器不支持 HIGHEST 崩溃
                    val qualitySelector = QualitySelector.fromOrderedList(
                        listOf(Quality.FHD, Quality.HD, Quality.SD),
                        FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                    )
                    val recorder = Recorder.Builder()
                        .setQualitySelector(qualitySelector)
                        .build()
                    videoCaptureUseCase = VideoCapture.withOutput(recorder)

                    // 选择后置摄像头
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    // 解除所有绑定
                    provider.unbindAll()

                    // 优先尝试同时绑定 Preview + ImageCapture + VideoCapture
                    try {
                        provider.bindToLifecycle(
                            activity,
                            cameraSelector,
                            previewUseCase,
                            imageCaptureUseCase,
                            videoCaptureUseCase
                        )
                    } catch (e: Exception) {
                        Utils.logcat(
                            TAG,
                            "设备不支持同时绑定 3 个用例 (${e.message})，回退为默认绑定 Preview + ImageCapture"
                        )
                        // 回退方案：默认绑定 Preview + ImageCapture
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            activity,
                            cameraSelector,
                            previewUseCase,
                            imageCaptureUseCase
                        )
                    }
                } catch (e: Exception) {
                    Utils.logcat(TAG, "setupCamera error: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(activity))
        }
    }

    fun captureImage(
        processComplete: (bitmap: Bitmap) -> Unit
    ) {
        val provider = cameraProvider ?: run {
            Utils.toast("相机未就绪，请稍候")
            return
        }
        val imageCapture = imageCaptureUseCase ?: return
        val preview = previewUseCase ?: return

        // 确保 ImageCapture 已绑定（若之前只绑定了录像组件则重新绑定）
        if (!provider.isBound(imageCapture)) {
            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    activity,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Utils.logcat(TAG, "bind imageCapture error: ${e.message}")
            }
        }

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(activity),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    processImage(image, processComplete)
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Utils.logcat(TAG, "captureImage error: ${exception.message}")
                    Utils.toast("拍照失败: ${exception.message}")
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    fun startRecording(
        processComplete: (file: File) -> Unit
    ) {
        val provider = cameraProvider
        val videoCapture = videoCaptureUseCase
        val preview = previewUseCase

        if (provider == null || videoCapture == null || preview == null) {
            Utils.toast("相机未就绪，无法录像")
            return
        }

        if (isRecording) {
            Utils.toast("录像已在进行中")
            return
        }

        // 确保 VideoCapture 已经绑定到生命周期（如果之前只绑定了拍照组件则通过 unbindAll 干净重绑）
        if (!provider.isBound(videoCapture)) {
            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    activity,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    videoCapture
                )
            } catch (e: Exception) {
                Utils.logcat(TAG, "绑定录像用例失败: ${e.message}")
                Utils.toast("启动录像失败: 无法绑定录像组件")
                return
            }
        }

        val videoFile = createVideoFile(activity)
        if (videoFile == null) {
            Utils.toast("创建视频文件失败")
            return
        }

        onRecordingStopped = processComplete

        try {
            val outputOptions = FileOutputOptions.Builder(videoFile).build()
            var pendingRecording = videoCapture.output.prepareRecording(activity, outputOptions)

            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    pendingRecording = pendingRecording.withAudioEnabled()
                } catch (e: Exception) {
                    Utils.logcat(TAG, "启用音频录制失败，进行无声录制: ${e.message}")
                }
            }

            currentRecording = pendingRecording.start(ContextCompat.getMainExecutor(activity)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        isRecording = true
                        Utils.logcat(TAG, "录像开始: ${videoFile.absolutePath}")
                    }

                    is VideoRecordEvent.Finalize -> {
                        isRecording = false
                        currentRecording = null
                        if (!event.hasError()) {
                            Utils.logcat(TAG, "录像完成: ${videoFile.absolutePath}")
                            if (videoFile.exists() && videoFile.length() > 0) {
                                onRecordingStopped?.invoke(videoFile)
                            } else {
                                Utils.toast("录像文件为空")
                            }
                        } else {
                            Utils.logcat(TAG, "录像异常: code=${event.error}, cause=${event.cause?.message}")
                            Utils.toast("录像异常: ${event.cause?.message ?: "错误码 ${event.error}"}")
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            isRecording = false
            currentRecording = null
            Utils.logcat(TAG, "startRecording exception: ${e.message}")
            Utils.toast("启动录像异常: ${e.message}")
        }
    }

    fun stopRecording() {
        if (!isRecording && currentRecording == null) {
            Utils.toast("当前没有在录像")
            return
        }
        try {
            currentRecording?.stop()
        } catch (e: Exception) {
            Utils.logcat(TAG, "stopRecording exception: ${e.message}")
        } finally {
            currentRecording = null
            isRecording = false
        }
    }

    fun release() {
        try {
            if (isRecording) {
                stopRecording()
            }
            onRecordingStopped = null
            cameraProvider?.unbindAll()
            cameraProvider = null
            previewUseCase = null
            imageCaptureUseCase = null
            videoCaptureUseCase = null
        } catch (e: Exception) {
            Utils.logcat(TAG, "release error: ${e.message}")
        }
    }

    private fun processImage(
        imageProxy: ImageProxy,
        processComplete: (bitmap: Bitmap) -> Unit
    ) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val rawBitmap = imageProxy.toBitmap()

        // 仅旋转 Bitmap 至屏幕正向，不做裁切，保持相机原生画幅与取景画面完全一致
        val rotatedBitmap = if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        } else {
            rawBitmap
        }

        main.execute {
            processComplete.invoke(rotatedBitmap)
        }
    }

    private fun createVideoFile(context: Context): File? {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return try {
            val file = File(storageDir, "VID_${timeStamp}.mp4")
            Utils.logcat(TAG, "预设视频路径: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Utils.logcat(TAG, "创建文件路径失败: ${e.message}")
            File(context.cacheDir, "VID_${timeStamp}.mp4")
        }
    }

    companion object {
        private const val TAG = "CameraHelper"
    }
}
