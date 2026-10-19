package com.example.william.my.module.ml.download

/**
 * 一个按需从远端拉取的模型资产。
 *
 * 官方 2.x 样例中，语音识别 / 零样本分类 / 语音合成这几类任务的模型动辄数十 MB 到数 GB，
 * **官方本身就是运行时下载**（各 sample 自带 `ModelDownloader` / `ModelMetadataManager`），
 * 不随 APK 分发。本模块沿用同一策略：模型首次使用时下载到应用私有目录，之后完全离线可用
 * （见 [ModelStore]）。这与官方一致，也避免让 APK 膨胀到 GB 量级。
 *
 * @property fileName 落盘后的文件名，同时也是加载 [com.google.ai.edge.litert.CompiledModel] 时使用的名字
 * @property approxBytes 预估字节数，仅用于下载进度展示（真实长度以响应头为准）
 * @property repo 模型仓库（`owner/name`，见 [MIRROR_BASE]）；走 [directUrl] 时可省略
 * @property remotePath 仓库内相对路径；走 [directUrl] 时可省略
 * @property revision 仓库修订号；官方清单里是钉死的 commit，填 `null` 表示跟随默认分支
 * @property directUrl 绝对下载地址。上游不在 HuggingFace 时使用（例如官方
 *   `image_classification` 的 `efficientnet_lite2.tflite` 由官方 Gradle 从
 *   `storage.googleapis.com` 拉取），非空时**优先于** `repo + remotePath` 拼装
 */
data class RemoteModel(
    val fileName: String,
    val approxBytes: Long,
    val repo: String = "",
    val remotePath: String = "",
    val revision: String? = null,
    val directUrl: String? = null,
) {
    init {
        require(directUrl != null || (repo.isNotEmpty() && remotePath.isNotEmpty())) {
            "$fileName 需要提供 directUrl，或同时提供 repo 与 remotePath"
        }
    }

    /** 下载地址 */
    val url: String
        get() = directUrl ?: "$MIRROR_BASE/$repo/resolve/${revision ?: "main"}/$remotePath"

    companion object {
        /**
         * 官方各 sample 的下载器硬编码 `https://huggingface.co`，本机该域名不可达
         * （DNS 解析后 443 一律超时），改用内容一致的镜像站。切换镜像只需改这一处。
         */
        const val MIRROR_BASE = "https://hf-mirror.com"

        /** 官方 `image_classification` 的 Gradle 下载脚本里 `efficientnet_lite2` 的原始地址 */
        const val EFFICIENTNET_LITE2_URL =
            "https://storage.googleapis.com/ai-edge/interpreter-samples/image_classification/android/efficientnet_lite2.tflite"
    }
}

/**
 * 各新增任务所用的远端模型清单。
 *
 * 仓库与文件均按官方仓库实测核对，体积为实测值；改动前请重新核对上游。
 * 同一仓库里可能另有厂商 NPU 变体（`*_MediaTek_*.tflite` / `*_Qualcomm_*.tflite`）
 * 与量化变体（`*_i8` / `*_int4`），那些是实验性路径，**不在本清单内**。
 */
object RemoteModels {

    /**
     * 图像分类的可选模型 EfficientNet-Lite2（24 MB）
     *
     * 官方 `image_classification` 样例在设置面板里可切换 Lite0 / Lite2，两张图由官方
     * Gradle 任务下载到 assets。本模块遵循「新增模型不入库、运行时下载」的既定策略：
     * Lite0 仍随 APK 分发（切换默认值不能依赖网络），Lite2 首次选中时下载。
     *
     * 上游是 Google Cloud Storage 而非 HuggingFace，因此走 [RemoteModel.directUrl]。
     * 实测输入 `[1, 260, 260, 3]`、输出 `[1, 1000]` FP32 Softmax —— 与 Lite0 的
     * 224 输入不同，页面必须按模型分别取尺寸（见 `TFLiteModelHelper.readInputEdge`）。
     */
    val EFFICIENTNET_LITE2: RemoteModel = RemoteModel(
        fileName = "efficientnet_lite2.tflite",
        approxBytes = 24_325_765L,
        directUrl = RemoteModel.EFFICIENTNET_LITE2_URL,
    )

    private const val REPO_MATCHA = "litert-community/Matcha-TTS"

    private const val REPO_KITTEN = "litert-community/kitten-tts-nano-0.8"

    /**
     * 神经 G2P（DeepPhonemizer）图
     *
     * **两个语音合成示例共用同一张图**：官方流式样例自己的说明里也写明它取的就是 Matcha 仓库
     * 里这一份（`dp_g2p_matcha_fp16.tflite`）。落盘文件名一致，因此先做过任一页面之后，
     * 另一个页面不会重复下载。
     */
    val G2P: RemoteModel = RemoteModel(
        fileName = "dp_g2p_matcha_fp16.tflite",
        repo = REPO_MATCHA,
        remotePath = "dp_g2p_matcha_fp16.tflite",
        approxBytes = 25_784_000L,
    )

