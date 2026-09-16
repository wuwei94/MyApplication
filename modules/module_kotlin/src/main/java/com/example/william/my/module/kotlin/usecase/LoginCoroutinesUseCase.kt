package com.example.william.my.module.kotlin.usecase

import com.example.william.my.basic.basic_repo.api.NetworkApi
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.core.retrofit.createApi
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.module.kotlin.data.NetworkResult
import com.example.william.my.module.kotlin.utils.ThreadUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Android 上的 Kotlin 协程用例（登录）
 * https://developer.android.google.cn/kotlin/coroutines
 * <p>
 * suspend -> Result；演示模块直连 [NetworkApi]，不经过 Repository。
 */
class LoginCoroutinesUseCase(private val defaultDispatcher: CoroutineDispatcher) {

    private val api = createApi(NetworkApi::class.java)

    suspend fun login(
        username: String,
        password: String,
    ): NetworkResult<RetrofitResponse<LoginData>> = withContext(defaultDispatcher) {
        // 打印线程
        ThreadUtils.isMainThread("LoginCoroutinesUseCase login")

        // 阻塞网络请求
        NetworkResult.Success(api.loginSuspend(username, password))
    }
}
