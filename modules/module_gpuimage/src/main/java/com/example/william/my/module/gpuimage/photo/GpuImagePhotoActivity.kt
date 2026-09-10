package com.example.william.my.module.gpuimage.photo

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
import com.example.william.my.module.gpuimage.databinding.GpuimageActivityPhotoBinding
import com.example.william.my.module.gpuimage.helper.GpuImageChipHelper
import com.example.william.my.module.gpuimage.helper.GpuImageFilterCatalog

/**
 * 滤镜拍照示例（专项页面）
 *
 * 核心设计：
 * 1. 取景：CameraX ImageAnalysis 取流，经 GPUImage 实时渲染滤镜到 GLSurfaceView；
 * 2. 拍摄：ImageCapture 捕获全尺寸超高清原图（而非低画质屏幕取帧截图）；
 * 3. 滤镜处理：GPUImage 离屏渲染当前选中的滤镜，生成全分辨率滤镜照片并存盘；
 * 4. 预览：支持即时大图全屏浮层卡片查看与返回键拦截。
 */
@Route(path = RouterPath.GpuImage.Photo)
class GpuImagePhotoActivity :
    BaseVBActivity<GpuimageActivityPhotoBinding>(),
    View.OnClickListener {

    private val photoHelper by lazy {
        GpuImagePhotoHelper(this, mBinding.glSurfaceView)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            Utils.toast("未授予相机权限，无法使用拍照功能")
        }
    }

    override fun getViewBinding(): GpuimageActivityPhotoBinding = GpuimageActivityPhotoBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        GpuImageChipHelper.populate(
            container = mBinding.chipContainer,
            names = GpuImageFilterCatalog.FILTERS.map { it.name },
            initialIndex = 0,
        ) { index ->
            val spec = GpuImageFilterCatalog.FILTERS[index]
            photoHelper.setFilter(spec.factory)
            mBinding.tvCurrentFilter.text = spec.name
        }

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

        checkAndRequestPermission()
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.btnCapture -> {
                Utils.toast("正在拍摄高清滤镜照片...")
                photoHelper.capturePhoto { bitmap, file ->
                    showPhotoPreview(bitmap)
                    Utils.toast("照片已保存至: ${file.name}")
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
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        photoHelper.setup()
        photoHelper.start()
    }

    override fun onResume() {
        super.onResume()
        photoHelper.onResume()
    }

    override fun onPause() {
        photoHelper.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        closePreview()
        photoHelper.release()
        super.onDestroy()
    }

    private fun showPhotoPreview(bitmap: Bitmap) {
        mBinding.previewImage.setImageBitmap(bitmap)
        mBinding.layoutPreview.visibility = View.VISIBLE
    }

    private fun closePreview() {
        mBinding.layoutPreview.visibility = View.GONE
        mBinding.previewImage.setImageBitmap(null)
    }

    override fun fitsSystemWindows(): Boolean = false
}
