package com.example.william.my.module.ml.asr

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * [AsrModelCatalog] 对**真实 asset 清单**的解析测试
 *
 * 直接读 `src/main/assets/asr_model_metadata.json` 本体（而不是另抄一份到测试资源里），
 * 否则夹具迟早与线上清单漂移 —— 这个文件的每一行都会被下载层和预处理层直接消费。
 *
 * 覆盖三类容易出错的地方：
 * 1. **默认值**：清单里省略的字段要与官方 `LogMelSpectroConfig` 的默认值一致（`nMels` 默认 80）；
 * 2. **落盘名**：6 个远端分词器都叫 `tokenizer.json`，必须按模型名区分，否则互相覆盖；
 * 3. **URL 还原**：完整 URL 要能拆回 `repo` + `remotePath` 供下载层使用。
 */
class AsrModelCatalogTest {

    private val catalog: AsrModelCatalog by lazy { AsrModelCatalog(readAsset()) }

    @Test
    fun modelNames_containsAllSixOfficialModelsInOrder() {
        assertEquals(
            listOf(
                "parakeet-tdt-0.6b-v3",
                "parakeet-ctc-0.6b",
                "parakeet-tdt_ctc-0.6b-ja",
                "moonshine-tiny",
                "whisper-tiny",
                "qwen3-asr-0.6b",
            ),
            catalog.modelNames,
        )
    }

    @Test
    fun configOf_parsesLogMelFields() {
        val whisper = catalog.configOf("whisper-tiny")
        assertEquals(30000, whisper.inputMilliseconds)
        assertEquals(50258, whisper.decodeStartTokenId)
        assertEquals(50257, whisper.decodeStopTokenId)

        val mel = requireNotNull(whisper.logMelSpectro)
        assertEquals(400, mel.nFFT)
        // 清单里没写 nMels → 官方默认 80（不可想当然写 128）
        assertEquals(80, mel.nMels)
        assertEquals(3000, mel.nFrames)
        assertEquals("whisper", mel.normType)
        assertEquals(0.0f, mel.preemphasis, 0.0f)
    }

    @Test
    fun configOf_moonshineHasNoLogMel_getsRawWaveform() {
        val moonshine = catalog.configOf("moonshine-tiny")
        assertNull(moonshine.logMelSpectro)
        assertEquals(1, moonshine.decodeStartTokenId)
        assertEquals(2, moonshine.decodeStopTokenId)
        // 未声明就是 -1，表示「不需要跳过任何引导 token」
        assertEquals(-1, moonshine.decodeSkipUntilTokenId)
    }

    @Test
    fun configOf_parakeetCtcTransposesMel_andJaKeepsDefaultMelCount() {
        val ctc = catalog.configOf("parakeet-ctc-0.6b")
        assertTrue(requireNotNull(ctc.logMelSpectro).transpose)
        // ctc 清单里没写 nMels/nFrames
        assertEquals(80, requireNotNull(ctc.logMelSpectro).nMels)
        assertEquals(0, requireNotNull(ctc.logMelSpectro).nFrames)

        val ja = catalog.configOf("parakeet-tdt_ctc-0.6b-ja")
        val jaMel = requireNotNull(ja.logMelSpectro)
        // 名单里唯独 v3 显式写了 nMels=128，ja 没有 → 必须落到默认 80
        assertEquals(80, jaMel.nMels)
        assertEquals(500, jaMel.nFrames)
        assertFalse(jaMel.transpose)
        assertEquals(0.97f, jaMel.preemphasis, 1e-6f)
    }

    @Test
    fun configOf_qwen3AsrIsCpuOnly_andSkipsLeadingTokens() {
        val qwen = catalog.configOf("qwen3-asr-0.6b")
        assertFalse(qwen.supportsGpu)
        assertEquals(151704, qwen.decodeSkipUntilTokenId)
        assertEquals(198, qwen.decodeStartTokenId)
        assertEquals(151645, qwen.decodeStopTokenId)
        assertTrue(catalog.configOf("whisper-tiny").supportsGpu)
    }

    @Test
    fun configOf_unknownModel_throws() {
        val exception = runCatching { catalog.configOf("not-a-model") }.exceptionOrNull()
        assertTrue(exception is NoSuchElementException)
    }

    @Test
    fun approxBytes_isModelPlusTokenizer() {
        catalog.modelNames.forEach { name ->
            val config = catalog.configOf(name)
            assertEquals(
                "「首次需下载约 xx MB」提示依赖这两个字段",
                config.approxModelBytes + config.approxTokenizerBytes,
                config.approxBytes,
            )
            assertTrue(config.approxModelBytes > 0L)
            assertTrue(config.approxTokenizerBytes > 0L)
        }
    }

    /** 六个远端分词器都叫 `tokenizer.json`，落盘名必须唯一，否则互相覆盖 */
    @Test
    fun tokenizerFileName_isUniquePerModel() {
        val names = catalog.modelNames.map { catalog.configOf(it).tokenizerFileName }
        assertEquals(names.size, names.toSet().size)
        assertEquals("tokenizer-whisper-tiny.json", catalog.configOf("whisper-tiny").tokenizerFileName)
        assertEquals(
            "tokenizer-parakeet-tdt_ctc-0.6b-ja.json",
            catalog.configOf("parakeet-tdt_ctc-0.6b-ja").tokenizerFileName,
        )
    }

    @Test
    fun remoteModels_splitsFullUrlIntoRepoAndPath() {
        val models = catalog.configOf("whisper-tiny").remoteModels()
        assertEquals(2, models.size)

        val model = models[0]
        assertEquals("whisper_tiny_30s_i8.tflite", model.fileName)
        assertEquals("litert-community/whisper-tiny", model.repo)
        assertEquals("whisper_tiny_30s_i8.tflite", model.remotePath)
        assertEquals(41116288L, model.approxBytes)

        val tokenizer = models[1]
        assertEquals("tokenizer-whisper-tiny.json", tokenizer.fileName)
        assertEquals("openai/whisper-tiny", tokenizer.repo)
        assertEquals("tokenizer.json", tokenizer.remotePath)
    }

    /** 下载地址必须是可达的镜像站，且能被 [com.example.william.my.module.ml.download.RemoteModel] 拼回来 */
    @Test
    fun remoteModels_urlRoundTrips() {
        catalog.modelNames.forEach { name ->
            catalog.configOf(name).remoteModels().forEach { model ->
                assertEquals("https://hf-mirror.com/${model.repo}/resolve/main/${model.remotePath}", model.url)
            }
        }
    }

    @Test
    fun modelFileName_matchesRemoteUrlTail() {
        catalog.modelNames.forEach { name ->
            val config = catalog.configOf(name)
            assertEquals(config.modelUrl.substringAfterLast('/'), config.modelFileName)
        }
    }

    private fun readAsset(): String {
        val candidates = listOf(
            File("src/main/assets/${AsrModelCatalog.ASSET_NAME}"),
            File("modules/module_ml/src/main/assets/${AsrModelCatalog.ASSET_NAME}"),
        )
        val file = candidates.firstOrNull { it.isFile }
            ?: error("找不到 ${AsrModelCatalog.ASSET_NAME}，候选路径：${candidates.map { it.absolutePath }}")
        return file.readText(Charsets.UTF_8)
    }

    /** 夹具自检：确保上面读到的确实是那份 6 项的清单，而不是空文件或解析失败的兜底 */
    @Test
    fun assetFixture_isNonTrivial() {
        val json = JSONObject(readAsset())
        assertEquals(6, json.length())
        assertTrue(json.has("qwen3-asr-0.6b"))
    }
}
