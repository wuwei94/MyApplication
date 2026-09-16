package com.example.william.my.module.kotlin.usecase

import com.example.william.my.basic.basic_repo.api.NetworkApi
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.core.retrofit.createApi
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.module.kotlin.utils.ThreadUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Android 上的 Kotlin 数据流用例（登录）
 * https://developer.android.google.cn/kotlin/flow
 * <p>
 * suspend -> Flow；演示模块直连 [NetworkApi]，不经过 Repository，异常由收集方处理。
 */
class LoginFlowUseCase(private val defaultDispatcher: CoroutineDispatcher) {

    private val api = createApi(NetworkApi::class.java)

    /**
     * 1. 创建数据流
     */
    private fun createFlow(username: String, password: String): Flow<RetrofitResponse<LoginData>> = flow {
        // 打印线程
        ThreadUtils.isMainThread("LoginFlowUseCase login")

        // 将请求结果发送到流
        emit(api.loginSuspend(username, password))
    }
        // 在 IO 调度器上执行
        .flowOn(defaultDispatcher)

    /**
     * 2. 修改数据流
     * 这些操作是惰性的，不会触发流。它们只是转换流在该时间点发出的当前值。
     */
    fun login(username: String, password: String): Flow<RetrofitResponse<LoginData>> = createFlow(username, password)
        // 在默认调度程序上执行
        .map { news ->
            news
        }
        // 在默认调度程序上执行
        .onEach {
        }
        // flowOn 影响上游的 flow
        .flowOn(defaultDispatcher)
}
