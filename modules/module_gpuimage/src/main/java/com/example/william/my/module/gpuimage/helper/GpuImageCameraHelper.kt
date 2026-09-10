package com.example.william.my.module.gpuimage.helper

import android.opengl.GLSurfaceView
import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.william.my.basic.basic_shared.utils.Utils
import jp.co.cyberagent.android.gpuimage.GPUImageRenderer
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.util.Rotation
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 相机实时帧滤镜辅助类
 *
 * 把 CameraX 的连续帧流接到 GPUImage 的渲染管线上，页面只负责权限与滤镜切换。
 *
 * 管线：`ImageAnalysis`（YUV_420_888 帧）→ [GpuImageYuvConverter] 转 NV21 →
 * `GPUImageRenderer#onPreviewFrame(byte[], int, int)` → GL 线程内 JNI 转 RGBA 并
 * 上传纹理 → 当前 `GPUImageFilter` 逐帧渲染到 [GLSurfaceView]。
 *
 * 说明：这里直接复用 `GPUImageRenderer` 作为 `GLSurfaceView.Renderer`，而不使用
 * `GPUImageView`——后者面向静态图（`setImage`）封装，未开放「外部帧源」入口；
 * 而 `onPreviewFrame` 是公开 API，正是库为相机预览预留的取帧口。
 *
 * 采用基于 Activity 生命周期的实例设计，避免在静态字段中持有 CameraProvider 或
 * Context 导致内存泄漏。
 */
class GpuImageCameraHelper(
    private val activity: FragmentActivity,
    private val glSurfaceView: GLSurfaceView,
) {

    private val renderer = GPUImageRenderer(GPUImageFilter())

    private var cameraProvider: ProcessCameraProvider? = null
    private var analysisExecutor: ExecutorService? = null
    private var nv21Buffer: ByteArray? = null
    private var lastRotationDegrees = -1
    private var glInitialized = false

    /**
     * 初始化 OpenGL 渲染环境。
     *
     * 渲染模式必须是 [GLSurfaceView.RENDERMODE_CONTINUOUSLY]：`GPUImageRenderer`
     * 的取帧方法只负责把「纹理上传」投递进 GL 线程队列，并不会主动请求重绘，
     * 需靠连续渲染循环消费队列并刷新画面。
     */
    fun setup() {
        if (glInitialized) return
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        glInitialized = true
    }

    /** 绑定 ImageAnalysis 用例开始取帧（调用前需已获得相机权限） */
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
                        // 只保留最新帧：渲染管线本就丢弃积压帧，避免帧队列堆积延迟
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { it.setAnalyzer(executor) { image -> analyze(image) } }

                    provider.unbindAll()
                    provider.bindToLifecycle(activity, CameraSelector.DEFAULT_BACK_CAMERA, analysis)
                } catch (e: Exception) {
                    Utils.logcat(TAG, "start error: ${e.message}")
                }
            },
            ContextCompat.getMainExecutor(activity),
        )
    }

    /**
     * 切换滤镜。
     *
     * `GPUImageRenderer#setFilter` 内部把滤镜替换与旧滤镜销毁投递到 GL 线程执行，
     * 因此可安全地在 UI 线程调用，无需页面自行处理线程切换。
     */
    fun setFilter(filter: GPUImageFilter) {
        renderer.setFilter(filter)
    }

    fun onResume() {
        if (glInitialized) glSurfaceView.onResume()
    }

    fun onPause() {
        if (glInitialized) glSurfaceView.onPause()
    }

    /** 解绑相机并关闭分析线程 */
    fun release() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        analysisExecutor?.shutdown()
        analysisExecutor = null
    }

    /** 分析线程回调：转 NV21 后交给渲染器上传纹理 */
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

    /**
     * 按相机帧的旋转角设置纹理坐标旋转，使画面正向显示。
     *
     * 旋转只改变纹理采样坐标、不重排像素，因此无需逐帧拷贝；同一角度只需设置一次。
     * 注意该调用会改写渲染器的纹理坐标缓冲，必须投递到 GL 线程执行。
     */
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

    companion object {
        private const val TAG = "GpuImageCameraHelper"

        /**
         * 帧分辨率策略：16:9 且不超过 720p。
         *
         * 该链路的每一帧都要经 JNI 转 RGBA 后再上传纹理，属于 CPU 侧开销，
         * 更高分辨率（1080p/4K）会显著掉帧，故以 720p 为性能与画质的折中。
         */
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
