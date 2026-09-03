package com.example.william.my.core.base.utils

import android.text.TextUtils
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * GZip 纯内存字符串压缩与解压工具类
 *
 * 基于 JDK 原生 [GZIPOutputStream] / [GZIPInputStream] 与 Base64 编码，
 * 适用于超长 JSON 文本、网络上报 Payload 与批量埋点日志的内存级无损压缩。
 */
object GzipUtils {

    fun compress(data: String?): String? {
        if (data == null || data.isEmpty()) {
            return null
        }
        val out = ByteArrayOutputStream()
        val gzip: GZIPOutputStream
        try {
            gzip = GZIPOutputStream(out)
            gzip.write(data.toByteArray(StandardCharsets.UTF_8))
            gzip.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Base64.encodeToString(out.toByteArray(), Base64.NO_PADDING)
    }

    fun uncompress(data: String?): String? {
        if (TextUtils.isEmpty(data)) {
            return null
        }
        val decode = Base64.decode(data, Base64.NO_PADDING)
        val out = ByteArrayOutputStream()
        val bin = ByteArrayInputStream(decode)
        var gzipStream: GZIPInputStream? = null
        try {
            gzipStream = GZIPInputStream(bin)
            val buffer = ByteArray(256)
            var n: Int
            while (gzipStream.read(buffer).also { n = it } >= 0) {
                out.write(buffer, 0, n)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                out.close()
                gzipStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return String(out.toByteArray(), StandardCharsets.UTF_8)
    }
}

/**
 * 兼容旧命名的类型别名
 */
@Deprecated("请使用复数命名的 GzipUtils", ReplaceWith("GzipUtils"))
typealias GzipUtil = GzipUtils
