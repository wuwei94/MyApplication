package com.example.william.my.module.ml.asr

import com.example.william.my.module.ml.download.RemoteModel
import org.json.JSONObject

/**
 * 一个 ASR 模型 + 分词器的完整配置（对应官方 `ModelConfig`）。
 *
 * 与官方 `ModelConfig` 的差异有两处，都是**去掉本模块不支持的维度**：
 * - 去掉 `npuModelPath` / `npuModelRemoteUrlPattern` / `npuHighPerformance`。
 *   官方默认就把 NPU 运行时库注释掉了（`build.gradle.kts` 里
 *   `dynamicFeatures.add(":litert_npu_runtime_libraries:…")` 整块是注释），
 *   且需要按 SoC 走不同的动态特性模块；本模块没有 NPU 设备可验证，故不保留这条路。
 * - 去掉 `hasDecoder`：官方算了它却从没被读过（[AsrModelCatalog] 的分派是按模型名里的
 *   `-ctc` / `-tdt` 走的），这个字段在官方代码里是死代码，不迁移。
 */
internal data class AsrModelConfig(
    /** 模型名，同时也是元数据 JSON 的键与下拉框显示值 */
    val key: String,
    val modelFileName: String,
    val modelUrl: String,
    val tokenizerFileName: String,
    val tokenizerUrl: String,
    val inputMilliseconds: Int,
    /** `null` 表示该模型**吃原始波形**（Moonshine），此时走 [DummyAudioProcessor] */
    val logMelSpectro: LogMelSpectroConfig?,
    val decodeStartTokenId: Int,
    val decodeStopTokenId: Int,
    val decodeSkipUntilTokenId: Int,
    /**
     * GPU 上是否强制 fp32（官方清单字段 `gpuEnforceFloat32`）
     *
     * 官方 `LiteRtRunner` 在 `accelerator == GPU` 时按此字段选 `Precision.FP32` /
     * `Precision.DEFAULT`（源码注释：Force float32 and constant tensors sharing for GPU
     * inference）。官方 schema 的默认值是 **true**，本模块沿用同一默认值 ——
     * 没有在真机上对比过 fp16 的识别质量，因此不擅自放宽。
     */
    val gpuEnforceFloat32: Boolean = true,
    /** 是否允许 GPU 共享常量张量（官方 `gpuShareConstantTensors`，官方默认 true） */
    val gpuShareConstantTensors: Boolean = true,
    /** 是否支持 GPU 后端（官方 README 的能力表里 Qwen3-ASR 只标了 CPU） */
    val supportsGpu: Boolean,
    val approxModelBytes: Long,
    val approxTokenizerBytes: Long,
) {

    /** 该配置需要下载的总字节数（仅用于「首次需下载约 xx MB」提示） */
    val approxBytes: Long get() = approxModelBytes + approxTokenizerBytes

    /** 该配置需要下载的文件（模型 + 分词器） */
    fun remoteModels(): List<RemoteModel> = listOf(
        remoteModelOf(modelFileName, modelUrl, approxModelBytes),
        remoteModelOf(tokenizerFileName, tokenizerUrl, approxTokenizerBytes),
    )
}

/**
 * 把 `https://hf-mirror.com/<owner>/<repo>/resolve/<rev>/<path>` 还原成 [RemoteModel]。
 *
 * 元数据 JSON 里存的是**完整 URL**（与官方 schema 一致），而本模块的下载层需要
 * `repo` + `remotePath` 两段，这里做一次转换。取不到 `/resolve/` 时直接报错而不是
 * 猜一个路径 —— 清单写错要立刻暴露。
 */
private fun remoteModelOf(fileName: String, url: String, approxBytes: Long): RemoteModel {
    val marker = "/resolve/"
    val index = url.indexOf(marker)
    require(index > 0) { "模型地址格式无法识别：$url" }
    val repo = url.substringAfter("://").substringAfter('/').substringBeforeLast(marker)
    val rest = url.substring(index + marker.length)
    val remotePath = rest.substringAfter('/')
    require(repo.isNotEmpty() && remotePath.isNotEmpty()) { "模型地址格式无法识别：$url" }
    return RemoteModel(
        fileName = fileName,
        repo = repo,
        remotePath = remotePath,
        approxBytes = approxBytes.coerceAtLeast(0L),
    )
}

