package com.example.william.my.module.widget.activity

import android.os.Bundle
import android.view.Gravity
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.ninepatch.NinePatchHelper

/**
 * 9-patch — 可拉伸图片
 *
 * 9-patch 是 Android 特有的图片格式，支持局部拉伸。
 *
 * 核心特性：
 * 1. 局部拉伸：只拉伸指定区域，保持其他区域不变
 * 2. 内容区域：定义内容显示区域，自动适配不同尺寸
 * 3. 资源优化：减少图片资源数量，适配不同屏幕
 * 4. 性能优秀：系统原生支持，性能优秀
 *
 * 9-patch 图片结构：
 * - 左上角：拉伸区域标记（黑色像素）
 * - 右下角：内容区域标记（黑色像素）
 *
 * 基本用法：
 * ```kotlin
 * // 加载 Asset .9 图片
 * NinePatchHelper.ninePatchChunk(context, imageView, "image.9.png")
 *
 * // 加载网络 .9 图片
 * NinePatchHelper.ninePatchChunk(context, imageView, networkUrl)
 * ```
 *
 * 制作方法：
 * 1. 使用 Android Studio 的 Draw 9-patch 工具
 * 2. 在图片边缘绘制黑色像素标记拉伸和内容区域
 * 3. 保存为 .9.png 格式
 *
 * 适用场景：
 * - 气泡背景、对话框背景
 * - 按钮背景、输入框背景
 * - 需要局部拉伸的场景
 */
@Route(path = RouterPath.Widget.NinePatch)
class NinePatchActivity : BasicResponseActivity() {

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "Asset .9 图片",
            "Network .9 图片",
            "清空",
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方按钮加载 9-patch 图片")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                NinePatchHelper.ninePatchChunk(this, mBinding.basicsResponse, Constants.Url_NinePatchAsset)
                appendLog("已加载 Asset .9 图片")
            }

            1 -> {
                NinePatchHelper.ninePatchChunk(this, mBinding.basicsResponse, Constants.Url_NinePatchNetwork)
                appendLog("已加载 Network .9 图片")
            }

            2 -> {
                mBinding.basicsResponse.background = null
                appendLog("已清空背景")
            }
        }
    }
}
