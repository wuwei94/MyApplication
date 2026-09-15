package com.example.william.my.module.kotlin.activity

import android.os.Bundle
import androidx.activity.viewModels
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.kotlin.viewmodel.CoroutinesVMFactory
import com.example.william.my.module.kotlin.viewmodel.CoroutinesViewModel

/**
 * Kotlin Coroutines — 协程并发与作用域
 *
 * 协程是 Kotlin 的轻量级线程管理方案，通过挂起函数与结构化并发简化异步编程。
 * 本页演示线程调度、并发聚合、异常隔离、超时取消与全局异常处理五大核心能力。
 *
 * 核心机制与避坑点：
 * 1. 结构化并发：自动管理协程生命周期与层次关系，父协程取消时子协程一并取消
 * 2. 线程调度：Dispatchers.Main / IO / Default 灵活切换，withContext 实现线程切换
 * 3. 异步并发：async / await 并行执行与 awaitAll 结果聚合
 * 4. 异常隔离：supervisorScope / SupervisorJob 隔离子协程故障，防止级联取消
 * 5. 取消与超时：withTimeoutOrNull 超时降级，isActive 协作式响应取消信号
 * 6. 异常处理器：CoroutineExceptionHandler 拦截未捕获异常
 *
 * https://developer.android.google.cn/kotlin/coroutines
 */
@Route(path = RouterPath.Kotlin.Coroutines)
class CoroutinesActivity : BasicResponseActivity() {

    private val viewModel: CoroutinesViewModel by viewModels {
        CoroutinesVMFactory
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 Kotlin 协程：线程切换、并发聚合、异常隔离、超时取消与全局异常处理")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 基础异步网络请求（withContext 调度）",
        "2. async / await 并发请求合并（awaitAll 并行提速）",
        "3. 结构化并发与异常隔离（supervisorScope）",
        "4. 协程超时处理与协作式取消（withTimeoutOrNull / isActive）",
        "5. 上下文元素组合与异常处理器（CoroutineExceptionHandler）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> viewModel.login(Constants.Value_Username, Constants.Value_Password)
            1 -> viewModel.testConcurrentAsync()
            2 -> viewModel.testSupervisorScope()
            3 -> viewModel.testTimeoutAndCancel()
            4 -> viewModel.testExceptionHandler()
        }
    }

    override fun observeViewModel() {
        super.observeViewModel()

        viewModel.coroutineLog.observe(this) { log ->
            appendLog(log)
        }
    }
}
