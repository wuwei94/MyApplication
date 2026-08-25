package com.example.william.my.module.kotlin.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.kotlin.data.NetworkResult
import com.example.william.my.module.kotlin.viewmodel.FlowVMFactory
import com.example.william.my.module.kotlin.viewmodel.FlowViewModel
import kotlinx.coroutines.launch

/**
 * Kotlin Flow — 响应式数据流
 *
 * Flow 是 Kotlin 协程的响应式数据流，用于处理异步数据流。
 *
 * 核心特性：
 * 1. 冷流机制：仅在 collect 时触发生产，资源零浪费
 * 2. 变换操作符：map / filter / take 惰性数据流变换
 * 3. 组合操作符：zip（严格对齐配对）与 combine（最新值组合）
 * 4. 防抖与搜索：debounce + distinctUntilChanged + flatMapLatest 优雅处理输入检索
 * 5. 热流架构：StateFlow（状态持有）与 SharedFlow（事件广播）
 * 6. 健壮性保障：catch 异常降级与 retry 自动重试
 *
 * https://developer.android.google.cn/kotlin/flow
 */
@Route(path = RouterPath.Kotlin.Flow)
class FlowActivity : BasicResponseActivity() {

    private val mViewModel: FlowViewModel by viewModels {
        FlowVMFactory
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 Kotlin Flow：冷流收集、数据变换、双流组合、防抖搜索、热流对比与异常重试")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 基础冷流登录请求与生命周期收集",
            "2. 数据流基础变换与过滤（map / filter / take）",
            "3. 数据流组合操作符（zip vs combine）",
            "4. 防抖搜索与动态流切换（debounce + flatMapLatest）",
            "5. 热流对比（StateFlow 状态持有 vs SharedFlow 事件广播）",
            "6. 流的异常捕获与自动重试（retry + catch）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> mViewModel.login(Constants.Value_Username, Constants.Value_Password)
            1 -> mViewModel.testFlowTransform()
            2 -> mViewModel.testZipAndCombine()
            3 -> mViewModel.testDebounceAndFlatMapLatest()
            4 -> mViewModel.testHotFlows()
            5 -> mViewModel.testRetryAndCatch()
        }
    }

    override fun observeViewModel() {
        super.observeViewModel()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 收集登录 UI 状态流
                launch {
                    mViewModel.uiState.collect { uiState ->
                        when (uiState) {
                            is NetworkResult.Loading -> {
                                appendLog("【Flow UIState】Loading...")
                            }

                            is NetworkResult.Success -> {
                                appendLog("【Flow UIState】Success: ${uiState.string()}")
                            }

                            is NetworkResult.Error -> {
                                appendLog("【Flow UIState】Error: ${uiState.string()}")
                            }
                        }
                    }
                }

                // 收集通用 Flow 日志流
                launch {
                    mViewModel.flowLog.collect { log ->
                        appendLog(log)
                    }
                }
            }
        }
    }
}