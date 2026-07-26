package com.example.william.my.core.okhttp.format

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import okio.GzipSource
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

object ParseUtils {

    /**
     * 是否可以解析
     * text/plain
     * text/xml
     * text/html
     * application/json
     * application/x-www-form-urlencoded
     */
    fun MediaType?.isParseAble(): Boolean {
        return if (this == null) {
            false
        } else (isText(this) || isPlain(this)
                || isXml(this) || isHtml(this)
                || isJson(this) || isForm(this))
    }

    fun isText(mediaType: MediaType?): Boolean {
        return mediaType?.subtype?.lowercase(Locale.getDefault())?.contains("text") == true
    }

    fun isPlain(mediaType: MediaType?): Boolean {
        return mediaType?.subtype?.lowercase(Locale.getDefault())?.contains("plain") == true
    }

    fun isXml(mediaType: MediaType?): Boolean {
        return mediaType?.subtype?.lowercase(Locale.getDefault())?.contains("xml") == true
    }

    fun isHtml(mediaType: MediaType?): Boolean {
        return mediaType?.subtype?.lowercase(Locale.getDefault())?.contains("html") == true
    }

    fun isJson(mediaType: MediaType?): Boolean {
        return mediaType?.subtype?.lowercase(Locale.getDefault())?.contains("json") == true
    }

    fun isForm(mediaType: MediaType?): Boolean {
        return mediaType?.subtype?.lowercase(Locale.getDefault())
            ?.contains("x-www-form-urlencoded") == true
    }

    fun parseRequest(request: Request): String {
        val requestBody = request.body ?: return ""
        return try {
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
            val headers = response.headers
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            var buffer = source.buffer
            if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                GzipSource(buffer.clone()).use { gzippedResponseBody ->
                    buffer = Buffer()
                    buffer.writeAll(gzippedResponseBody)
                }
            }
            val contentType = responseBody.contentType()
            val charset: Charset =
                contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            return buffer.clone().readString(charset)
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