package com.example.william.my.module.ml

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 端侧 AI 模块入口 — 导航到 LiteRT（原 TensorFlow Lite）官方示例页面。
 *
 * 官方文档: https://ai.google.dev/edge/litert
 * 官方样例: https://github.com/google-ai-edge/litert-samples
 *
 * 覆盖官方 `litert-samples/samples/litert/` 顶层 **9 个通用任务**（视觉 3 / 文本 2 / 语音 4）。
 * 不计入的目录：`qualcomm` 下的各子目录（需要 Qualcomm NPU 运行时或 LiteRT-LM）、`phototalk_sample_app`
 * 与 `google/sample_app_tpu`（LiteRT-LM，后者还绑定 Google Tensor TPU）、
 * `semantic_similarity`（C++）、`image_generation`（iOS / macOS）、
 * `flutter` / `flutter_intel_npu`（Flutter 实现）、`colab`（notebook）：
 *
 * 视觉
 * 1. MNIST 手写数字实时识别（画板涂鸦 + 28x28 输入 + 置信度条形图）
 * 2. EfficientNet-Lite 图像物体分类（整图拉伸 + 1000 类别 + Top-5 + CPU/GPU 切换）
 * 3. MediaPipe Selfie Multiclass 人像语义分割（256x256 逐像素 argmax + 半透明色块叠加）
 *
 * 文本
 * 4. MobileBERT / 平均词向量文本情感分类（内嵌词表分词 + 定长整型输入 + 二分类概率）
 * 5. Laya 多语言零样本分类（提示词构建 + 主图/动作头双图 + 校准解码 + CPU/GPU 切换）
 *
 * 语音
 * 6. Matcha-TTS 英文语音合成（G2P + 扩散解码器 + 声码器 + AudioTrack 播放）
 * 7. KittenTTS 流式语音合成（动态序列长度 + 逐句生产者/消费者 + 流式 AudioTrack）
 * 8. Qwen3-TTS 语音语言模型合成（talker + MTP + codec 三图宿主循环，模型运行时下载）
 * 9. 语音识别（6 个 ASR 模型：Parakeet CTC/TDT + Moonshine + Whisper + Qwen3-ASR，
 *    文件 / 麦克风双输入，重叠窗口 + 编辑距离对齐，未确认文本标灰）
 *
 * 推理 API：绝大多数页面用 2.x 的 `CompiledModel`；流式语音合成的两张定长 LSTM 图
 * （predictor / prosody）因官方加载器暂不支持 variable tensors，与官方一样走经典 `Interpreter`。
 */
@Route(path = RouterPath.Ml.Main)
class MlMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = arrayListOf<RouterItem>()
        routerItems.add(RouterItem("── 视觉模型实战 ──", ""))
        routerItems.add(RouterItem("MNIST 手写数字实时识别 (画板涂鸦)", RouterPath.Ml.DigitClassifier))
        routerItems.add(RouterItem("EfficientNet-Lite 图像物体分类 (选图识别)", RouterPath.Ml.ImageClassification))
        routerItems.add(RouterItem("Selfie Multiclass 人像语义分割 (逐像素分类)", RouterPath.Ml.ImageSegmentation))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 文本模型实战 ──", ""))
        routerItems.add(RouterItem("MobileBERT 文本情感分类 (定长分词 + 二分类)", RouterPath.Ml.TextClassification))
        routerItems.add(RouterItem("Laya 多语言零样本分类 (提示词 + 双图 + 校准)", RouterPath.Ml.ZeroShotClassification))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 语音模型实战 ──", ""))
        routerItems.add(RouterItem("Matcha-TTS 英文语音合成 (文本转语音)", RouterPath.Ml.TextToSpeech))
        routerItems.add(RouterItem("KittenTTS 流式语音合成 (逐句边合成边播放)", RouterPath.Ml.TextToSpeechStreaming))
        routerItems.add(RouterItem("Qwen3-TTS 语音语言模型合成 (三图宿主循环)", RouterPath.Ml.TextToSpeechLm))
        routerItems.add(RouterItem("Speech Recognition 语音识别 (6 模型 + 文件/麦克风)", RouterPath.Ml.SpeechRecognition))
        return routerItems
    }
}
