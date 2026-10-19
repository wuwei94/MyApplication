package com.example.william.my.module.ml.download

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * 下载器的两条关键路径回归测试
 *
 * 用 [ServerSocket] 手写一个只认 `GET` + `Range` 的极简 HTTP 服务端：Android 单测的
 * classpath 里没有 `com.sun.net.httpserver`（`jdk.httpserver` 模块不可见），而这段逻辑
 * 又必须脱离外网验证。覆盖两个容易静默出错的点：
 * 1. **失败不留 target**：半截文件落地后 `ModelStore` 会把它当成可用模型；
 * 2. **`.part` 续传**：206 的 `Content-Length` 是剩余长度而非总长，校验口径错了就会
 *    把续传误判成截断（这条用例就是为了钉住这个口径）。
 */
class ModelDownloaderTest {

    private lateinit var server: MiniHttpServer
    private lateinit var filesDir: File
    private lateinit var payload: ByteArray

    @Before
    fun setUp() {
        payload = ByteArray(160 * 1024) { (it % 251).toByte() }
        filesDir = Files.createTempDirectory("ml-downloader-test").toFile()
        server = MiniHttpServer(payload)
    }

    @After
    fun tearDown() {
        server.close()
        filesDir.deleteRecursively()
    }

    private fun model(path: String): RemoteModel = RemoteModel(
        fileName = "test.tflite",
        approxBytes = payload.size.toLong(),
        directUrl = "${server.baseUrl}$path",
    )

    @Test
    fun serverError_throwsWithoutLeavingTargetFile() {
        server.route("/broken", 500)

        val model = model("/broken")
        val error = assertThrows(IOException::class.java) {
            runBlocking { ModelDownloader(filesDir).download(model) }
        }

        assertTrue(error.message.orEmpty().contains("500"))
        assertFalse(ModelStore.fileIn(filesDir, model).exists())
    }

    @Test
    fun resumesFromExistingPartFile() = runBlocking {
        server.route("/ok", 200)

        val model = model("/ok")
        // 伪造「上一次下到 4 KB 就断了」
        ModelStore.partFileIn(filesDir, model).writeBytes(payload.copyOfRange(0, 4096))

        val file = ModelDownloader(filesDir).download(model)

        assertArrayEquals(payload, file.readBytes())
        // 完整落地后临时文件必须已被 rename 掉
        assertFalse(ModelStore.partFileIn(filesDir, model).exists())
    }

    /**
     * 只支持单次 `GET`（可带 `Range`），每个响应都以 `Connection: close` 收尾
     *
     * 客户端是 `HttpURLConnection`，它会在连接关闭后自动重连，因此不必实现 keep-alive。
     */
    private class MiniHttpServer(private val payload: ByteArray) : Closeable {

        private val routes = ConcurrentHashMap<String, Int>()

        private val socket = ServerSocket(0, 0, InetAddress.getByName("127.0.0.1"))

        @Volatile
        private var running = true

        val baseUrl: String = "http://127.0.0.1:${socket.localPort}"

        private val worker = thread(isDaemon = true, name = "mini-http") {
            while (running) {
                val client = try {
                    socket.accept()
                } catch (e: IOException) {
                    break
                }
                runCatching { handle(client) }
            }
        }

        fun route(path: String, status: Int) {
            routes[path] = status
        }

        private fun handle(client: Socket) {
            client.use { connection ->
                val reader = connection.getInputStream().bufferedReader()
                val requestLine = reader.readLine() ?: return
                val path = requestLine.split(' ').getOrNull(1) ?: return
                var rangeStart = 0
                while (true) {
                    val line = reader.readLine() ?: break
                    if (line.isEmpty()) break
                    if (line.startsWith("Range:", ignoreCase = true)) {
                        rangeStart = line.substringAfter("bytes=").substringBefore('-').trim().toIntOrNull() ?: 0
                    }
                }

                val status = routes[path] ?: 404
                val output = connection.getOutputStream()
                if (status != 200 && status != 206) {
                    output.write("HTTP/1.1 $status Error\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".toByteArray())
                    output.flush()
                    return
                }

                val body = if (rangeStart > 0) payload.copyOfRange(rangeStart, payload.size) else payload
                val code = if (rangeStart > 0) 206 else 200
                val header = buildString {
                    append("HTTP/1.1 $code OK\r\n")
                    append("Content-Length: ${body.size}\r\n")
                    if (code == 206) {
                        append("Content-Range: bytes $rangeStart-${payload.size - 1}/${payload.size}\r\n")
                    }
                    append("Connection: close\r\n\r\n")
                }
                output.write(header.toByteArray())
                output.write(body)
                output.flush()
            }
        }

        override fun close() {
            running = false
            socket.close()
            worker.join(1_000)
        }
    }
}
