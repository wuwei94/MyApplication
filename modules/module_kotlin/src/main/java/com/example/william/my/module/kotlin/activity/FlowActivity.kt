package com.example.william.my.module.kotlin.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.base.Constants
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
 * 1. 冷流：只有在收集时才会执行，避免资源浪费
 * 2. 顺序执行：数据按顺序发射，保证数据一致性
 * 3. 协程支持：基于协程，支持挂起函数
 * 4. 生命周期感知：结合 repeatOnLifecycle，自动管理订阅
 *
 * 基本用法：
 * ```kotlin
 * // 创建 Flow
 * fun fetchData(): Flow<String> = flow {
 *     emit("Loading...")
 *     delay(1000)
 *     emit("Data loaded")
 * }
 *
 * // 收集 Flow
 * lifecycleScope.launch {
 *     repeatOnLifecycle(Lifecycle.State.STARTED) {
 *         fetchData().collect { value ->
 *             // 处理数据
 *         }
 *     }
 * }
 * ```
 *
 * 适用场景：
 * - 网络请求、数据库查询等异步操作
 * - 数据流处理、事件流处理
 * - 需要生命周期感知的场景
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
        showDescription("点击下方列表项通过 Flow 发起登录请求")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("Flow 登录请求")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        if (position == 0) {
            appendLog("发起 Flow 登录请求...")
            mViewModel.login(Constants.Value_Username, Constants.Value_Password)
        }
    }

    override fun observeViewModel() {
        super.observeViewModel()

        // 在生命周期范围内启动协同程序
        // Start a coroutine in the lifecycle scope
        lifecycleScope.launch {
            // 每次生命周期处于已启动状态（或更高）时，在新的协同路由中启动该块，并在其 STOPPED 时取消该块。
            // repeatOnLifecycle launches the block in a new coroutine every time the
            // lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 触发流并开始监听值。
                // Note that this happens when lifecycle is STARTED and stops collecting when the lifecycle is STOPPED
                // Trigger the flow and start listening for values.
                // Note that this happens when lifecycle is STARTED and stops collecting when the lifecycle is STOPPED
                mViewModel.uiState.collect { uiState ->
                    // New value received
                    when (uiState) {
                        is NetworkResult.Loading -> {
                            appendLog("Flow 状态: Loading")
                        }

                        is NetworkResult.Success -> {
                            appendLog("Flow 成功: ${uiState.string()}")
                        }

                        is NetworkResult.Error -> {
                            appendLog("Flow 失败: ${uiState.string()}")
                        }
                    }
                }
            }
        }
    }
}