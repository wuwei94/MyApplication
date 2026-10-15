package com.example.william.my.module.ml.tts

import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * KittenTTS 的音色表（style vectors）
 *
 * 官方样把这些数据以 `voices.bin` 的形式推送到设备（由 `conversion/` 从
 * `voices.npz` "重打包"而来），HF 仓库里**只有** `voices.npz`。因此这里直接下载
 * `voices.npz` 并在端侧解包 —— `.npz` 就是一个 zip，每个音色一个 `.npy`，解析成本可以忽略
 * （见 [read]，`.npy` 解析已收敛到 [Npy]），也就不必额外托管一份派生产物。
 *
 * 形状为 `[8, 400, 256]`：**每个音色 400 行、每行对应一个文本长度**，所以取哪一行由句子
 * 的字符数决定（上游 pip 包同此约定）——这是一个「长度条件化」的音色表，不是常量向量。
 */
internal object KittenVoiceTable {

    /** 8 个内置音色，顺序即 [read] 返回的表中音频维度的顺序 */
    val VOICES = listOf(
        "expr-voice-2-m",
        "expr-voice-2-f",
        "expr-voice-3-m",
        "expr-voice-3-f",
        "expr-voice-4-m",
        "expr-voice-4-f",
        "expr-voice-5-m",
        "expr-voice-5-f",
    )

    const val STYLE_ROWS = 400
    const val STYLE_DIM = 256

    /** 每个音色的字节数：400 × 256 × float32 */
    const val BYTES_PER_VOICE = STYLE_ROWS * STYLE_DIM * 4

    /** 解析 `.npz`，返回 `[VOICES.size * STYLE_ROWS * STYLE_DIM]` 的扁平 float 表 */
    fun read(npz: InputStream): FloatArray {
        val entries = HashMap<String, ByteArray>(VOICES.size)
        ZipInputStream(npz.buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val name = entry.name.substringAfterLast('/')
                if (name.endsWith(".npy")) {
                    entries[name] = zip.readBytes()
                }
            }
        }

        val table = FloatArray(VOICES.size * STYLE_ROWS * STYLE_DIM)
        VOICES.forEachIndexed { index, voice ->
            val npy = entries["$voice.npy"]
                ?: error("音色表缺少 $voice.npy（实际条目: ${entries.keys.sorted()}）")
            val values = Npy.floats(npy)
            require(values.size == STYLE_ROWS * STYLE_DIM) {
                "$voice.npy 形状不符：${values.size} 个 float，期望 ${STYLE_ROWS * STYLE_DIM}"
            }
            values.copyInto(table, index * STYLE_ROWS * STYLE_DIM)
        }
        return table
    }

    /**
     * 取某个音色针对 [textLength] 的 style 行
     *
     * 行号取 `min(字符数, STYLE_ROWS - 1)`，与上游 pip 包一致（超出表长时钳到末行）。
     */
    fun styleFor(table: FloatArray, voice: Int, textLength: Int, out: FloatArray) {
        require(out.size == STYLE_DIM) { "style 缓冲区应为 $STYLE_DIM，实际 ${out.size}" }
        val row = minOf(textLength, STYLE_ROWS - 1).coerceAtLeast(0)
        val offset = (voice * STYLE_ROWS + row) * STYLE_DIM
        table.copyInto(out, 0, offset, offset + STYLE_DIM)
    }
}