/**
 * ASR 模型清单（对应官方 `ModelMetadataManager`）。
 *
 * 解析的是 asset 里的 `asr_model_metadata.json`：**结构与官方同源**，只做了两处必要改动 ——
 * 下载地址换成可达的镜像站，以及补上官方 schema 里没有的 `approxBytes*` / `supportsGpu`
 * 两个字段（用于界面上「首次需下载约 xx MB」提示与后端下拉框的可用性）。
 *
 * 与官方一样是**纯 JSON 驱动**：新增模型只需往 JSON 里加一项，代码零改动。
 */
internal class AsrModelCatalog(json: String) {

    private val configs: Map<String, AsrModelConfig>

    /** 下拉框里的模型名，顺序见 init 里的说明 */
    val modelNames: List<String>

    init {
        val root = JSONObject(json)
        val parsed = LinkedHashMap<String, AsrModelConfig>()
        for (key in root.keys()) {
            val entry = root.getJSONObject(key)
            val modelUrl = entry.optString("modelRemoteUrl", "")
            val tokenizerUrl = entry.optString("tokenizerUrl", "")
            require(modelUrl.isNotEmpty() && tokenizerUrl.isNotEmpty()) { "$key 缺少模型或分词器地址" }
            val modelBytes = entry.optLong("approxBytesModel", 0L)
            val tokenizerBytes = entry.optLong("approxBytesTokenizer", 0L)
            parsed[key] = AsrModelConfig(
                key = key,
                modelFileName = modelUrl.substringAfterLast('/'),
                modelUrl = modelUrl,
                // 六个分词器的远端文件名**都叫 tokenizer.json**，扁平落盘会互相覆盖，
                // 因此落盘名带上模型名（与官方按 `<模型名>/tokenizer.json` 分目录等效）。
                tokenizerFileName = "tokenizer-$key.json",
                tokenizerUrl = tokenizerUrl,
                inputMilliseconds = entry.optInt("inputMilliseconds", -1),
                logMelSpectro = entry.optJSONObject("logMelSpectro")?.let(::parseLogMelConfig),
                decodeStartTokenId = entry.optInt("decodeStartTokenId", -1),
                decodeStopTokenId = entry.optInt("decodeStopTokenId", -1),
                decodeSkipUntilTokenId = entry.optInt("decodeSkipUntilTokenId", -1),
                // 这两个字段在官方 JSON 里通常省略，默认值与官方 `ModelConfigJson` 一致为 true
                gpuEnforceFloat32 = entry.optBoolean("gpuEnforceFloat32", true),
                gpuShareConstantTensors = entry.optBoolean("gpuShareConstantTensors", true),
                supportsGpu = entry.optBoolean("supportsGpu", true),
                approxModelBytes = modelBytes,
                approxTokenizerBytes = tokenizerBytes,
            )
        }
        require(parsed.isNotEmpty()) { "ASR 模型清单为空" }
        configs = parsed
        // 下拉框顺序要跟清单里**书写的先后**一致（官方用 kotlinx.serialization 读进 LinkedHashMap，
        // 天然是声明顺序）。而 `org.json` 的 `keys()` 顺序取决于其内部 Map 实现
        // （Android 框架是 LinkedHashMap，JVM 上的 `org.json:json` 是 HashMap），
        // 因此这里按键在**原始文本**里首次出现的位置重排一次，保证两端表现一致。
        modelNames = parsed.keys.sortedBy { key -> json.indexOf("\"$key\"") }
    }

    fun configOf(modelName: String): AsrModelConfig = configs[modelName] ?: throw NoSuchElementException("模型清单里没有 $modelName")

    companion object {

        /** 官方用的资源名，保持一致 */
        const val ASSET_NAME = "asr_model_metadata.json"

        private fun parseLogMelConfig(json: JSONObject) = LogMelSpectroConfig(
            nFFT = json.getInt("nFFT"),
            nMels = json.optInt("nMels", 80),
            hopLength = json.optInt("hopLength", 160),
            nFrames = json.optInt("nFrames", 0),
            transpose = json.optBoolean("transpose", false),
            preemphasis = json.optDouble("preemphasis", 0.0).toFloat(),
            normType = json.optString("normType", "standard"),
        )
    }
}
