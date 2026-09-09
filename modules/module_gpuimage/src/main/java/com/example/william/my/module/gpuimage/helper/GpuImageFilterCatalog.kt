package com.example.william.my.module.gpuimage.helper

import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageEmbossFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageExposureFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGammaFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHalftoneFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImagePixelationFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImagePosterizeFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSharpenFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSketchFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSobelEdgeDetectionFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageToonFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVignetteFilter

/**
 * GPUImage 滤镜目录
 *
 * 集中管理两个演示页使用的滤镜清单，避免页面各自维护重复列表。
 *
 * - [FILTERS]：滤镜浏览页的「一键滤镜库」，每个滤镜通过 [FilterSpec.factory] 惰性创建。
 *   GPUImage 所有滤镜均为 `GPUImageFilter` 子类，可直接传入
 *   `GPUImageView#setFilter(GPUImageFilter)`；无参构造的 `GPUImageFilter()`
 *   即“直通滤镜”（显示原图）。
 * - [AdjustKind]：参数调节页的「可调参数滤镜」，每个枚举描述参数的最小/最大值、
 *   默认值以及 `newFilter(value)` 工厂，用于把 SeekBar 进度映射为滤镜参数。
 */
object GpuImageFilterCatalog {

    data class FilterSpec(
        val name: String,
        val factory: () -> GPUImageFilter,
    )

    /**
     * 滤镜浏览页清单（涵盖色彩、风格化、边缘检测、畸变等常用类别）。
     * 参数含义参考 iOS GPUImage 同名滤镜，shader 与其保持一致。
     */
    val FILTERS: List<FilterSpec> = listOf(
        FilterSpec("原图") { GPUImageFilter() },
        FilterSpec("怀旧") { GPUImageSepiaToneFilter(1f) },
        FilterSpec("黑白") { GPUImageGrayscaleFilter() },
        FilterSpec("反色") { GPUImageColorInvertFilter() },
        FilterSpec("高饱和") { GPUImageSaturationFilter(1.8f) },
        FilterSpec("伽马提亮") { GPUImageGammaFilter(1.6f) },
        FilterSpec("高斯模糊") { GPUImageGaussianBlurFilter(2.5f) },
        FilterSpec("像素画") { GPUImagePixelationFilter().apply { setPixel(25f) } },
        FilterSpec("色调分离") { GPUImagePosterizeFilter(8) },
        FilterSpec("浮雕") { GPUImageEmbossFilter(1.2f) },
        FilterSpec("素描") { GPUImageSketchFilter() },
        FilterSpec("卡通") { GPUImageToonFilter(0.3f, 10f) },
        FilterSpec("边缘检测") { GPUImageSobelEdgeDetectionFilter() },
        FilterSpec("网点印刷") { GPUImageHalftoneFilter() },
        FilterSpec("晕影暗角") { GPUImageVignetteFilter() },
        FilterSpec("色相偏移") { GPUImageHueFilter(90f) },
    )

    /**
     * 可调参数滤镜枚举。
     *
     * @property title 滤镜中文名
     * @property api 对应的参数 setter（页面副标题提示读者使用的 API）
     * @property min / [max] SeekBar 进度映射的参数范围
     * @property def 默认参数值（映射到 SeekBar 初始位置）
     * @property newFilter 由具体参数值构造滤镜实例
     */
    enum class AdjustKind(
        val title: String,
        val api: String,
        val min: Float,
        val max: Float,
        val def: Float,
        val newFilter: (value: Float) -> GPUImageFilter,
    ) {
        BRIGHTNESS("亮度", "setBrightness(float)", -1f, 1f, 0f, { GPUImageBrightnessFilter(it) }),
        CONTRAST("对比度", "setContrast(float)", 0f, 4f, 1f, { GPUImageContrastFilter(it) }),
        SATURATION("饱和度", "setSaturation(float)", 0f, 2f, 1f, { GPUImageSaturationFilter(it) }),
        GAMMA("伽马", "setGamma(float)", 0.2f, 3f, 1f, { GPUImageGammaFilter(it) }),
        EXPOSURE("曝光", "setExposure(float)", -5f, 5f, 0f, { GPUImageExposureFilter(it) }),
        HUE("色相", "setHue(float)", 0f, 360f, 0f, { GPUImageHueFilter(it) }),
        SHARPNESS("锐度", "setSharpness(float)", -4f, 4f, 0f, { GPUImageSharpenFilter(it) }),
        PIXELATION("像素化", "setPixel(float)", 1f, 60f, 1f, { v -> GPUImagePixelationFilter().apply { setPixel(v) } }),
        POSTERIZE("色调分离", "setColorLevels(int)", 2f, 30f, 10f, { GPUImagePosterizeFilter(it.toInt()) }),
    }
}
