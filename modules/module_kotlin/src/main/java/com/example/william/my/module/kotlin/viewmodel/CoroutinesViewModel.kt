package com.example.william.my.module.kotlin.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.module.kotlin.data.NetworkResult
import com.example.william.my.module.kotlin.usecase.CoroutinesUseCase
import com.example.william.my.module.kotlin.utils.ThreadUtils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Android 上的 Kotlin 协程 ViewModel
 * 演示：基础异步调度、async/await 并发、supervisorScope 异常隔离、超时取消与 CoroutineExceptionHandler
 *
 * https://developer.android.google.cn/kotlin/coroutines
 */
class CoroutinesViewModel(private val useCase: CoroutinesUseCase) : ViewModel() {

    private val _coroutineLog = MutableLiveData<String>()
    val coroutineLog: LiveData<String>
        get() = _coroutineLog

    /**
     * 1. 基础异步网络请求（withContext 调度）
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            ThreadUtils.isMainThread("CoroutinesViewModel login")
            _coroutineLog.postValue("【1. 基础请求】发起登录挂起请求...")

            val result: NetworkResult<RetrofitResponse<LoginData>> =
                try {
                    useCase.login(username, password)
                } catch (e: Exception) {
                    NetworkResult.Error(Exception("Network request failed"))
                }

            when (result) {
                is NetworkResult.Success<RetrofitResponse<LoginData>> -> {
                    _coroutineLog.postValue("【1. 基础请求】成功: ${Gson().toJson(result.data)}")
                }

                is NetworkResult.Error -> {
                    _coroutineLog.postValue("【1. 基础请求】失败: ${result.exception.message}")
                }

                is NetworkResult.Loading -> {
                    _coroutineLog.postValue("【1. 基础请求】加载中……")
                }
            }
        }
    }

    /**
     * 2. async / await 并发请求合并（awaitAll 并行提速）
     */
    fun testConcurrentAsync() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            _coroutineLog.postValue("【2. 并发请求】启动 Task A (耗时 400ms) 与 Task B (耗时 500ms)...")

            val deferredA = async(Dispatchers.IO) {
                delay(400)
                "Task A 完成 (用户配置)"
            }
            val deferredB = async(Dispatchers.IO) {
                delay(500)
                "Task B 完成 (未读消息数: 99+)"
            }

            // 并行等待所有任务完成
            val results = awaitAll(deferredA, deferredB)
            val duration = System.currentTimeMillis() - startTime
            _coroutineLog.postValue("【2. 并发请求】所有并发完成，总耗时: ${duration}ms, 结果: $results")
        }
    }

    /**
     * 3. 结构化并发与异常隔离（supervisorScope）
     */
    fun testSupervisorScope() {
        viewModelScope.launch {
            _coroutineLog.postValue("【3. 异常隔离】在 supervisorScope 中并发启动子任务 1 (抛出异常) 与子任务 2 (正常)...")

            supervisorScope {
                val jobFailed = launch(Dispatchers.IO) {
                    delay(200)
                    _coroutineLog.postValue("【3. 异常隔离】子任务 1 发生致命异常，即将崩溃！")
                    throw IllegalStateException("子任务 1 模拟网络解析异常")
                }

                val jobSuccess = launch(Dispatchers.IO) {
                    delay(400)
                    _coroutineLog.postValue("【3. 异常隔离】子任务 2 未受子任务 1 崩溃影响，正常执行成功！")
                }

                jobFailed.join()
                jobSuccess.join()
            }
            _coroutineLog.postValue("【3. 异常隔离】supervisorScope 结束：子任务崩溃被有效隔离，父协程依然存活。")
        }
    }

    /**
     * 4. 协程超时处理与协作式取消（withTimeoutOrNull / isActive）
     */
    fun testTimeoutAndCancel() {
        viewModelScope.launch {
            _coroutineLog.postValue("【4. 超时与取消】测试 1: 使用 withTimeoutOrNull(300ms) 执行耗时 600ms 任务...")
            val result = withTimeoutOrNull(300) {
                delay(600)
                "执行成功"
            }
            _coroutineLog.postValue("【4. 超时与取消】超时返回结果: $result（优雅降级为 null，未抛出 TimeoutCancellationException）")

            _coroutineLog.postValue("【4. 超时与取消】测试 2: 协作式取消循环计算...")
            val job = launch(Dispatchers.Default) {
                var count = 0
                while (isActive) {
                    count++
                    if (count % 5000000 == 0) {
                        ensureActive()
                    }
                }
                _coroutineLog.postValue("【4. 超时与取消】循环被取消退出，isActive=$isActive")
            }
            delay(100)
            job.cancel()
            job.join()
            _coroutineLog.postValue("【4. 超时与取消】Job 已成功取消并回收。")
        }
    }

    /**
     * 5. CoroutineContext 上下文组合与 CoroutineExceptionHandler
     */
    fun testExceptionHandler() {
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            val coroutineName = context[CoroutineName]?.name ?: "Unnamed"
            _coroutineLog.postValue("【5. 上下文与异常处理】捕获未捕获异常: [${throwable.message}], 来源协程: $coroutineName")
        }

        // 使用 + 运算符组合 Dispatchers, CoroutineName 与 CoroutineExceptionHandler
        val context = Dispatchers.IO + CoroutineName("Antigravity-Coroutines-Worker") + exceptionHandler

        viewModelScope.launch(context) {
            _coroutineLog.postValue("【5. 上下文与异常处理】在自定义上下文 [$context] 中抛出根异常...")
            delay(200)
            throw RuntimeException("全局异常处理器测试：未捕获异常被拦截")
        }
    }
}

object CoroutinesVMFactory : ViewModelProvider.Factory {

    private val useCase = CoroutinesUseCase(Dispatchers.IO)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CoroutinesViewModel(useCase) as T
    }
}
