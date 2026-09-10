package com.example.william.my.module.gpuimage.helper

import androidx.camera.core.ImageProxy

/**
 * 相机帧格式转换工具（YUV_420_888 → NV21）
 *
 * GPUImage 的取帧入口 `GPUImageRenderer#onPreviewFrame(byte[], int, int)` 沿用 Camera1
 * 预览回调的数据格式，即 NV21（Y 平面 + VU 交错色度平面），并由 JNI
 * `GPUImageNativeLibrary.YUVtoRBGA` 转成 RGBA 后上传纹理。
 *
 * 而 CameraX 的 `ImageAnalysis` 输出 YUV_420_888：Y/U/V 三个独立平面，且各平面的
 * 行步长（rowStride）可能大于图像宽度（硬件对齐填充）、色度平面的像素步长
 * （pixelStride）可能是 1（平面分离）或 2（UV 交错）。因此不能直接把平面缓冲区
 * 拼接成 NV21，必须按步长逐像素搬运。
 *
 * 转换按绝对索引读取平面缓冲区，不依赖其 position/limit 状态；[out] 由调用方
 * 复用（尺寸不变时无需重新分配），避免每帧产生新数组。
 */
internal object GpuImageYuvConverter {

    /** NV21 帧字节数：Y 占全尺寸，UV 交错后占 1/2 尺寸 */
    fun nv21Size(width: Int, height: Int): Int = width * height * 3 / 2

    /**
     * 将 [image] 的 YUV_420_888 数据写入 [out]（NV21 布局）。
     *
     * @param out 长度需不小于 [nv21Size]，由调用方按帧尺寸复用
     */
    fun toNv21(image: ImageProxy, out: ByteArray) {
        val width = image.width
        val height = image.height

        copyLuma(image.planes[0], width, height, out)
        copyChroma(image.planes[1], image.planes[2], width, height, out, width * height)
    }

    /** Y 平面：逐行拷贝，跳过行尾对齐填充 */
    private fun copyLuma(
        plane: ImageProxy.PlaneProxy,
        width: Int,
        height: Int,
        out: ByteArray,
    ) {
        val buffer = plane.buffer
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride

        var position = 0
        for (row in 0 until height) {
            val rowStart = row * rowStride
            for (col in 0 until width) {
                out[position++] = buffer.get(rowStart + col * pixelStride)
            }
        }
    }

    /** 色度平面：NV21 要求 VU 交错（V 在前），并跳过行尾对齐填充 */
    private fun copyChroma(
        uPlane: ImageProxy.PlaneProxy,
        vPlane: ImageProxy.PlaneProxy,
        width: Int,
        height: Int,
        out: ByteArray,
        offset: Int,
    ) {
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        val uRowStride = uPlane.rowStride
        val vRowStride = vPlane.rowStride
        val uPixelStride = uPlane.pixelStride

        val chromaWidth = width / 2
        val chromaHeight = height / 2

        var position = offset
        for (row in 0 until chromaHeight) {
            val uRowStart = row * uRowStride
            val vRowStart = row * vRowStride
            for (col in 0 until chromaWidth) {
                val uOffset = uRowStart + col * uPixelStride
                val vOffset = vRowStart + col * uPixelStride
                out[position++] = vBuffer.get(vOffset)
                out[position++] = uBuffer.get(uOffset)
            }
        }
    }
}