    /** Matcha-TTS：文本编码器 + 扩散解码器 + 声码器 + 神经 G2P 四张图，合计约 88 MB。 */
    val MATCHA: List<RemoteModel> = listOf(
        RemoteModel(
            fileName = "matcha_textenc_fp16.tflite",
            repo = REPO_MATCHA,
            remotePath = "matcha_textenc_fp16.tflite",
            approxBytes = 14_772_000L,
        ),
        RemoteModel(
            fileName = "matcha_decoder_fp16.tflite",
            repo = REPO_MATCHA,
            remotePath = "matcha_decoder_fp16.tflite",
            approxBytes = 22_617_000L,
        ),
        RemoteModel(
            fileName = "matcha_vocoder_fp16.tflite",
            repo = REPO_MATCHA,
            remotePath = "matcha_vocoder_fp16.tflite",
            approxBytes = 28_966_000L,
        ),
        G2P,
    )

    /** KittenTTS 的时长预测器（含定长 LSTM 的 variable tensors） */
    val KITTEN_PREDICTOR: RemoteModel = RemoteModel(
        fileName = "kitten_predictor_fp16.tflite",
        repo = REPO_KITTEN,
        remotePath = "kitten_predictor_fp16.tflite",
        approxBytes = 16_990_756L,
    )

    /** KittenTTS 的韵律预测器 */
    val KITTEN_PROSODY: RemoteModel = RemoteModel(
        fileName = "kitten_prosody_fp16.tflite",
        repo = REPO_KITTEN,
        remotePath = "kitten_prosody_fp16.tflite",
        approxBytes = 1_697_636L,
    )

    /** KittenTTS 的声码器（ISTFTNet），输出是动态形状的波形张量 */
    val KITTEN_VOCODER: RemoteModel = RemoteModel(
        fileName = "kitten_vocoder_fp16.tflite",
        repo = REPO_KITTEN,
        remotePath = "kitten_vocoder_fp16.tflite",
        approxBytes = 13_367_840L,
    )

    /**
     * KittenTTS 的音色表
     *
     * 官方样例要求设备上有一份 `voices.bin`（由 `conversion/` 从 `voices.npz` 重打包而来），
     * 但 HF 仓库里**只有 `voices.npz`**。这里直接取 `.npz` 在端侧解包，省掉一份派生产物。
     */
    val KITTEN_VOICES: RemoteModel = RemoteModel(
        fileName = "voices.npz",
        repo = REPO_KITTEN,
        remotePath = "voices.npz",
        approxBytes = 3_278_902L,
    )

    /**
     * 流式语音合成（KittenTTS nano）：三张图 + 音色表 + 共用的 G2P，合计约 61 MB。
     *
     * 官方把模型推送到设备；本模块改为运行时下载，与其余新增任务一致。
     */
    val KITTEN: List<RemoteModel> = listOf(
        KITTEN_PREDICTOR,
        KITTEN_PROSODY,
        KITTEN_VOCODER,
        KITTEN_VOICES,
        G2P,
    )

    private const val REPO_LAYA = "litert-community/Laya-Multilingual-LiteRT"

    /**
     * 官方 `model_manifest.json` 钉死的修订号
     *
     * 这是**有意的**：Laya 主图 250 MB、嵌入表 393 MB，跟随默认分支一旦上游换权重，
     * 分词器 / 嵌入表 / 图三者就可能对不上（本模块按官方原文沿用同一修订号）。
     */
    private const val LAYA_REVISION = "058cbf34ed2c6a2854ec60e9534c229d08740387"

    /**
     * Laya 多语言零样本分类：主图 + 动作头 + 校准表 + 分词器 + 嵌入表，合计约 679 MB。
     *
     * 官方清单里还有一个 `tokenizer_config.json`（524 B），但 `LayaTokenizer` 只读
     * `tokenizer.json`（其中已含 `pre_tokenizer` 与 `model.vocab` / `model.merges`），
     * 因此不下载这个用不到的文件 —— 其余 6 项与官方清单一一对应。
     */
    val LAYA: List<RemoteModel> = listOf(
        RemoteModel(
            fileName = "laya_ml_s256_embeds_wfp16.tflite",
            repo = REPO_LAYA,
            remotePath = "laya_ml_s256_embeds_wfp16.tflite",
            approxBytes = 250_889_408L,
            revision = LAYA_REVISION,
        ),
        RemoteModel(
            fileName = "laya_ml_act_head_fp32.tflite",
            repo = REPO_LAYA,
            remotePath = "laya_ml_act_head_fp32.tflite",
            approxBytes = 795_816L,
            revision = LAYA_REVISION,
        ),
        RemoteModel(
            fileName = "laya_ml_calibration.json",
            repo = REPO_LAYA,
            remotePath = "laya_ml_calibration.json",
            approxBytes = 9_156L,
            revision = LAYA_REVISION,
        ),
        RemoteModel(
            fileName = "tokenizer.json",
            repo = REPO_LAYA,
            remotePath = "tokenizer.json",
            approxBytes = 34_363_188L,
            revision = LAYA_REVISION,
        ),
        RemoteModel(
            fileName = "token_embeddings_fp16.bin",
            repo = REPO_LAYA,
            remotePath = "token_embeddings_fp16.bin",
            approxBytes = 393_216_000L,
            revision = LAYA_REVISION,
        ),
        RemoteModel(
            fileName = "token_embeddings.json",
            repo = REPO_LAYA,
            remotePath = "token_embeddings.json",
            approxBytes = 801L,
            revision = LAYA_REVISION,
        ),
    )

