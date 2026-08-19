package com.example.william.my.module.utils.activity

import android.content.res.Resources
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.AdaptScreenUtils
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.utils.databinding.UtilsActivityAdaptscreenBinding

/**
 * AdaptScreenUtils — 屏幕适配工具
 *
 * BlankJ AdaptScreenUtils 提供屏幕适配功能，支持等比缩放。
 *
 * 核心特性：
 * 1. 等比缩放：按照设计稿宽度进行等比缩放
 * 2. 多分辨率支持：支持不同屏幕分辨率的适配
 * 3. 简单易用：一行代码实现屏幕适配
 * 4. 无侵入：不修改系统资源配置
 *
 * 基本用法：
 * ```kotlin
 * // 在 Activity 中重写 getResources()
 * override fun getResources(): Resources {
 *     return AdaptScreenUtils.adaptWidth(super.getResources(), 1080)
 * }
 * ```
 *
 * 适配原理：
 * - 根据设计稿宽度（如 1080px）和实际屏幕宽度，计算缩放比例
 * - 修改 Resources 的 density、scaledDensity 等参数
 * - 实现 dp、sp 等单位的等比缩放
 *
 * 适用场景：
 * - 按照设计稿进行屏幕适配
 * - 多分辨率设备适配
 * - 需要等比缩放的场景
 *
 * https://github.com/Blankj/AndroidUtilCode
 */
@Route(path = RouterPath.Utils.AdaptScreenUtils)
class AdaptScreenUtilsActivity : BaseVBActivity<UtilsActivityAdaptscreenBinding>() {
    override fun getViewBinding(): UtilsActivityAdaptscreenBinding {
        return UtilsActivityAdaptscreenBinding.inflate(layoutInflater)
    }

    override fun getResources(): Resources {
        return AdaptScreenUtils.adaptWidth(super.getResources(), 1080)
    }
}