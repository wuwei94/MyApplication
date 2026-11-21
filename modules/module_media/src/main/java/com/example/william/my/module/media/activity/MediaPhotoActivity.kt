package com.example.william.my.module.media.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.media.databinding.MediaActivityPhotoBinding
import com.example.william.my.module.media.utils.ImageCaptureHelper

/**
 * CameraX — ImageCapture 拍照用例
 *
 * 基于 CameraX 的 ImageCapture 用例，演示预览取景与单张照片捕获的完整流程。
 *
 * 核心机制与避坑点：
 * 1. 预览取景：PreviewView 实时预览相机画面
 * 2. 单张捕获：ImageCapture 拍照并输出 JPEG 文件
 * 3. 照片预览：拍照后展示预览，支持关闭返回
 * 4. 权限处理：运行时申请 CAMERA 权限
 *
 * 官方参考：
 * https://developer.android.com/media/camera/camerax/get-started
 */
class MediaPhotoActivity :
    BaseVBActivity<MediaActivityPhotoBinding>(),
    View.OnClickListener {

    private val imageCaptureHelper by lazy {
        ImageCaptureHelper(this, binding.previewView)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            imageCaptureHelper.setupCamera()
        } else {
            Utils.toast("未授予相机权限，无法使用拍照功能")
        }
    }

    override fun getViewBinding(): MediaActivityPhotoBinding = MediaActivityPhotoBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        checkAndRequestPermission()

        binding.btnCapture.setOnClickListener(this)
        binding.btnClosePreview.setOnClickListener(this)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.layoutPreview.isVisible) {
                        closePreview()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnCapture -> {
                imageCaptureHelper.captureImage { bitmap ->
                    showImagePreview(bitmap)
                }
            }

            binding.btnClosePreview -> {
                closePreview()
            }
        }
    }

    /**
     * 检查相机权限，已授权则初始化相机，否则发起权限申请
     */
    private fun checkAndRequestPermission() {
        val hasCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCamera) {
            imageCaptureHelper.setupCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * 展示拍照结果 Bitmap 预览浮层
     */
    private fun showImagePreview(bitmap: Bitmap) {
        binding.previewImage.setImageBitmap(bitmap)
        binding.layoutPreview.visibility = View.VISIBLE
    }

    /**
     * 隐藏拍照预览浮层并清空预览图引用
     */
    private fun closePreview() {
        binding.layoutPreview.visibility = View.GONE
        binding.previewImage.setImageBitmap(null)
    }

    override fun fitsSystemWindows(): Boolean = false

    override fun onDestroy() {
        imageCaptureHelper.release()
        super.onDestroy()
    }
}