    private const val REPO_QWEN = "litert-community/Qwen3-TTS-12Hz-0.6B-Base"

    /**
     * Qwen3-TTS（语音语言模型）的宿主侧张量
     *
     * 官方 `install_to_device.sh` 会把仓库里的 `tables/` 与 `voices/` 目录**拍平**推到
     * 应用私有目录，本模块沿用同样的落盘名，因此这里 [RemoteModel.remotePath] 带目录、
     * [RemoteModel.fileName] 不带 —— 引擎侧只认后者。
     */
    private val QWEN_TABLES: List<RemoteModel> = listOf(
        RemoteModel(
            fileName = "codec_embedding_fp32.npy",
            repo = REPO_QWEN,
            remotePath = "tables/codec_embedding_fp32.npy",
            approxBytes = 12_583_040L,
        ),
        RemoteModel(
            fileName = "mtp_embeddings_fp16.npy",
            repo = REPO_QWEN,
            remotePath = "tables/mtp_embeddings_fp16.npy",
            approxBytes = 62_914_688L,
        ),
        RemoteModel(
            fileName = "text_projection_fp32.npz",
            repo = REPO_QWEN,
            remotePath = "tables/text_projection_fp32.npz",
            approxBytes = 25_179_078L,
        ),
        RemoteModel(
            fileName = "text_embedding_fp16.npy",
            repo = REPO_QWEN,
            remotePath = "tables/text_embedding_fp16.npy",
            approxBytes = 622_329_984L,
        ),
    )

    /**
     * Qwen3-TTS 三图 + 宿主表 + 分词器，合计约 **1.67 GB**（FAST 图集）
     *
     * 图集按**官方 `install_to_device.sh` 的默认（FAST）**装：共用的 `talker_int4`，
     * 折叠成一次调用的 `mtp_folded_int8`，以及拆成两段的 `codec_partA` + `codec_partB`。
     * 官方的另一套 reference 图集（`mtp_fp32` + `codec_decoder_fp32`，`FAST=0`）要多下约
     * 210 MB，且两套产出的音频本来就不同（折叠 MTP 的残差码本由图内 argmax 决定），
     * 因此本模块只装默认这一套 —— 引擎侧也不再保留永远不会走到的 reference 分支。
     *
     * 体积分布（实测）：文本嵌入表 622 MB / codec_partB 294 MB / talker_int4 256 MB /
     * mtp_folded_int8 230 MB / codec_partA 163 MB / 其余约 104 MB。
     * 下载顺序刻意**把最大的两个放在最后**，断点续传中断时先拿到的都是小文件。
     */
    val QWEN: List<RemoteModel> = listOf(
        RemoteModel(
            fileName = "vocab.json",
            repo = REPO_QWEN,
            remotePath = "vocab.json",
            approxBytes = 2_776_833L,
        ),
        RemoteModel(
            fileName = "merges.txt",
            repo = REPO_QWEN,
            remotePath = "merges.txt",
            approxBytes = 1_671_839L,
        ),
        RemoteModel(
            fileName = "demo_speaker.npy",
            repo = REPO_QWEN,
            remotePath = "voices/demo_speaker.npy",
            approxBytes = 4_224L,
        ),
    ) + QWEN_TABLES + listOf(
        RemoteModel(
            fileName = "talker_int4.tflite",
            repo = REPO_QWEN,
            remotePath = "talker_int4.tflite",
            approxBytes = 255_998_768L,
        ),
        RemoteModel(
            fileName = "mtp_folded_int8.tflite",
            repo = REPO_QWEN,
            remotePath = "mtp_folded_int8.tflite",
            approxBytes = 229_608_368L,
        ),
        RemoteModel(
            fileName = "codec_partA.tflite",
            repo = REPO_QWEN,
            remotePath = "codec_partA.tflite",
            approxBytes = 162_987_924L,
        ),
        RemoteModel(
            fileName = "codec_partB.tflite",
            repo = REPO_QWEN,
            remotePath = "codec_partB.tflite",
            approxBytes = 293_705_152L,
        ),
    )
}
