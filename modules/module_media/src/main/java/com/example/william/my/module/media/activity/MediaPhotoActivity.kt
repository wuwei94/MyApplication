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
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.media.databinding.MediaActivityPhotoBinding
import com.example.william.my.module.media.utils.ImageCaptureHelper

/**
 * 拍照示例 — 基于 CameraX 的 ImageCapture 用例，演示预览取景与单张照片捕获。
 */
@Route(path = RouterPath.Media.Photo)
class MediaPhotoActivity :
    BaseVBActivity<MediaActivityPhotoBinding>(),
    View.OnClickListener {

    private val imageCaptureHelper by lazy {
        ImageCaptureHelper(this, mBinding.previewView)
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

        mBinding.btnCapture.setOnClickListener(this)
        mBinding.btnClosePreview.setOnClickListener(this)

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
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.btnCapture -> {
                imageCaptureHelper.captureImage { bitmap ->
                    showImagePreview(bitmap)
                }
            }

            mBinding.btnClosePreview -> {
                closePreview()
            }
        }
    }

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

    private fun showImagePreview(bitmap: Bitmap) {
        mBinding.previewImage.setImageBitmap(bitmap)
        mBinding.layoutPreview.visibility = View.VISIBLE
    }

    private fun closePreview() {
        mBinding.layoutPreview.visibility = View.GONE
        mBinding.previewImage.setImageBitmap(null)
    }

    override fun fitsSystemWindows(): Boolean = false

    override fun onDestroy() {
        imageCaptureHelper.release()
        super.onDestroy()
    }
}
