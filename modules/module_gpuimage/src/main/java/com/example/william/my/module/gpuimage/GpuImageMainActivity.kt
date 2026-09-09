package com.example.william.my.module.gpuimage

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * GPU 图像滤镜处理入口（GPUImage）
 *
 * GitHub: https://github.com/cats-oss/android-gpuimage
 *
 * GPUImage 是移植自 iOS GPUImage 的 Android OpenGL ES 图像滤镜库，
 * 通过 `GPUImageView` + `GPUImageFilter` 子类在 GPU 上逐帧渲染，
 * 内置 60+ 种滤镜（shader 与 iOS 版保持一致），并支持滤镜链组合。
 *
 * 本模块演示三条核心使用链路：
 * 1. 滤镜实时预览：GPUImageView 载入图片后 `setFilter()` 无缝切换 16 种内置滤镜
 * 2. 参数实时调节：复用滤镜实例，连续调用参数 setter（亮度/对比度/色相等 9 种）
 * 3. 滤镜链组合：GPUImageFilterGroup 将多级滤镜串成单一滤镜渲染管线
 */
@Route(path = RouterPath.GpuImage.Main)
class GpuImageMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = arrayListOf<RouterItem>()
        routerItems.add(RouterItem("── 滤镜效果演示 ──", ""))
        routerItems.add(RouterItem("滤镜实时预览（GPUImageView + setFilter）", RouterPath.GpuImage.Filter))
        routerItems.add(RouterItem("滤镜参数实时调节（连续参数 setter）", RouterPath.GpuImage.Adjust))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 滤镜链组合 ──", ""))
        routerItems.add(RouterItem("GPUImageFilterGroup 多级滤镜链", RouterPath.GpuImage.Group))
        return routerItems
    }
}
