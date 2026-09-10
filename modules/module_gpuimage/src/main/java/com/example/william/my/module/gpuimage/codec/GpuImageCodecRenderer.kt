package com.example.william.my.module.gpuimage.codec

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.module.gpuimage.codec.encoder.VideoEncoderCore
import jp.co.cyberagent.android.gpuimage.GPUImageRenderer
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.util.Rotation
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.egl.EGLConfig as EGL10Config

/**
 * 录像专用 OpenGL ES 渲染包装器
 *
 * 核心设计：
 * 1. 代理持有 [GPUImageRenderer]，保留官方库对 60+ 种滤镜、Shader 切换与旋转矩阵的全部能力；
 * 2. 经典双 Surface 渲染机制：
 *    - 常规帧：直接渲染上屏至 [GLSurfaceView]；
 *    - 录制中：捕获当前 GL 线程的 EGLContext，在同一上下文内通过 [EGL14.eglMakeCurrent]
 *      将目标切换到 [VideoEncoderCore.inputSurface] 包装的 [encoderEglSurface]，
 *      重放渲染当前滤镜帧并注入 presentation timestamp，实现 GPU 纹理零拷贝硬编码。
 */
class GpuImageCodecRenderer(
    private val delegate: GPUImageRenderer,
) : GLSurfaceView.Renderer {

    private var screenWidth = 0
    private var screenHeight = 0

    @Volatile
    private var isRecording = false
    private var videoEncoder: VideoEncoderCore? = null
    private var encoderEglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var egl14Config: EGLConfig? = null

    override fun onSurfaceCreated(gl: GL10, config: EGL10Config) {
        delegate.onSurfaceCreated(gl, config)
        initEgl14Config()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        delegate.onSurfaceChanged(gl, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        // 1. 渲染当前滤镜画面到手机屏幕
        delegate.onDrawFrame(gl)

        // 2. 若当前正在录制，将同一滤镜帧绘制到 MediaCodec 的 InputSurface
        if (isRecording && encoderEglSurface != EGL14.EGL_NO_SURFACE) {
            val encoder = videoEncoder ?: return
            val display = EGL14.eglGetCurrentDisplay()
            val context = EGL14.eglGetCurrentContext()
            val screenDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW)
            val screenReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ)

            try {
                // 切换 EGL 目标至视频编码器输入表面
                EGL14.eglMakeCurrent(display, encoderEglSurface, encoderEglSurface, context)
                GLES20.glViewport(0, 0, encoder.width, encoder.height)

                // 重新渲染当前滤镜帧到编码器
                delegate.onDrawFrame(gl)

                // 写入当前帧高精度纳秒时间戳（至关重要，驱动编码器生成正确的 pts）
                val timestampNs = System.nanoTime()
                EGLExt.eglPresentationTimeANDROID(display, encoderEglSurface, timestampNs)
                EGL14.eglSwapBuffers(display, encoderEglSurface)

                // 消费编码器输出缓冲写入 Muxer
                encoder.drainEncoder(false)
            } catch (e: Exception) {
                Utils.logcat(TAG, "render to encoder surface error: ${e.message}")
            } finally {
                // 恢复屏幕绘制表面与视口
                EGL14.eglMakeCurrent(display, screenDrawSurface, screenReadSurface, context)
                GLES20.glViewport(0, 0, screenWidth, screenHeight)
            }
        }
    }

    private fun initEgl14Config() {
        val display = EGL14.eglGetCurrentDisplay()
        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL_RECORDABLE_ANDROID, 1,
            EGL14.EGL_NONE,
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(display, attribList, 0, configs, 0, 1, numConfigs, 0)
        egl14Config = configs[0]
    }

    /**
     * 必须在 GL 线程调用（通过 queueEvent 投递）
     */
    fun startRecording(encoder: VideoEncoderCore) {
        val display = EGL14.eglGetCurrentDisplay()
        val config = egl14Config
        if (display == EGL14.EGL_NO_DISPLAY || config == null) {
            Utils.logcat(TAG, "EGL 环境尚未就绪，无法创建编码 Surface")
            return
        }

        val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
        encoderEglSurface = EGL14.eglCreateWindowSurface(
            display,
            config,
            encoder.inputSurface,
            surfaceAttribs,
            0,
        )
        videoEncoder = encoder
        isRecording = true
        Utils.logcat(TAG, "GL 线程已成功创建编码 EGLSurface")
    }

    /**
     * 必须在 GL 线程调用（通过 queueEvent 投递）
     */
    fun stopRecording() {
        isRecording = false
        val display = EGL14.eglGetCurrentDisplay()

        videoEncoder?.drainEncoder(true)
        videoEncoder?.release()
        videoEncoder = null

        if (encoderEglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglDestroySurface(display, encoderEglSurface)
            encoderEglSurface = EGL14.EGL_NO_SURFACE
        }
        Utils.logcat(TAG, "GL 线程已释放编码 EGLSurface")
    }

    fun setFilter(filter: GPUImageFilter) {
        delegate.setFilter(filter)
    }

    fun setRotationCamera(rotation: Rotation, flipHorizontal: Boolean, flipVertical: Boolean) {
        delegate.setRotationCamera(rotation, flipHorizontal, flipVertical)
    }

    fun onPreviewFrame(data: ByteArray, width: Int, height: Int) {
        delegate.onPreviewFrame(data, width, height)
    }

    companion object {
        private const val TAG = "GpuImageCodecRenderer"
        private const val EGL_RECORDABLE_ANDROID = 0x3142
    }
}
