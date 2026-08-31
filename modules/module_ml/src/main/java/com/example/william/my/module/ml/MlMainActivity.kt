package com.example.william.my.module.ml

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 机器学习 / 端侧 AI 入口（TensorFlow Lite / LiteRT 实战演练）
 *
 * 官方文档: https://www.tensorflow.org/lite
 *
 * 演示 Google TensorFlow Lite 官方轻量级端侧推理框架的核心技术与落地方案：
 * 1. MNIST 手写数字实时识别（画板涂鸦 + 28x28 灰度输入 + 置信度条形图）
 * 2. MobileNet 图像物体分类（4:3 黄金比例无黑边 + Center-Crop + 1000 类别中文标签 + Top-5 置信度）
 * 3. CPU 多核 vs GPU 硬件加速跑分（2×3 科学对照矩阵：单核 1T、单核+XNN、多核 4T、多核+XNN、GPU Delegate）
 * 4. TFLite 张量底层操作与内存生命周期（FlatBuffers 零拷贝 mmap、Direct Memory、MIMO 调度）
 */
@Route(path = RouterPath.Ml.Main)
class MlMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = arrayListOf<RouterItem>()
        routerItems.add(RouterItem("── 视觉模型实战 ──", ""))
        routerItems.add(RouterItem("MNIST 手写数字实时识别 (画板涂鸦)", RouterPath.Ml.DigitClassifier))
        routerItems.add(RouterItem("MobileNet 图像物体分类 (选图识别)", RouterPath.Ml.ImageClassification))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 硬件加速与性能 ──", ""))
        routerItems.add(RouterItem("CPU 多核 vs GPU 硬件加速 (跑分实测)", RouterPath.Ml.GpuDelegate))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 底层架构与内存规范 ──", ""))
        routerItems.add(RouterItem("TFLite 张量底层操作与内存架构", RouterPath.Ml.TensorBasics))
        return routerItems
    }
}
