package com.example.william.my.module.gpuimage.group.activity

import android.graphics.Bitmap
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.gpuimage.databinding.GpuimageActivityGroupBinding
import com.example.william.my.module.gpuimage.helper.GpuImageChipHelper
import com.example.william.my.module.gpuimage.helper.GpuImageHelper
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageEmbossFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGammaFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHalftoneFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImagePixelationFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSketchFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSobelEdgeDetectionFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageToonFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVignetteFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * GPUImage — GPUImageFilterGroup 多级滤镜链
 *
 * GPUImageFilterGroup 将多个 GPUImageFilter 包成「一个」滤镜，内部用 FBO
 * （Frame Buffer Object）离屏渲染：第 N 级滤镜的输出纹理作为第 N+1 级的输入，
 * 首尾相接。本页提供基础滤镜（第 1 层）与叠加滤镜（第 2 层）两级选择。
 *
 * 核心机制与避坑点：
 * 1. 滤镜链组合：GPUImageFilterGroup(listOf(a, b)) 将多级滤镜串联为单一滤镜
 * 2. FBO 离屏渲染：中间结果写入帧缓冲对象，逐级传递纹理
 * 3. 两级选择：基础滤镜（模糊/素描等）+ 叠加滤镜（反色/晕影等）
 * 4. 动态重建：切换任一层后重新组装 FilterGroup 并应用到 GPUImageView
 *
 * https://github.com/cats-oss/android-gpuimage
 */
@Route(path = RouterPath.GpuImage.Group)
class GpuImageGroupActivity : BaseVBActivity<GpuimageActivityGroupBinding>() {

    override fun getViewBinding(): GpuimageActivityGroupBinding = GpuimageActivityGroupBinding.inflate(layoutInflater)

    private data class GroupSpec(val name: String, val factory: () -> GPUImageFilter)

    /** 链第 1 层：基础滤镜 */
    private val baseFilters: List<GroupSpec> = listOf(
        GroupSpec("高斯模糊") { GPUImageGaussianBlurFilter(2.5f) },
        GroupSpec("素描") { GPUImageSketchFilter() },
        GroupSpec("浮雕") { GPUImageEmbossFilter(1.2f) },
        GroupSpec("边缘检测") { GPUImageSobelEdgeDetectionFilter() },
        GroupSpec("像素画") { GPUImagePixelationFilter().apply { setPixel(25f) } },
        GroupSpec("卡通") { GPUImageToonFilter(0.3f, 10f) },
        GroupSpec("网点印刷") { GPUImageHalftoneFilter() },
    )

    /** 链第 2 层：叠加调整（null 表示不叠加） */
    private val overlayFilters: List<GroupSpec?> = listOf(
        null,
        GroupSpec("怀旧") { GPUImageSepiaToneFilter(0.8f) },
        GroupSpec("反色") { GPUImageColorInvertFilter() },
        GroupSpec("黑白") { GPUImageGrayscaleFilter() },
        GroupSpec("高饱和") { GPUImageSaturationFilter(1.8f) },
        GroupSpec("伽马提亮") { GPUImageGammaFilter(1.5f) },
        GroupSpec("晕影暗角") { GPUImageVignetteFilter() },
        GroupSpec("色相偏移") { GPUImageHueFilter(60f) },
    )

    private var currentBitmap: Bitmap? = null
    private var baseIndex = 0
    private var overlayIndex = 1

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        GpuImageChipHelper.populate(
            container = binding.chipBase,
            names = baseFilters.map { it.name },
            initialIndex = baseIndex,
        ) { index ->
            baseIndex = index
            rebuildChain()
        }
        GpuImageChipHelper.populate(
            container = binding.chipOverlay,
            names = overlayFilters.map { it?.name ?: "无叠加" },
            initialIndex = overlayIndex,
        ) { index ->
            overlayIndex = index
            rebuildChain()
        }

        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                GpuImageHelper.decodeAsset(this@GpuImageGroupActivity, GpuImageHelper.SAMPLE_DOG)
            }
            if (bitmap != null) {
                currentBitmap?.recycle()
                currentBitmap = bitmap
                binding.gpuImageView.setImage(bitmap)
            }
            rebuildChain()
        }
    }

    /** 组装滤镜链：基础层必选，叠加层可空，最后以单一 FilterGroup 应用 */
    private fun rebuildChain() {
        val chain = mutableListOf(baseFilters[baseIndex].factory())
        val overlay = overlayFilters[overlayIndex]
        overlay?.let { chain += it.factory() }

        binding.gpuImageView.setFilter(GPUImageFilterGroup(chain))
        binding.tvChainLabel.text = "链路: ${baseFilters[baseIndex].name}" + (overlay?.let { " → ${it.name}" } ?: "")
        binding.tvChainDesc.text =
            "当前链路：原图 → ${baseFilters[baseIndex].name}" +
            (overlay?.let { " → ${it.name}" } ?: "") +
            "\nAPI：GPUImageFilterGroup(filters) 共 ${chain.size} 级，逐级 FBO 离屏串联"
    }

    override fun onPause() {
        super.onPause()
        binding.gpuImageView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.gpuImageView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentBitmap?.recycle()
        currentBitmap = null
    }
}
