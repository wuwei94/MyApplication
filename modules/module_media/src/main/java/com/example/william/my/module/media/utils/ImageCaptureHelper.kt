package com.example.william.my.module.media.utils

import android.graphics.Bitmap
import android.graphics.Matrix
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
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.core.base.utils.AppExecutorsHelper

/**
 * CameraX 拍照辅助类
 *
 * 只负责绑定 Preview + ImageCapture 两个用例，演示拍照取景与图像捕获。
 * 采用基于 Activity 生命周期的实例设计，避免在静态字段中持有 ProcessCameraProvider 或 Context 导致内存泄漏。
 */
class ImageCaptureHelper(
    private val activity: FragmentActivity,
    private val preview: PreviewView
) {

    private val main = AppExecutorsHelper.main()

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var imageCaptureUseCase: ImageCapture? = null

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

                    previewUseCase = Preview.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setTargetRotation(rotation)
                        .build()
                        .also { it.setSurfaceProvider(preview.surfaceProvider) }

                    imageCaptureUseCase = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setResolutionSelector(resolutionSelector)
                        .setTargetRotation(rotation)
                        .build()

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        activity,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        previewUseCase,
                        imageCaptureUseCase
                    )
                } catch (e: Exception) {
                    Utils.logcat(TAG, "setupCamera error: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(activity))
        }
    }

    fun captureImage(processComplete: (bitmap: Bitmap) -> Unit) {
        val imageCapture = imageCaptureUseCase ?: run {
            Utils.toast("相机未就绪，请稍候")
            return
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

    fun release() {
        try {
            cameraProvider?.unbindAll()
            cameraProvider = null
            previewUseCase = null
            imageCaptureUseCase = null
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

    companion object {
        private const val TAG = "ImageCaptureHelper"
    }
}
