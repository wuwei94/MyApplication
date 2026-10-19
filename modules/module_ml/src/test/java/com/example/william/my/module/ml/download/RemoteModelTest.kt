package com.example.william.my.module.ml.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * 下载地址拼装规则的回归测试
 *
 * 这里只测纯 Kotlin 的 URL 决策，不触碰网络与文件系统。三个容易回归的点：
 * 1. **直链优先级**：`efficientnet_lite2` 的上游是 Google Cloud Storage（官方
 *    `download_model.gradle` 的原地址），不能被错误地套上 HuggingFace 镜像前缀；
 * 2. **镜像拼装未变**：其余模型仍走 `hf-mirror.com/<repo>/resolve/<revision>/<path>`，
 *    钉死的 revision 必须原样出现在 URL 里（Laya 的权重与分词器靠它对齐）；
 * 3. **互斥约束**：既没有直链、也没有 repo + remotePath 时应当构造失败，
 *    而不是产出一个指向空仓库的无效地址。
 */
class RemoteModelTest {

    @Test
    fun efficientnetLite2_usesOfficialDirectUrl() {
        // 官方 image_classification 的 download_model.gradle 里下载的就是这个地址
        assertEquals(
            "https://storage.googleapis.com/ai-edge/interpreter-samples/" +
                "image_classification/android/efficientnet_lite2.tflite",
            RemoteModels.EFFICIENTNET_LITE2.url,
        )
    }

    @Test
    fun huggingFaceEntry_stillUsesMirrorPrefix() {
        assertEquals(
            "https://hf-mirror.com/litert-community/Matcha-TTS/resolve/main/dp_g2p_matcha_fp16.tflite",
            RemoteModels.G2P.url,
        )
    }

    @Test
    fun pinnedRevision_appearsInUrl() {
        val mainModel = RemoteModels.LAYA.first()
        assertEquals(
            "https://hf-mirror.com/litert-community/Laya-Multilingual-LiteRT/" +
                "resolve/${mainModel.revision}/laya_ml_s256_embeds_wfp16.tflite",
            mainModel.url,
        )
    }

    @Test
    fun neitherDirectUrlNorRepo_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            RemoteModel(fileName = "x.tflite", approxBytes = 1L)
        }
    }
}
