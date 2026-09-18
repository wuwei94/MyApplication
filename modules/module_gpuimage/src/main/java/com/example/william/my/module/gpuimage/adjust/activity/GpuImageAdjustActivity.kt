package com.example.william.my.module.gpuimage.adjust.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.gpuimage.databinding.GpuimageActivityAdjustBinding
import com.example.william.my.module.gpuimage.helper.GpuImageChipHelper
import com.example.william.my.module.gpuimage.helper.GpuImageFilterCatalog
import com.example.william.my.module.gpuimage.helper.GpuImageHelper
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageExposureFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGammaFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImagePixelationFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImagePosterizeFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSharpenFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * GPUImage — 滤镜参数实时调节
 *
 * GPUImage 的带参滤镜（亮度 / 对比度 / 饱和度等 9 种）通过各自的 float / int
 * 参数 setter 调节效果强度。本页演示「同实例调参」的连续调节模式：
 * 复用同一个滤镜实例，拖动 SeekBar 实时更新参数并请求重绘。
 *
 * 核心机制与避坑点：
 * 1. 同实例调参：选择滤镜种类时以默认参数创建一次实例，后续仅更新参数
 * 2. 参数映射：SeekBar 进度线性映射到参数区间 [min, max]
 * 3. 即时生效：GPUImageView 默认逐帧渲染，参数在下一帧即生效
 * 4. 显式重绘：requestRender() 保证 WHEN_DIRTY 渲染模式下也能立即刷新
 *
 * https://github.com/cats-oss/android-gpuimage
 */
@Route(path = RouterPath.GpuImage.Adjust)
class GpuImageAdjustActivity :
    BaseVBActivity<GpuimageActivityAdjustBinding>(),
    SeekBar.OnSeekBarChangeListener,
    View.OnClickListener {

    override fun getViewBinding(): GpuimageActivityAdjustBinding = GpuimageActivityAdjustBinding.inflate(layoutInflater)

    private var currentBitmap: Bitmap? = null
    private var selectedKind = GpuImageFilterCatalog.AdjustKind.BRIGHTNESS

    /** 当前调节的滤镜实例（复用同一个实例，仅更新参数） */
    private var currentFilter: GPUImageFilter? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding.btnReset.setOnClickListener(this)
        binding.seekBar.setOnSeekBarChangeListener(this)

        GpuImageChipHelper.populate(
            container = binding.chipContainer,
            names = GpuImageFilterCatalog.AdjustKind.entries.map { it.title },
            initialIndex = selectedKind.ordinal,
        ) { index -> onKindSelected(GpuImageFilterCatalog.AdjustKind.entries[index]) }

        // 先按默认参数应用滤镜，再加载示例图
        onKindSelected(selectedKind)
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                GpuImageHelper.decodeAsset(this@GpuImageAdjustActivity, GpuImageHelper.SAMPLE_CAR)
            }
            if (bitmap != null) {
                currentBitmap?.recycle()
                currentBitmap = bitmap
                binding.gpuImageView.setImage(bitmap)
                // 图片就绪后重放当前滤镜
                currentFilter?.let { binding.gpuImageView.setFilter(it) }
            }
        }
    }

    override fun onClick(v: View?) {
        if (v == binding.btnReset) {
            // 重置为默认参数
            onKindSelected(selectedKind)
        }
    }

    private fun onKindSelected(kind: GpuImageFilterCatalog.AdjustKind) {
        selectedKind = kind
        currentFilter = kind.newFilter(kind.def)
        binding.gpuImageView.setFilter(currentFilter)

        binding.tvParamLabel.text = "${kind.title} · ${kind.api}"
        binding.tvParamRange.text = "参数区间: [${kind.min}, ${kind.max}]"
        binding.seekBar.progress = toProgress(kind.def)
        binding.tvParamValue.text = formatValue(kind.def)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val filter = currentFilter ?: return
        val value = selectedKind.min + (selectedKind.max - selectedKind.min) * progress / 100f

        // 同一滤镜实例连续调用参数 setter，并请求一帧重绘
        updateFilterParameter(filter, value)
        binding.gpuImageView.requestRender()
        binding.tvParamValue.text = formatValue(value)
    }

    /** 将滑块进度映射到当前滤镜种类的参数 setter */
    private fun updateFilterParameter(filter: GPUImageFilter, value: Float) {
        when (selectedKind) {
            GpuImageFilterCatalog.AdjustKind.BRIGHTNESS -> (filter as GPUImageBrightnessFilter).setBrightness(value)
            GpuImageFilterCatalog.AdjustKind.CONTRAST -> (filter as GPUImageContrastFilter).setContrast(value)
            GpuImageFilterCatalog.AdjustKind.SATURATION -> (filter as GPUImageSaturationFilter).setSaturation(value)
            GpuImageFilterCatalog.AdjustKind.GAMMA -> (filter as GPUImageGammaFilter).setGamma(value)
            GpuImageFilterCatalog.AdjustKind.EXPOSURE -> (filter as GPUImageExposureFilter).setExposure(value)
            GpuImageFilterCatalog.AdjustKind.HUE -> (filter as GPUImageHueFilter).setHue(value)
            GpuImageFilterCatalog.AdjustKind.SHARPNESS -> (filter as GPUImageSharpenFilter).setSharpness(value)
            GpuImageFilterCatalog.AdjustKind.PIXELATION -> (filter as GPUImagePixelationFilter).setPixel(value)
            GpuImageFilterCatalog.AdjustKind.POSTERIZE -> (filter as GPUImagePosterizeFilter).setColorLevels(value.toInt())
        }
    }

    private fun toProgress(value: Float): Int = ((value - selectedKind.min) / (selectedKind.max - selectedKind.min) * 100).toInt()

    private fun formatValue(value: Float): String = if (selectedKind == GpuImageFilterCatalog.AdjustKind.POSTERIZE) {
        value.toInt().toString()
    } else {
        String.format("%.2f", value)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

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
