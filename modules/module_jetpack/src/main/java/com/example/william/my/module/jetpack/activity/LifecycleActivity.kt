package com.example.william.my.module.jetpack.activity

import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Jetpack Lifecycle — 生命周期感知型组件
 *
 * 演示 Android Jetpack 核心生命周期机制：
 * 1. [DefaultLifecycleObserver]：业务组件/Presenter 解耦监听 Activity/Fragment 生命周期事件；
 * 2. [ProcessLifecycleOwner]：全局 Application 级别的前后台切换感知（无需手动统计 Activity 计数）；
 * 3. [repeatOnLifecycle]：协程安全数据流收集，当页面处于非活跃状态（如后台 STOPPED）时自动取消收集并挂起协程，进入活跃状态时自动重启；
 * 4. [flowWithLifecycle]：Flow 级别的单流生命周期过滤操作符。
 */
@Route(path = RouterPath.Jetpack.Lifecycle)
class LifecycleActivity : BasicResponseActivity() {

    private var repeatJob: Job? = null
    private var flowWithJob: Job? = null

    // 1. 自定义业务观察者：实现 DefaultLifecycleObserver，彻底与 Activity 解耦
    private val customObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            appendLog("【组件观察者】onCreate: Activity 正在创建")
        }

        override fun onResume(owner: LifecycleOwner) {
            appendLog("【组件观察者】onResume: Activity 进入前台交互状态")
        }

        override fun onPause(owner: LifecycleOwner) {
            appendLog("【组件观察者】onPause: Activity 失去焦点")
        }

        override fun onDestroy(owner: LifecycleOwner) {
            // Activity 销毁时清理资源，避免内存泄漏
            appendLog("【组件观察者】onDestroy: 自动反注册与清理")
        }
    }

    // 2. 全局应用级别观察者：ProcessLifecycleOwner
    private val appProcessObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            appendLog("【应用前后台】App 进入前台 (ProcessLifecycleOwner onStart)")
        }

        override fun onStop(owner: LifecycleOwner) {
            appendLog("【应用前后台】App 进入后台 (ProcessLifecycleOwner onStop)")
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            """
            Jetpack Lifecycle 体系演进：
            • DefaultLifecycleObserver：替代传统的 BaseActivity 模板方法回调；
            • ProcessLifecycleOwner：进程级全局前后台状态监听；
            • repeatOnLifecycle / flowWithLifecycle：现代协程流生命周期感知安全收集。
            """.trimIndent()
        )

        // 绑定组件生命周期观察者
        lifecycle.addObserver(customObserver)

        // 注册全局前后台监听
        ProcessLifecycleOwner.get().lifecycle.addObserver(appProcessObserver)
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 检查当前 Activity 生命周期状态 (Lifecycle.currentState)",
            "2. 启动 repeatOnLifecycle(STARTED) 数据流监听 (退到后台自动挂起)",
            "3. 启动 flowWithLifecycle(RESUMED) 单流监听",
            "4. 模拟触发组件生命周期事件日志"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> checkLifecycleState()
            1 -> testRepeatOnLifecycle()
            2 -> testFlowWithLifecycle()
            3 -> {
                appendLog("当前组件已注册观察者，尝试切换 App 到后台或按返回键观察控制台与日志区回调")
            }
        }
    }

    private fun checkLifecycleState() {
        val state = lifecycle.currentState
        val isAtLeastStarted = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        val isAtLeastResumed = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        appendLog("当前生命周期: $state | isAtLeast(STARTED)=$isAtLeastStarted | isAtLeast(RESUMED)=$isAtLeastResumed")
    }

    private fun testRepeatOnLifecycle() {
        repeatJob?.cancel()
        appendLog("已启动 repeatOnLifecycle(STARTED) 协程数据流发射（每秒 1 次）...")
        repeatJob = lifecycleScope.launch {
            // 当生命周期到达 STARTED 时启动协程，当降至 STOPPED（如按 Home 键切后台）时自动取消
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val tickerFlow = flow {
                    var count = 0
                    while (true) {
                        emit(count++)
                        delay(1000)
                    }
                }
                tickerFlow.collect { count ->
                    updateLog("repeatOnLifecycle", "repeatOnLifecycle 持续收到数据流: 第 $count 拍 (仅在 STARTED/RESUMED 下活跃)")
                }
            }
        }
    }

    private fun testFlowWithLifecycle() {
        flowWithJob?.cancel()
        appendLog("已启动 flowWithLifecycle(RESUMED) 操作符数据流...")
        val tickerFlow = flow {
            var count = 0
            while (true) {
                emit(count++)
                delay(1000)
            }
        }
        flowWithJob = lifecycleScope.launch {
            tickerFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .collect { count ->
                    updateLog("flowWithLifecycle", "flowWithLifecycle 持续收到数据流: 第 $count 拍 (仅在 RESUMED 下活跃)")
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(customObserver)
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appProcessObserver)
    }
}
