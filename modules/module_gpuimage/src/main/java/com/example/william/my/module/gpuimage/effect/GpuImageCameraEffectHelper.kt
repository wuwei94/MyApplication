package com.example.william.my.module.gpuimage.effect

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Environment
import android.view.Surface
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraEffect
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
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
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageRGBFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CameraX 1.3+ CameraEffect 拍摄辅助类
 *
 * 核心设计：
 * 1. 利用 CameraX [CameraEffect] 在流媒体管道中同时为 Preview 与 VideoCapture 注入
 *    OpenGL 滤镜（[GpuImageSurfaceProcessor]），实现零拷贝的硬件级滤镜渲染。
 * 2. 视频录制直接交由 CameraX [VideoCapture] 与 [Recorder]，自动实现高质量音频采集、
 *    硬件 H.264 编码与音画同步混流。
 * 3. 拍照采用高低分工双轨设计：[ImageCapture] 捕获相机传感器全尺寸高清画质原始帧，
 *    通过 [GPUImage] 离屏渲染当前滤镜 Shader，产出高清全像素滤镜照片，杜绝屏幕截图伪劣画质。
 */
class GpuImageCameraEffectHelper(
    private val activity: FragmentActivity,
    private val previewView: PreviewView,
) {

    private val mainExecutor = ContextCompat.getMainExecutor(activity)
    private val surfaceProcessor = GpuImageSurfaceProcessor()

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var imageCaptureUseCase: ImageCapture? = null
    private var videoCaptureUseCase: VideoCapture<Recorder>? = null

    private var isRecording = false
    private var currentRecording: Recording? = null
    private var onRecordingStopped: ((file: File) -> Unit)? = null

    private var currentFilterType = GpuImageSurfaceProcessor.FILTER_NONE

    fun isRecording(): Boolean = isRecording

    fun setFilterType(filterType: Int) {
        currentFilterType = filterType
        surfaceProcessor.setFilterType(filterType)
    }

    fun setupCamera() {
        previewView.post {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
            cameraProviderFuture.addListener(
                {
                    try {
                        val provider = cameraProviderFuture.get()
                        cameraProvider = provider

                        val rotation = previewView.display?.rotation ?: Surface.ROTATION_0

                        val resolutionSelector = ResolutionSelector.Builder()
                            .setAspectRatioStrategy(
                                AspectRatioStrategy(
                                    AspectRatio.RATIO_16_9,
                                    AspectRatioStrategy.FALLBACK_RULE_AUTO,
                                ),
                            )
                            .build()

                        previewUseCase = Preview.Builder()
                            .setResolutionSelector(resolutionSelector)
                            .setTargetRotation(rotation)
                            .build()
                            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                        imageCaptureUseCase = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .setResolutionSelector(resolutionSelector)
                            .setTargetRotation(rotation)
                            .build()

                        val qualitySelector = QualitySelector.fromOrderedList(
                            listOf(Quality.FHD, Quality.HD, Quality.SD),
                            FallbackStrategy.lowerQualityOrHigherThan(Quality.SD),
                        )
                        val recorder = Recorder.Builder()
                            .setQualitySelector(qualitySelector)
                            .build()
                        videoCaptureUseCase = VideoCapture.withOutput(recorder)

                        // 创建 CameraEffect，作用于 PREVIEW 和 VIDEO_CAPTURE 两个用例
                        val cameraEffect = object : CameraEffect(
                            PREVIEW or VIDEO_CAPTURE,
                            surfaceProcessor.glExecutor,
                            surfaceProcessor,
                            { throwable ->
                                Utils.logcat(TAG, "CameraEffect error: ${throwable.message}")
                            },
                        ) {}

                        val useCaseGroup = UseCaseGroup.Builder()
                            .addUseCase(previewUseCase!!)
                            .addUseCase(imageCaptureUseCase!!)
                            .addUseCase(videoCaptureUseCase!!)
                            .addEffect(cameraEffect)
                            .build()

                        provider.unbindAll()
                        provider.bindToLifecycle(
                            activity,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            useCaseGroup,
                        )
                    } catch (e: Exception) {
                        Utils.logcat(TAG, "setupCamera error: ${e.message}")
                    }
                },
                mainExecutor,
            )
        }
    }

    /**
     * 捕获全尺寸高清滤镜照片
     */
    fun capturePhoto(onSuccess: (bitmap: Bitmap, file: File) -> Unit) {
        val imageCapture = imageCaptureUseCase ?: run {
            Utils.toast("相机未就绪，无法拍照")
            return
        }

        imageCapture.takePicture(
            mainExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    processFilteredPhoto(image, onSuccess)
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Utils.logcat(TAG, "capturePhoto error: ${exception.message}")
                    Utils.toast("拍照失败: ${exception.message}")
                }
            },
        )
    }

    private fun processFilteredPhoto(
        imageProxy: ImageProxy,
        onSuccess: (bitmap: Bitmap, file: File) -> Unit,
    ) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val rawBitmap = imageProxy.toBitmap()

        val rotatedBitmap = if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        } else {
            rawBitmap
        }

        // 使用 GPUImage 进行全分辨率离屏渲染
        val gpuImageFilter = getGpuImageFilter(currentFilterType)
        val gpuImage = GPUImage(activity).apply {
            setFilter(gpuImageFilter)
        }
        val filteredBitmap = gpuImage.getBitmapWithFilterApplied(rotatedBitmap)

        val photoFile = createPhotoFile(activity)
        if (photoFile != null) {
            try {
                FileOutputStream(photoFile).use { out ->
                    filteredBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
            } catch (e: Exception) {
                Utils.logcat(TAG, "save photo file error: ${e.message}")
            }
        }

        mainExecutor.execute {
            if (photoFile != null) {
                onSuccess.invoke(filteredBitmap, photoFile)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecording(onStopped: (file: File) -> Unit) {
        val videoCapture = videoCaptureUseCase ?: run {
            Utils.toast("相机未就绪，无法录像")
            return
        }

        if (isRecording) {
            Utils.toast("录像已在进行中")
            return
        }

        val videoFile = createVideoFile(activity) ?: run {
            Utils.toast("创建视频文件失败")
            return
        }

        onRecordingStopped = onStopped

        try {
            val outputOptions = FileOutputOptions.Builder(videoFile).build()
            var pendingRecording = videoCapture.output.prepareRecording(activity, outputOptions)

            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.RECORD_AUDIO,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    pendingRecording = pendingRecording.withAudioEnabled()
                } catch (e: Exception) {
                    Utils.logcat(TAG, "开启音频录制失败: ${e.message}")
                }
            }

            currentRecording = pendingRecording.start(mainExecutor) { event ->
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
                            Utils.logcat(TAG, "录像失败: error=${event.error}, cause=${event.cause?.message}")
                            Utils.toast("录像失败: ${event.cause?.message ?: "错误码 ${event.error}"}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            isRecording = false
            currentRecording = null
            Utils.logcat(TAG, "startRecording exception: ${e.message}")
            Utils.toast("启动录像异常: ${e.message}")
        }
    }

    fun stopRecording() {
        if (!isRecording && currentRecording == null) {
            Utils.toast("当前没有录像任务")
            return
        }
        try {
            currentRecording?.stop()
        } catch (e: Exception) {
            Utils.logcat(TAG, "stopRecording error: ${e.message}")
        } finally {
            currentRecording = null
            isRecording = false
        }
    }

    fun release() {
        if (isRecording) {
            stopRecording()
        }
        onRecordingStopped = null
        cameraProvider?.unbindAll()
        cameraProvider = null
        previewUseCase = null
        imageCaptureUseCase = null
        videoCaptureUseCase = null
        surfaceProcessor.release()
    }

    private fun getGpuImageFilter(filterType: Int): GPUImageFilter = when (filterType) {
        GpuImageSurfaceProcessor.FILTER_GRAYSCALE -> GPUImageGrayscaleFilter()
        GpuImageSurfaceProcessor.FILTER_SEPIA -> GPUImageSepiaToneFilter()
        GpuImageSurfaceProcessor.FILTER_INVERT -> GPUImageColorInvertFilter()
        GpuImageSurfaceProcessor.FILTER_WARM -> GPUImageRGBFilter(1.15f, 1.05f, 0.85f)
        GpuImageSurfaceProcessor.FILTER_COOL -> GPUImageRGBFilter(0.85f, 1.05f, 1.20f)
        GpuImageSurfaceProcessor.FILTER_CONTRAST -> GPUImageContrastFilter(1.4f)
        else -> GPUImageFilter()
    }

    private fun createPhotoFile(context: Context): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
        if (!storageDir.exists()) storageDir.mkdirs()
        return File(storageDir, "IMG_FILTER_$timeStamp.jpg")
    }

    private fun createVideoFile(context: Context): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        if (!storageDir.exists()) storageDir.mkdirs()
        return File(storageDir, "VID_FILTER_$timeStamp.mp4")
    }

    companion object {
        private const val TAG = "GpuImageCameraEffectHelper"
    }
}
