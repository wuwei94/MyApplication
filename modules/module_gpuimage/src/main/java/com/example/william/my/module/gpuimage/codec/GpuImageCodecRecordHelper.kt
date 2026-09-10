package com.example.william.my.module.gpuimage.codec

import android.annotation.SuppressLint
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
import com.example.william.my.module.gpuimage.codec.encoder.AudioEncoderCore
import com.example.william.my.module.gpuimage.codec.encoder.MediaMuxerWrapper
import com.example.william.my.module.gpuimage.codec.encoder.VideoEncoderCore
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
 * 经典 EGL 共享上下文 + MediaCodec 硬编录像辅助类
 *
 * 核心设计：
 * 1. 串联 CameraX 帧采集（ImageAnalysis + NV21 上传）与 GL 渲染线程；
 * 2. 在 GLSurfaceView 渲染管线中协调 [VideoEncoderCore]（H.264 视频硬编码）、
 *    [AudioEncoderCore]（AAC 音频硬编码）以及 [MediaMuxerWrapper]（MP4 混流封装）；
 * 3. 拍照采用高清离屏滤镜处理，录像生成纯净 MP4 本地文件。
 */
class GpuImageCodecRecordHelper(
    private val activity: FragmentActivity,
    private val glSurfaceView: GLSurfaceView,
) {

    private val baseRenderer = GPUImageRenderer(GPUImageFilter())
    private val renderer = GpuImageCodecRenderer(baseRenderer)

    private var cameraProvider: ProcessCameraProvider? = null
    private var analysisExecutor: ExecutorService? = null
    private var imageCaptureUseCase: ImageCapture? = null

    private var nv21Buffer: ByteArray? = null
    private var lastRotationDegrees = -1
    private var glInitialized = false

    private var currentFilter: GPUImageFilter = GPUImageFilter()
    private var currentFilterFactory: () -> GPUImageFilter = { GPUImageFilter() }

    @Volatile
    private var isRecording = false
    private var muxerWrapper: MediaMuxerWrapper? = null
    private var audioEncoder: AudioEncoderCore? = null
    private var onRecordingStopped: ((file: File) -> Unit)? = null

    fun isRecording(): Boolean = isRecording

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
        val filter = factory()
        currentFilter = filter
        renderer.setFilter(filter)
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

    @SuppressLint("MissingPermission")
    fun startRecording(onStopped: (file: File) -> Unit) {
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
            val muxer = MediaMuxerWrapper(videoFile, expectedTracks = 2)
            muxerWrapper = muxer

            // 720x1280 竖屏分辨率，3.5 Mbps 码率，30 fps 帧率
            val videoEncoder = VideoEncoderCore(
                width = 720,
                height = 1280,
                bitRate = 3_500_000,
                frameRate = 30,
                muxerWrapper = muxer,
            )

            val audioEnc = AudioEncoderCore(muxer)
            audioEncoder = audioEnc
            audioEnc.start()

            glSurfaceView.queueEvent {
                renderer.startRecording(videoEncoder)
            }
            isRecording = true
            Utils.logcat(TAG, "Codec 录像启动完成: ${videoFile.absolutePath}")
        } catch (e: Exception) {
            isRecording = false
            Utils.logcat(TAG, "startRecording error: ${e.message}")
            Utils.toast("启动录像异常: ${e.message}")
        }
    }

    fun stopRecording() {
        if (!isRecording) return
        isRecording = false

        glSurfaceView.queueEvent {
            renderer.stopRecording()
        }

        try {
            audioEncoder?.stop()
            audioEncoder?.release()
            audioEncoder = null

            muxerWrapper?.stopAndRelease()
            val outputFile = muxerWrapper?.outputFile
            muxerWrapper = null

            if (outputFile != null && outputFile.exists() && outputFile.length() > 0) {
                ContextCompat.getMainExecutor(activity).execute {
                    onRecordingStopped?.invoke(outputFile)
                }
            }
        } catch (e: Exception) {
            Utils.logcat(TAG, "stopRecording error: ${e.message}")
        }
    }

    fun onResume() {
        if (glInitialized) glSurfaceView.onResume()
    }

    fun onPause() {
        if (glInitialized) glSurfaceView.onPause()
    }

    fun release() {
        if (isRecording) {
            stopRecording()
        }
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
        return File(storageDir, "IMG_CODEC_$timeStamp.jpg")
    }

    private fun createVideoFile(context: Context): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        if (!storageDir.exists()) storageDir.mkdirs()
        return File(storageDir, "VID_CODEC_$timeStamp.mp4")
    }

    companion object {
        private const val TAG = "GpuImageCodecRecordHelper"

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
