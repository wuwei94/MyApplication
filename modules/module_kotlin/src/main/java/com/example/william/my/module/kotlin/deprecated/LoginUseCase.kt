package com.example.william.my.module.kotlin.deprecated

import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.module.kotlin.data.NetworkResult
import com.example.william.my.module.kotlin.utils.ThreadUtils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Android 上的 Kotlin 协程用例（HttpURLConnection 历史实现）
 * https://developer.android.google.cn/kotlin/coroutines
 * <p>
 * suspend -> Result
 */
@Deprecated(message = "HttpURLConnection")
class LoginUseCase(private val defaultDispatcher: CoroutineDispatcher) {

    /**
     * 2. 使用协程确保主线程安全
     * <p>
     * 将 协程 切换到 I/O 调度，确保主线程安全
     * Move the execution of the coroutine to the I/O dispatcher
     */
    suspend fun login(username: String, password: String): NetworkResult<LoginData> {

        return withContext(defaultDispatcher) {
            // 打印线程
            ThreadUtils.isMainThread("LoginUseCase login")

            // 阻塞网络请求
            // Blocking network request code
            makeLoginRequest(username, password)
        }
    }

    /**
     * 1. 在后台线程中执行
     * <p>
     * 发出网络请求，阻塞当前线程
     * Function that makes the network request, blocking the current thread
     */
    private fun makeLoginRequest(username: String, password: String): NetworkResult<LoginData> {
        // 打印线程
        ThreadUtils.isMainThread("LoginUseCase makeLoginRequest")

        val url = URL(Constants.Url_Login)
        val jsonString = "username=$username&password=$password"
        (url.openConnection() as? HttpURLConnection)?.run {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            // setRequestProperty("Content-Type", "application/json; utf-8")// 发送的实体数据的数据类型
            // setRequestProperty("Accept", "application/json") // 希望接受的数据类型
            doOutput = true
            outputStream.write(jsonString.toByteArray())
            return NetworkResult.Success(parseInputStream(inputStream))
        }
        return NetworkResult.Error(Exception("Cannot open HttpURLConnection"))
    }

    /**
     * 解析输入流
     */
    private fun parseInputStream(input: InputStream): LoginData {
        val msg = StringBuilder()
        val reader = BufferedReader(InputStreamReader(input))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            msg.append(line)
        }
        reader.close()
        val response = msg.toString()
        return Gson().fromJson(response, LoginData::class.java)
    }
}