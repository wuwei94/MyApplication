package com.example.william.my.module.kotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.william.my.basic.basic_repo.bean.LoginData
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.module.kotlin.data.NetworkResult
import com.example.william.my.module.kotlin.usecase.FlowUseCase
import com.example.william.my.module.kotlin.utils.ThreadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Android 上的 Kotlin 数据流 ViewModel
 * 演示：冷流收集、变换操作符、zip/combine 组合、debounce/flatMapLatest 防抖、StateFlow vs SharedFlow、catch/retry 异常重试
 *
 * https://developer.android.google.cn/kotlin/flow
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class FlowViewModel(private val useCase: FlowUseCase) : ViewModel() {

    // 1. 登录 UI 状态流 (StateFlow)
    private val _uiState: MutableStateFlow<NetworkResult<RetrofitResponse<LoginData>>> =
        MutableStateFlow(NetworkResult.Loading)
    val uiState: StateFlow<NetworkResult<RetrofitResponse<LoginData>>> =
        _uiState.asStateFlow()

    // 独立日志输出流，用于驱动页面日志展示
    private val _flowLog = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val flowLog: SharedFlow<String> = _flowLog.asSharedFlow()

    /**
     * 1. 基础冷流登录请求与生命周期收集
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            ThreadUtils.isMainThread("FlowViewModel login")
            _flowLog.emit("【1. 基础冷流】发起 Flow 登录请求...")

            val flow: Flow<RetrofitResponse<LoginData>> =
                useCase.login(username, password)

            flow
                .onStart {
                    _uiState.value = NetworkResult.Loading
                }
                .catch { exception ->
                    _uiState.value = NetworkResult.Error(Exception(exception))
                    _flowLog.emit("【1. 基础冷流】登录异常: ${exception.message}")
                }
                .onCompletion {
                    _flowLog.emit("【1. 基础冷流】请求链路完成 (onCompletion)")
                }
                .collect { response ->
                    _uiState.value = NetworkResult.Success(response)
                }
        }
    }

    /**
     * 2. 数据流基础变换与过滤操作符（map / filter / take）
     */
    fun testFlowTransform() {
        viewModelScope.launch {
            _flowLog.emit("【2. 变换操作符】源数据: 1..6，经过 filter(偶数) -> map(计算平方) -> take(2)...")
            flowOf(1, 2, 3, 4, 5, 6)
                .filter { it % 2 == 0 }
                .map { "偶数项: $it -> 平方: ${it * it}" }
                .take(2)
                .collect {
                    _flowLog.emit("【2. 变换操作符】接收: $it")
                }
        }
    }

    /**
     * 3. 数据流组合操作符（zip vs combine）
     */
    fun testZipAndCombine() {
        viewModelScope.launch {
            val flowLetters = flow {
                emit("A")
                delay(200)
                emit("B")
                delay(200)
                emit("C")
            }
            val flowNumbers = flow {
                emit(1)
                delay(100)
                emit(2)
                delay(100)
                emit(3)
            }

            _flowLog.emit("【3. zip 操作符】1 对 1 严格按序号配对:")
            flowLetters.zip(flowNumbers) { letter, number ->
                "[$letter : $number]"
            }.collect {
                _flowLog.emit("【3. zip 结果】$it")
            }

            delay(100)
            _flowLog.emit("【3. combine 操作符】任意一方有新值时与另一方最新值组合:")
            val flowLetters2 = flow {
                emit("X")
                delay(200)
                emit("Y")
            }
            val flowNumbers2 = flow {
                emit(10)
                delay(100)
                emit(20)
                delay(150)
                emit(30)
            }
            flowLetters2.combine(flowNumbers2) { letter, number ->
                "($letter + $number)"
            }.collect {
                _flowLog.emit("【3. combine 结果】$it")
            }
        }
    }

    /**
     * 4. 防抖搜索与动态流切换（debounce + distinctUntilChanged + flatMapLatest）
     */
    fun testDebounceAndFlatMapLatest() {
        viewModelScope.launch {
            _flowLog.emit("【4. 防抖搜索】模拟输入流: 'k' -> 'ko' -> 'kot' -> 'kot' -> 'kotlin'...")

            val searchInputFlow = flow {
                emit("k")
                delay(50)
                emit("ko")
                delay(50)
                emit("kot")
                delay(300) // 停顿超过 200ms，触发一次
                emit("kot") // 重复值，将被 distinctUntilChanged 过滤
                delay(50)
                emit("kotlin") // 最终输入
            }

            searchInputFlow
                .debounce(200) // 200ms 防抖
                .distinctUntilChanged() // 过滤连续相同输入
                .flatMapLatest { query ->
                    // 模拟网络搜索流，收到新 query 时自动取消旧搜索流
                    flow {
                        _flowLog.emit("【4. 防抖搜索】触发检索关键词: [$query]")
                        delay(150)
                        emit("检索结果: 关于 '$query' 的 10 条匹配项")
                    }
                }
                .collect { result ->
                    _flowLog.emit("【4. 防抖搜索】展示: $result")
                }
        }
    }

    /**
     * 5. 热流对比（StateFlow 状态持有 vs SharedFlow 事件广播）
     */
    fun testHotFlows() {
        viewModelScope.launch {
            _flowLog.emit("【5. 热流对比】StateFlow 具备初始值并保留最新状态；SharedFlow 适合广播一次性事件。")

            val oneOffEventFlow = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)

            val collectorJob = launch {
                oneOffEventFlow.collect { event ->
                    _flowLog.emit("【5. SharedFlow 订阅者收到事件】$event")
                }
            }

            delay(50)
            _flowLog.emit("【5. SharedFlow 发送事件】发送 Toast 通知事件...")
            oneOffEventFlow.emit("ShowToast: '操作已成功完成！'")
            delay(100)
            collectorJob.cancel()
        }
    }

    /**
     * 6. 流的异常捕获与自动重试（retry + catch）
     */
    fun testRetryAndCatch() {
        viewModelScope.launch {
            _flowLog.emit("【6. 异常重试】模拟不稳定网络流 (前 2 次抛异常，第 3 次成功)...")
            var attemptCount = 0

            val unstableFlow = flow {
                attemptCount++
                _flowLog.emit("【6. 异常重试】第 $attemptCount 次尝试请求数据...")
                if (attemptCount < 3) {
                    throw IOException("网络超时异常 (attempt=$attemptCount)")
                }
                emit("成功获取服务端核心配置数据！")
            }

            unstableFlow
                .retry(retries = 2) { cause ->
                    val shouldRetry = cause is IOException
                    _flowLog.emit("【6. 异常重试】捕获 [${cause.message}], 是否重试: $shouldRetry")
                    shouldRetry
                }
                .catch { exception ->
                    _flowLog.emit("【6. 异常重试】重试耗尽，降级处理: ${exception.message}")
                    emit("兜底本地缓存数据")
                }
                .collect { data ->
                    _flowLog.emit("【6. 异常重试】最终收集结果: $data")
                }
        }
    }
}

object FlowVMFactory : ViewModelProvider.Factory {

    private val useCase = FlowUseCase(Dispatchers.IO)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FlowViewModel(useCase) as T
    }
}
