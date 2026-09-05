package com.example.william.my.core.okhttp.format

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * 日志格式解析器
 */
object FormatParser {

    internal const val MAX_LOG_BODY_BYTES = 1024L * 1024L
    private const val OMITTED_BODY = "Body omitted because its size is unknown or exceeds 1 MiB"

    /**
     * 是否可以解析
     * text/plain
     * text/xml
     * text/html
     * application/json
     * application/x-www-form-urlencoded
     */
    fun MediaType?.isParseAble(): Boolean = if (this == null) {
        false
    } else {
        (
            isText(this) ||
                isPlain(this) ||
                isXml(this) ||
                isHtml(this) ||
                isJson(this) ||
                isForm(this)
            )
    }

    fun isText(mediaType: MediaType?): Boolean = mediaType?.subtype?.lowercase(Locale.getDefault())?.contains("text") == true

    fun isPlain(mediaType: MediaType?): Boolean = mediaType?.subtype?.lowercase(Locale.getDefault())?.contains("plain") == true

    fun isXml(mediaType: MediaType?): Boolean = mediaType?.subtype?.lowercase(Locale.getDefault())?.contains("xml") == true

    fun isHtml(mediaType: MediaType?): Boolean = mediaType?.subtype?.lowercase(Locale.getDefault())?.contains("html") == true

    fun isJson(mediaType: MediaType?): Boolean = mediaType?.subtype?.lowercase(Locale.getDefault())?.contains("json") == true

    fun isForm(mediaType: MediaType?): Boolean = mediaType?.subtype?.lowercase(Locale.getDefault())
        ?.contains("x-www-form-urlencoded") == true

    fun parseRequest(request: Request): String {
        val requestBody = request.body ?: return ""
        return try {
            val contentLength = requestBody.contentLength()
            if (contentLength !in 0..MAX_LOG_BODY_BYTES) {
                return OMITTED_BODY
            }
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val contentType = requestBody.contentType()
            val charset: Charset =
                contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            buffer.readString(charset)
        } catch (e: IOException) {
            e.printStackTrace()
            "{\"error\": \"" + e.message + "\"}"
        }
    }

    fun parseResponse(response: Response): String {
        val responseBody = response.body ?: return ""
        return try {
            val preview = response.peekBody(MAX_LOG_BODY_BYTES + 1L)
            val bytes = preview.bytes()
            val contentType = responseBody.contentType()
            val charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            val bodyString = bytes
                .copyOf(bytes.size.coerceAtMost(MAX_LOG_BODY_BYTES.toInt()))
                .toString(charset)
            if (bytes.size > MAX_LOG_BODY_BYTES) "$bodyString\n$OMITTED_BODY" else bodyString
        } catch (e: IOException) {
            e.printStackTrace()
            "{\"error\": \"" + e.message + "\"}"
        }
    }

    /**
     * json 格式化
     *
     * @param json
     * @return
     */
    fun jsonFormat(json: String): String {
        if (json.isEmpty()) {
            return "Empty/Null json content"
        }
        val message: String = try {
            if (json.startsWith("{")) {
                val jsonObject = JSONObject(json)
                jsonObject.toString(4)
            } else if (json.startsWith("[")) {
                val jsonArray = JSONArray(json)
                jsonArray.toString(4)
            } else {
                json
            }
        } catch (e: JSONException) {
            json
        } catch (error: OutOfMemoryError) {
            "Output omitted because of Object size"
        }
        return message
    }

    /**
     * xml 格式化
     *
     * @param xml
     * @return
     */
    fun xmlFormat(xml: String): String {
        if (xml.isEmpty()) {
            return "Empty/Null xml content"
        }
        val message: String = try {
            val xmlInput: Source = StreamSource(StringReader(xml))
            val xmlOutput = StreamResult(StringWriter())
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            transformer.transform(xmlInput, xmlOutput)
            xmlOutput.writer.toString().replaceFirst(">".toRegex(), ">\n")
        } catch (e: TransformerException) {
            xml
        }
        return message
    }
}
