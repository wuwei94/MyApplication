package com.example.william.my.module.performance.activity

import android.os.Bundle
import androidx.startup.AppInitializer
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.performance.startup.LogSdk
import com.example.william.my.module.performance.startup.LogSdkInitializer
import com.example.william.my.module.performance.startup.ManualLazyInitializer
import com.example.william.my.module.performance.startup.ManualLazySdk
import com.example.william.my.module.performance.startup.SecuritySdk
import com.example.william.my.module.performance.startup.SecuritySdkInitializer

/**
 * Jetpack App Startup — 应用初始化性能优化组件
 *
 * 核心设计与性能价值：
 * 1. 【减少 ContentProvider 数量】：避免每个第三方 SDK 各自声明 ContentProvider 导致冷启动耗时成倍增加；
 * 2. 【统一拓扑排序】：通过 Initializer.dependencies() 自动解析有向无环图（DAG），确保依赖组件严格按序初始化；
 * 3. 【按需延迟初始化】：支持通过 AppInitializer.getInstance(context).initializeComponent(...) 手动延迟触发，实现非核心组件懒加载。
 */
@Route(path = RouterPath.Performance.Startup)
class StartupActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            """
            Jetpack App Startup 启动性能优化实践：
            • 统一托管：单个 InitializationProvider 聚合所有 SDK 初始化，消除多 Provider 启动开销；
            • 自动依赖拓扑：dependencies() 定义先后依赖顺序；
            • 懒加载与按需：手动 initializeComponent 规避 Application.onCreate 阻塞。
            """.trimIndent(),
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 检查自动初始化状态 (LogSdk / SecuritySdk)",
        "2. 执行手动延迟按需初始化 (ManualLazyInitializer)",
        "3. 查询已缓存组件初始化实例 (isEagerlyInitialized)",
        "4. 性能原理：多 ContentProvider vs App Startup 耗时对比分析",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> checkAutoInitializedSdks()
            1 -> triggerManualLazyInit()
            2 -> checkEagerlyInitializedStatus()
            3 -> showPerformanceAnalysis()
        }
    }

    /**
     * 1. 检查自动初始化的 SDK 实例（通过 AppInitializer.initializeComponent 获取已初始化的单例）
     */
    private fun checkAutoInitializedSdks() {
        appendLog("── 正在查询自动初始化的 SDK ──")
        val appInitializer = AppInitializer.getInstance(this)

        // 若已经在 Manifest 的 InitializationProvider 中自动加载，initializeComponent 会直接返回已缓存的单例
        val logSdk: LogSdk = appInitializer.initializeComponent(LogSdkInitializer::class.java)
        appendLog("✓ LogSdk 单例获取成功: initTime=${logSdk.initTimeMillis}, version=${logSdk.version}")

        val securitySdk: SecuritySdk = appInitializer.initializeComponent(SecuritySdkInitializer::class.java)
        appendLog("✓ SecuritySdk 单例获取成功: initTime=${securitySdk.initTimeMillis} (依赖 LogSdk 前置完成)")
    }

    /**
     * 2. 手动延迟按需初始化
     */
    private fun triggerManualLazyInit() {
        appendLog("── 开始执行手动延迟按需初始化 ──")
        val appInitializer = AppInitializer.getInstance(this)
        val startTime = System.currentTimeMillis()
        val lazySdk: ManualLazySdk = appInitializer.initializeComponent(ManualLazyInitializer::class.java)
        val costTime = System.currentTimeMillis() - startTime
        appendLog("✓ ManualLazySdk 初始化完成: state=${lazySdk.state}, 耗时=${costTime}ms")
    }

    /**
     * 3. 检查初始化器是否由框架自动初始化
     */
    private fun checkEagerlyInitializedStatus() {
        appendLog("── 检查组件是否被声明为自动急迫初始化 ──")
        val appInitializer = AppInitializer.getInstance(this)

        val logSdkEager = appInitializer.isEagerlyInitialized(LogSdkInitializer::class.java)
        val securitySdkEager = appInitializer.isEagerlyInitialized(SecuritySdkInitializer::class.java)
        val manualLazyEager = appInitializer.isEagerlyInitialized(ManualLazyInitializer::class.java)

        appendLog("• LogSdkInitializer (自动): isEagerlyInitialized = $logSdkEager")
        appendLog("• SecuritySdkInitializer (自动): isEagerlyInitialized = $securitySdkEager")
        appendLog("• ManualLazyInitializer (按需): isEagerlyInitialized = $manualLazyEager")
    }

    /**
     * 4. 耗时与性能分析说明
     */
    private fun showPerformanceAnalysis() {
        appendLog(
            """
            ── App Startup 冷启动性能优化分析 ──
            1. 为什么弃用各自独立的 ContentProvider？
               • Android 进程启动时，系统会在 Application.onCreate 之前串行拉起所有注册的 ContentProvider.onCreate()；
               • 每个 ContentProvider 在 Framework 层存在 IPC 注册与 Binder 消耗（通常 1~5ms / 个）；
               • 多个 SDK 叠加会导致冷启动白屏时间明显增加。
            2. App Startup 方案收益：
               • 将 N 个 ContentProvider 减少为 1 个 InitializationProvider；
               • 内部由 DAG 算法自动拓扑排序并并发/链式调用 Initializer.create()；
               • 支持 tools:node="remove" 完全移除不需要在启动阶段初始化的 SDK，转为业务触发时懒加载。
            """.trimIndent(),
        )
    }
}
