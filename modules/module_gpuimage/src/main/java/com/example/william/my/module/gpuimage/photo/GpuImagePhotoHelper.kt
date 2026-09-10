package com.example.william.my.module.gpuimage.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.opengl.GLSurfaceView
import android.os.Environment
import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.module.gpuimage.helper.GpuImageYuvConverter
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.GPUImageRenderer
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.util.Rotation
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 滤镜拍照专项辅助类
 *
 * 核心机制：
 * 1. 实时预览：CameraX [ImageAnalysis] 连续帧流 $\rightarrow$ [GPUImageRenderer] 实时着色上屏；
 * 2. 高清捕获：点击拍照时由 [ImageCapture] 捕获相机传感器全尺寸大图（千万级像素）；
 * 3. 离屏渲染：使用 [GPUImage] 离屏应用当前选中的同款 Shader 滤镜，保存高清 JPG 文件。
 */
class GpuImagePhotoHelper(
    private val activity: FragmentActivity,
    private val glSurfaceView: GLSurfaceView,
) {

    private val renderer = GPUImageRenderer(GPUImageFilter())

    private var cameraProvider: ProcessCameraProvider? = null
    private var analysisExecutor: ExecutorService? = null
    private var imageCaptureUseCase: ImageCapture? = null

    private var nv21Buffer: ByteArray? = null
    private var lastRotationDegrees = -1
    private var glInitialized = false

    private var currentFilterFactory: () -> GPUImageFilter = { GPUImageFilter() }

    fun setup() {
        if (glInitialized) return
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        glInitialized = true
    }

    fun start() {
        val executor = Executors.newSingleThreadExecutor()
        analysisExecutor = executor

        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener(
            {
                try {
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider

                    val analysis = ImageAnalysis.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { it.setAnalyzer(executor) { image -> analyze(image) } }

                    imageCaptureUseCase = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setResolutionSelector(resolutionSelector)
                        .build()

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        activity,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        analysis,
                        imageCaptureUseCase,
                    )
                } catch (e: Exception) {
                    Utils.logcat(TAG, "start error: ${e.message}")
                }
            },
            ContextCompat.getMainExecutor(activity),
        )
    }

    fun setFilter(factory: () -> GPUImageFilter) {
        currentFilterFactory = factory
        renderer.setFilter(factory())
    }

    fun capturePhoto(onSuccess: (bitmap: Bitmap, file: File) -> Unit) {
        val imageCapture = imageCaptureUseCase ?: run {
            Utils.toast("相机未就绪，无法拍照")
            return
        }

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(activity),
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

        val filter = currentFilterFactory()
        val gpuImage = GPUImage(activity).apply {
            setFilter(filter)
        }
        val filteredBitmap = gpuImage.getBitmapWithFilterApplied(rotatedBitmap)

        val photoFile = createPhotoFile(activity)
        if (photoFile != null) {
            try {
                FileOutputStream(photoFile).use { out ->
                    filteredBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
            } catch (e: Exception) {
                Utils.logcat(TAG, "save photo error: ${e.message}")
            }
        }

        ContextCompat.getMainExecutor(activity).execute {
            if (photoFile != null) {
                onSuccess.invoke(filteredBitmap, photoFile)
            }
        }
    }

    fun onResume() {
        if (glInitialized) glSurfaceView.onResume()
    }

    fun onPause() {
        if (glInitialized) glSurfaceView.onPause()
    }

    fun release() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        analysisExecutor?.shutdown()
        analysisExecutor = null
    }

    private fun analyze(image: ImageProxy) {
        try {
            applyRotationIfNeeded(image.imageInfo.rotationDegrees)

            val frameSize = GpuImageYuvConverter.nv21Size(image.width, image.height)
            val buffer = nv21Buffer?.takeIf { it.size == frameSize }
                ?: ByteArray(frameSize).also { nv21Buffer = it }

            GpuImageYuvConverter.toNv21(image, buffer)
            renderer.onPreviewFrame(buffer, image.width, image.height)
        } catch (e: Exception) {
            Utils.logcat(TAG, "analyze error: ${e.message}")
        } finally {
            image.close()
        }
    }

    private fun applyRotationIfNeeded(degrees: Int) {
        if (degrees == lastRotationDegrees) return
        lastRotationDegrees = degrees

        val rotation = when (degrees) {
            90 -> Rotation.ROTATION_90
            180 -> Rotation.ROTATION_180
            270 -> Rotation.ROTATION_270
            else -> Rotation.NORMAL
        }
        glSurfaceView.queueEvent { renderer.setRotationCamera(rotation, false, false) }
    }

    private fun createPhotoFile(context: Context): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
        if (!storageDir.exists()) storageDir.mkdirs()
        return File(storageDir, "IMG_FILTER_$timeStamp.jpg")
    }

    companion object {
        private const val TAG = "GpuImagePhotoHelper"

        private val resolutionSelector: ResolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    AspectRatio.RATIO_16_9,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO,
                ),
            )
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(1280, 720),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER,
                ),
            )
            .build()
    }
}
