package com.example.william.my.module.gpuimage.effect

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import androidx.camera.core.SurfaceOutput
import androidx.camera.core.SurfaceProcessor
import androidx.camera.core.SurfaceRequest
import com.example.william.my.basic.basic_shared.utils.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.Executor

/**
 * CameraX 1.3+ 官方 [SurfaceProcessor] 实现
 *
 * 核心职责：
 * 1. 独立 GL 线程维护 EGLContext 与 EGLDisplay 环境；
 * 2. 输入端：以 OES 外部纹理接收相机传感器数据流，向 CameraX 供给 [SurfaceRequest]；
 * 3. 输出端：支持多路 [SurfaceOutput]（取景 Preview 与视频录制 VideoCapture），按需构建 EGLWindowSurface；
 * 4. 逐帧渲染：每当相机帧到达，执行统一的 OpenGL ES 2.0 滤镜 Shader，同时上屏显示并写入视频编码器，
 *    通过 [EGLExt.eglPresentationTimeANDROID] 保持高精度时间戳，确保音视频录制帧率与音画同步。
 */
class GpuImageSurfaceProcessor :
    SurfaceProcessor,
    SurfaceTexture.OnFrameAvailableListener {

    private val glThread = HandlerThread("GpuImageEffectGLThread").apply { start() }
    private val glHandler = Handler(glThread.looper)
    val glExecutor = Executor { command -> glHandler.post(command) }

    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglConfig: EGLConfig? = null
    private var pbufferSurface: EGLSurface = EGL14.EGL_NO_SURFACE

    private var oesTextureId: Int = 0
    private var surfaceTexture: SurfaceTexture? = null
    private var inputSurface: Surface? = null

    private var programId: Int = 0
    private var aPositionLoc: Int = 0
    private var aTexCoordLoc: Int = 0
    private var uTexMatrixLoc: Int = 0
    private var uFilterTypeLoc: Int = 0

    @Volatile
    private var currentFilterType: Int = FILTER_NONE

    private val inputMatrix = FloatArray(16)
    private val outputMatrix = FloatArray(16)

    private val vertexBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer

    private class OutputSurfaceHolder(
        val eglSurface: EGLSurface,
        val outputSurface: Surface,
        val output: SurfaceOutput,
    )

    private val outputHolders = mutableMapOf<SurfaceOutput, OutputSurfaceHolder>()

    init {
        // 全屏顶点四边形（NDC 坐标）
        val vertices = floatArrayOf(
            -1.0f,
            -1.0f,
            1.0f,
            -1.0f,
            -1.0f,
            1.0f,
            1.0f,
            1.0f,
        )
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
        vertexBuffer.position(0)

        // 纹理坐标（常规排布）
        val texCoords = floatArrayOf(
            0.0f,
            0.0f,
            1.0f,
            0.0f,
            0.0f,
            1.0f,
            1.0f,
            1.0f,
        )
        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(texCoords)
        texCoordBuffer.position(0)

        glHandler.post { initGL() }
    }

    fun setFilterType(type: Int) {
        currentFilterType = type
    }

    private fun initGL() {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            Utils.logcat(TAG, "eglGetDisplay failed")
            return
        }

        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            Utils.logcat(TAG, "eglInitialize failed")
            return
        }

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
        EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, 1, numConfigs, 0)
        eglConfig = configs[0]

        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION,
            2,
            EGL14.EGL_NONE,
        )
        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)

        val pbufferAttribs = intArrayOf(
            EGL14.EGL_WIDTH,
            1,
            EGL14.EGL_HEIGHT,
            1,
            EGL14.EGL_NONE,
        )
        pbufferSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, pbufferAttribs, 0)
        EGL14.eglMakeCurrent(eglDisplay, pbufferSurface, pbufferSurface, eglContext)

        val texIds = IntArray(1)
        GLES20.glGenTextures(1, texIds, 0)
        oesTextureId = texIds[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        initShader()
    }

    private fun initShader() {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        programId = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        aPositionLoc = GLES20.glGetAttribLocation(programId, "aPosition")
        aTexCoordLoc = GLES20.glGetAttribLocation(programId, "aTexCoord")
        uTexMatrixLoc = GLES20.glGetUniformLocation(programId, "uTexMatrix")
        uFilterTypeLoc = GLES20.glGetUniformLocation(programId, "uFilterType")
    }

    override fun onInputSurface(request: SurfaceRequest) {
        glHandler.post {
            try {
                surfaceTexture?.release()
                inputSurface?.release()

                val st = SurfaceTexture(oesTextureId).also {
                    it.setDefaultBufferSize(request.resolution.width, request.resolution.height)
                    it.setOnFrameAvailableListener(this, glHandler)
                }
                surfaceTexture = st
                val surface = Surface(st)
                inputSurface = surface

                request.provideSurface(surface, glExecutor) { result ->
                    glHandler.post {
                        surface.release()
                        st.release()
                        if (inputSurface == surface) inputSurface = null
                        if (surfaceTexture == st) surfaceTexture = null
                    }
                }
            } catch (e: Exception) {
                Utils.logcat(TAG, "onInputSurface error: ${e.message}")
            }
        }
    }

    override fun onOutputSurface(output: SurfaceOutput) {
        glHandler.post {
            try {
                val surface = output.getSurface(glExecutor) { event ->
                    if (event.eventCode == SurfaceOutput.Event.EVENT_REQUEST_CLOSE) {
                        glHandler.post {
                            outputHolders.remove(output)?.let { holder ->
                                EGL14.eglDestroySurface(eglDisplay, holder.eglSurface)
                            }
                            output.close()
                        }
                    }
                }

                val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
                val eglSurface = EGL14.eglCreateWindowSurface(
                    eglDisplay,
                    eglConfig,
                    surface,
                    surfaceAttribs,
                    0,
                )
                outputHolders[output] = OutputSurfaceHolder(eglSurface, surface, output)
            } catch (e: Exception) {
                Utils.logcat(TAG, "onOutputSurface error: ${e.message}")
            }
        }
    }

    override fun onFrameAvailable(st: SurfaceTexture?) {
        val texture = surfaceTexture ?: return
        try {
            texture.updateTexImage()
            texture.getTransformMatrix(inputMatrix)
            val timestamp = texture.timestamp

            GLES20.glUseProgram(programId)
            GLES20.glUniform1i(uFilterTypeLoc, currentFilterType)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)

            GLES20.glEnableVertexAttribArray(aPositionLoc)
            GLES20.glVertexAttribPointer(aPositionLoc, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)

            GLES20.glEnableVertexAttribArray(aTexCoordLoc)
            GLES20.glVertexAttribPointer(aTexCoordLoc, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)

            for (holder in outputHolders.values) {
                holder.output.updateTransformMatrix(outputMatrix, inputMatrix)
                GLES20.glUniformMatrix4fv(uTexMatrixLoc, 1, false, outputMatrix, 0)

                EGL14.eglMakeCurrent(eglDisplay, holder.eglSurface, holder.eglSurface, eglContext)
                GLES20.glViewport(0, 0, holder.output.size.width, holder.output.size.height)

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

                EGLExt.eglPresentationTimeANDROID(eglDisplay, holder.eglSurface, timestamp)
                EGL14.eglSwapBuffers(eglDisplay, holder.eglSurface)
            }

            GLES20.glDisableVertexAttribArray(aPositionLoc)
            GLES20.glDisableVertexAttribArray(aTexCoordLoc)
        } catch (e: Exception) {
            Utils.logcat(TAG, "onFrameAvailable render error: ${e.message}")
        }
    }

    fun release() {
        glHandler.post {
            for (holder in outputHolders.values) {
                EGL14.eglDestroySurface(eglDisplay, holder.eglSurface)
                holder.output.close()
            }
            outputHolders.clear()

            surfaceTexture?.release()
            surfaceTexture = null
            inputSurface?.release()
            inputSurface = null

            if (oesTextureId != 0) {
                GLES20.glDeleteTextures(1, intArrayOf(oesTextureId), 0)
                oesTextureId = 0
            }
            if (programId != 0) {
                GLES20.glDeleteProgram(programId)
                programId = 0
            }

            if (pbufferSurface != EGL14.EGL_NO_SURFACE) {
                EGL14.eglDestroySurface(eglDisplay, pbufferSurface)
                pbufferSurface = EGL14.EGL_NO_SURFACE
            }
            if (eglContext != EGL14.EGL_NO_CONTEXT) {
                EGL14.eglDestroyContext(eglDisplay, eglContext)
                eglContext = EGL14.EGL_NO_CONTEXT
            }
            if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglTerminate(eglDisplay)
                eglDisplay = EGL14.EGL_NO_DISPLAY
            }

            glThread.quitSafely()
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int = GLES20.glCreateShader(type).also { shader ->
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shader)
            Utils.logcat(TAG, "loadShader compilation error: $log")
            GLES20.glDeleteShader(shader)
        }
    }

    companion object {
        private const val TAG = "GpuImageSurfaceProcessor"
        private const val EGL_RECORDABLE_ANDROID = 0x3142

        const val FILTER_NONE = 0
        const val FILTER_GRAYSCALE = 1
        const val FILTER_SEPIA = 2
        const val FILTER_INVERT = 3
        const val FILTER_WARM = 4
        const val FILTER_COOL = 5
        const val FILTER_CONTRAST = 6

        private const val VERTEX_SHADER_CODE = """
            uniform mat4 uTexMatrix;
            attribute vec4 aPosition;
            attribute vec4 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = (uTexMatrix * aTexCoord).xy;
            }
        """

        private const val FRAGMENT_SHADER_CODE = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vTexCoord;
            uniform samplerExternalOES sTexture;
            uniform int uFilterType;
            void main() {
                vec4 color = texture2D(sTexture, vTexCoord);
                if (uFilterType == 1) {
                    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
                    gl_FragColor = vec4(vec3(gray), color.a);
                } else if (uFilterType == 2) {
                    float r = dot(color.rgb, vec3(0.393, 0.769, 0.189));
                    float g = dot(color.rgb, vec3(0.349, 0.686, 0.168));
                    float b = dot(color.rgb, vec3(0.272, 0.534, 0.131));
                    gl_FragColor = vec4(clamp(vec3(r, g, b), 0.0, 1.0), color.a);
                } else if (uFilterType == 3) {
                    gl_FragColor = vec4(1.0 - color.rgb, color.a);
                } else if (uFilterType == 4) {
                    gl_FragColor = vec4(clamp(vec3(color.r * 1.15, color.g * 1.05, color.b * 0.85), 0.0, 1.0), color.a);
                } else if (uFilterType == 5) {
                    gl_FragColor = vec4(clamp(vec3(color.r * 0.85, color.g * 1.05, color.b * 1.20), 0.0, 1.0), color.a);
                } else if (uFilterType == 6) {
                    vec3 rgb = (color.rgb - 0.5) * 1.4 + 0.5;
                    gl_FragColor = vec4(clamp(rgb, 0.0, 1.0), color.a);
                } else {
                    gl_FragColor = color;
                }
            }
        """
    }
}
