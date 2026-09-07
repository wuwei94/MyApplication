package com.example.william.my.core.base.utils

import android.content.res.Resources

/**
 * 屏幕适配计算结果
 *
 * @property density 屏幕密度（像素比例）
 * @property scaledDensity 字型缩放因子（考虑用户系统字号设置）
 * @property densityDpi 屏幕每英寸像素点数
 */
data class DensityResult(
    val density: Float,
    val scaledDensity: Float,
    val densityDpi: Int,
)

/**
 * 屏幕适配纯函数算法
 *
 * 依据公式：`px = dp * density`，将目标宽度等分为 [designWidthDp] 份。
 * 同步依据系统原生字体缩放比例计算 [DensityResult.scaledDensity]，确保 `sp` 等比缩放且尊重系统无障碍字号设置。
 *
 * @param widthPixels 屏幕或当前窗口的像素宽度
 * @param noncompatDensity 系统未被篡改前的原生 density
 * @param noncompatScaledDensity 系统未被篡改前的原生 scaledDensity
 * @param designWidthDp 设计基准宽度（dp），默认 360dp
 */
fun calculateDensity(
    widthPixels: Int,
    noncompatDensity: Float,
    noncompatScaledDensity: Float,
    designWidthDp: Float = DensityAdaptUtils.DEFAULT_DESIGN_WIDTH_DP,
): DensityResult {
    val targetDensity = widthPixels / designWidthDp
    val fontScale = if (noncompatDensity > 0f) noncompatScaledDensity / noncompatDensity else 1.0f
    val targetScaledDensity = targetDensity * fontScale
    val targetDensityDpi = (160 * targetDensity).toInt()
    return DensityResult(
        density = targetDensity,
        scaledDensity = targetScaledDensity,
        densityDpi = targetDensityDpi,
    )
}

/**
 * 屏幕适配工具类（基于修改 density 的今日头条方案）
 *
 * 设计基准宽度：360dp（对应 1080px 设计稿在 3x 密度下的标准宽度 360dp）
 *
 * ---
 * ### 方案对比：`DensityAdaptUtils` (dp 方案) vs Blankj 的 `AdaptScreenUtils` (pt 方案)
 *
 * 1. **底层原理与换算公式**：
 *    - **今日头条 dp 方案 (`DensityAdaptUtils`)**：
 *      - 依据公式：`px = dp * density`
 *      - 通过强制修改当前 Resources 的 `DisplayMetrics.density = widthPixels / 360f` 与 `densityDpi`，
 *        使屏幕宽度等分成固定的 360 份（360dp）。
 *      - 布局编写：继续使用 Android 官方通用的 `dp` 单位（如 `180dp` 始终占屏幕半宽）。
 *    - **Blankj pt 方案 (`AdaptScreenUtils`)**：
 *      - 依据公式：`px = pt * xdpi * (1 / 72)`
 *      - 保持系统的 `density` 与 `densityDpi` 原生不变，仅修改 `DisplayMetrics.xdpi`，
 *        使屏幕宽度等分为设计稿的 pt 磅数。
 *      - 布局编写：布局文件必须全量改写为 `pt` 单位（如 `180pt` 占半宽）。
 *
 * 2. **为什么商业项目要防止系统原生控件与第三方 SDK 变形？**：
 *    - 若直接全局篡改 Application 的 `density`，系统原生的 `AlertDialog`、输入法弹起高度、Toast、
 *      WebView 字体缩放，以及接入的第三方带 UI 的 SDK（如微信/支付宝支付授权页、地图控件、客服弹窗、广告等），
 *      都会因为其写死的 `dp` 尺寸被等比放大/缩小，导致弹窗错位、字体爆框或按钮被挤出屏幕。
 *    - Blankj 的 pt 方案通过避开 `density`，天生对系统控件和第三方 SDK 具有免疫性。
 *
 * 3. **本工程为什么选用 `DensityAdaptUtils`，以及如何做好防变形隔离？**：
 *    - **零心智成本与免 XML 重构**：全工程各模块已全部采用标准 `dp` 编写，无需为了适配将成百上千处 `dp` 改为 `pt`。
 *    - **防变形的最佳实践（作用域隔离）**：
 *      - 本方案**严禁**在全局 `Application` 中篡改 `Resources`！
 *      - 仅在 `BaseActivity.getResources()` 中针对当前 Activity 实例做局部生效；
 *      - 系统弹窗、Toast 以及使用 Application Context 渲染的第三方 SDK 依然运行在系统原生 density 下，
 *        从而兼顾了“业务页面精准等比还原”与“系统控件安全不失真”。
 */
@Suppress("DEPRECATION")
object DensityAdaptUtils {

    const val DEFAULT_DESIGN_WIDTH_DP: Float = 360f

    /**
     * 适配 Resources 的 DisplayMetrics 与 Configuration
     *
     * @param resources 当前 Activity 的 Resources（注意：严禁传入 Application.resources）
     * @param designWidthDp 设计基准宽度（dp），默认 360dp
     * @return 适配后的 Resources
     */
    fun adaptWidth(resources: Resources, designWidthDp: Float = DEFAULT_DESIGN_WIDTH_DP): Resources {
        val dm = resources.displayMetrics
        val result = calculateDensity(
            widthPixels = dm.widthPixels,
            noncompatDensity = dm.density,
            noncompatScaledDensity = dm.scaledDensity,
            designWidthDp = designWidthDp,
        )

        dm.density = result.density
        dm.scaledDensity = result.scaledDensity
        dm.densityDpi = result.densityDpi
        resources.configuration.densityDpi = result.densityDpi

        return resources
    }
}
