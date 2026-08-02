package com.example.william.my.module.kotlin.repo

import com.example.william.my.basic.basic_repo.api.NetworkApi
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.core.retrofit.createApi
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.module.kotlin.data.NetworkResult
import com.example.william.my.module.kotlin.utils.ThreadUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Android 上的 Kotlin 协程
 * https://developer.android.google.cn/kotlin/coroutines
 * <p>
 * suspend -> Result
 */
class CoroutinesRepository(private val defaultDispatcher: CoroutineDispatcher) {

    private val api = createApi(NetworkApi::class.java)

    suspend fun login(
        username: String,
        password: String
    ): NetworkResult<RetrofitResponse<LoginData?>> {

        return withContext(defaultDispatcher) {
            //打印线程
            ThreadUtils.isMainThread("CoroutinesRepository login")

            // 阻塞网络请求
            // Blocking network request code
            NetworkResult.Success(api.loginSuspend(username, password))
        }
    }

    companion object {
        private val TAG = CoroutinesRepository::class.java.simpleName
    }
}