package com.example.william.my.module.gpuimage.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.gpuimage.databinding.GpuimageActivityCameraFilterBinding
import com.example.william.my.module.gpuimage.helper.GpuImageCameraHelper
import com.example.william.my.module.gpuimage.helper.GpuImageChipHelper
import com.example.william.my.module.gpuimage.helper.GpuImageFilterCatalog

/**
 * GPUImage 相机实时帧滤镜
 *
 * 与滤镜预览页（静态图）的区别：这里的图像源是相机连续帧流，滤镜作用于每一帧，
 * 页面不产出文件（拍照 / 录像属 module_media 的 CameraX 采集示例）。
 *
 * 核心 API：
 * - `ImageAnalysis`（CameraX）：以 YUV_420_888 输出连续帧，是本页唯一的取流用例
 * - `GPUImageRenderer#onPreviewFrame(byte[], int, int)`：把一帧 NV21 数据投递到 GL
 *   线程队列，由库完成 YUV→RGBA 转换与纹理上传
 * - `GPUImageRenderer#setFilter(GPUImageFilter)`：切换滤镜（内部线程安全）
 *
 * 滤镜清单复用 [GpuImageFilterCatalog.FILTERS]，滤镜条交互复用 [GpuImageChipHelper]。
 * 渲染线程、帧格式转换与相机绑定细节见 [GpuImageCameraHelper]。
 *
 * 局限：每帧的 YUV→RGBA 为 CPU 侧转换，分辨率上限控制在 720p；若需生产级实时滤镜
 * （1080p+ / 60fps），应改用相机 OES 纹理直通方案，而非该库的取帧回调路径。
 */
@Route(path = RouterPath.GpuImage.CameraFilter)
class GpuImageCameraFilterActivity : BaseVBActivity<GpuimageActivityCameraFilterBinding>() {

    private val cameraHelper by lazy { GpuImageCameraHelper(this, mBinding.glSurfaceView) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            Utils.toast("未授予相机权限，无法使用实时滤镜")
        }
    }

    override fun getViewBinding(): GpuimageActivityCameraFilterBinding = GpuimageActivityCameraFilterBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        GpuImageChipHelper.populate(
            container = mBinding.chipContainer,
            names = GpuImageFilterCatalog.FILTERS.map { it.name },
            initialIndex = 0,
        ) { index ->
            val spec = GpuImageFilterCatalog.FILTERS[index]
            cameraHelper.setFilter(spec.factory())
            mBinding.tvCurrentFilter.text = spec.name
        }

        checkAndRequestPermission()
    }

    override fun onResume() {
        super.onResume()
        cameraHelper.onResume()
    }

    override fun onPause() {
        cameraHelper.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        cameraHelper.release()
        super.onDestroy()
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
        cameraHelper.setup()
        cameraHelper.start()
    }
}
