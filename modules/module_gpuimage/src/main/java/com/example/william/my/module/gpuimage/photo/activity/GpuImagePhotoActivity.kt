package com.example.william.my.module.gpuimage.photo.activity

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
import com.example.william.my.module.gpuimage.photo.data.GpuImagePhotoHelper

/**
 * GPUImage — 滤镜拍照（全分辨率离屏渲染）
 *
 * 演示 CameraX 采集 + GPUImage 离屏渲染的滤镜拍照链路：取景用实时预览，
 * 拍摄时捕获全尺寸超高清原图，经 GPUImage 离屏渲染应用当前滤镜后无损保存。
 *
 * 核心机制与避坑点：
 * 1. 实时取景：CameraX ImageAnalysis 取流，GPUImage 实时渲染滤镜到 GLSurfaceView
 * 2. 全分辨率拍摄：ImageCapture 捕获全尺寸原图，而非低画质屏幕取帧截图
 * 3. 离屏滤镜：GPUImage 离屏渲染管道对原图应用当前选中滤镜，生成全分辨率照片
 * 4. 即时预览：拍摄完成后大图全屏浮层卡片查看，支持返回键拦截关闭
 *
 * https://github.com/cats-oss/android-gpuimage
 */
@Route(path = RouterPath.GpuImage.Photo)
class GpuImagePhotoActivity :
    BaseVBActivity<GpuimageActivityPhotoBinding>(),
    View.OnClickListener {

    private val photoHelper by lazy {
        GpuImagePhotoHelper(this, binding.glSurfaceView)
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
            container = binding.chipContainer,
            names = GpuImageFilterCatalog.FILTERS.map { it.name },
            initialIndex = 0,
        ) { index ->
            val spec = GpuImageFilterCatalog.FILTERS[index]
            photoHelper.setFilter(spec.factory)
            binding.tvCurrentFilter.text = spec.name
        }

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

        checkAndRequestPermission()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnCapture -> {
                Utils.toast("正在拍摄高清滤镜照片...")
                photoHelper.capturePhoto { bitmap, file ->
                    showPhotoPreview(bitmap)
                    Utils.toast("照片已保存至: ${file.name}")
                }
            }

            binding.btnClosePreview -> {
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
        binding.previewImage.setImageBitmap(bitmap)
        binding.layoutPreview.visibility = View.VISIBLE
    }

    private fun closePreview() {
        binding.layoutPreview.visibility = View.GONE
        binding.previewImage.setImageBitmap(null)
    }

    override fun fitsSystemWindows(): Boolean = false
}
