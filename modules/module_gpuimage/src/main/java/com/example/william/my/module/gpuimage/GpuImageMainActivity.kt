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
 * 本模块演示六条核心使用链路：
 * 1. 滤镜实时预览：GPUImageView 载入图片后 `setFilter()` 无缝切换 16 种内置滤镜
 * 2. 参数实时调节：复用滤镜实例，连续调用参数 setter（亮度/对比度/色相等 9 种）
 * 3. 滤镜链组合：GPUImageFilterGroup 将多级滤镜串成单一滤镜渲染管线
 * 4. 相机实时帧滤镜：CameraX ImageAnalysis 连续取帧，经 `GPUImageRenderer#onPreviewFrame`
 *    上传纹理后由滤镜逐帧渲染（图像源从静态图扩展为相机帧流）
 * 5. 滤镜拍照：CameraX ImageCapture 抓取全分辨率原始照片，由 GPUImage 离屏渲染管道应用滤镜并无损保存
 * 6. 滤镜录像：
 *    - CameraEffect 官方管线方案：SurfaceProcessor 拦截并由 GPUImage 离屏渲染录制 MP4
 *    - EGL + MediaCodec 硬编方案：EGL 共享上下文 + 离屏 FBO 渲染 + MediaCodec 硬编码录制 MP4
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
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 相机实时帧 ──", ""))
        routerItems.add(RouterItem("相机实时帧滤镜（CameraX 取帧 + onPreviewFrame）", RouterPath.GpuImage.CameraFilter))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 滤镜拍照 ──", ""))
        routerItems.add(RouterItem("滤镜拍照（ImageCapture + GPUImage 离屏渲染）", RouterPath.GpuImage.Photo))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 滤镜录像 ──", ""))
        routerItems.add(RouterItem("CameraX 特效滤镜录像（CameraEffect + SurfaceProcessor）", RouterPath.GpuImage.CameraEffect))
        routerItems.add(RouterItem("EGL + MediaCodec 滤镜录像（OpenGL 离屏渲染 + 硬编码）", RouterPath.GpuImage.CodecRecord))
        return routerItems
    }
}
