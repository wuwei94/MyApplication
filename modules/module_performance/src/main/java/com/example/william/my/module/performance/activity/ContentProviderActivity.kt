package com.example.william.my.module.performance.activity

import android.content.ContentValues
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.performance.provider.AutoInitProvider
import com.example.william.my.module.performance.provider.AutoInitSdk

/**
 * ContentProvider — 启动早期无侵入自动初始化与冷启动耗时剖析
 *
 * 核心机制与性能分析：
 * 1. 【无侵入初始化模式】：利用系统在 `Application.onCreate` 之前优先执行 `ContentProvider.onCreate` 的机制，自动捕获 Context 完成 SDK 初始化；
 * 2. 【冷启动耗时痛点】：每个 ContentProvider 在 AMS 与 ActivityThread 层面均存在反射拉起、Binder 绑定与 IPC 开销（单个约 1~5ms），大量 SDK 各自注册 Provider 会导致冷启动白屏明显拉长；
 * 3. 【演进趋势】：由此催生了 Jetpack App Startup，通过单个 InitializationProvider 统一聚合管理所有库初始化。
 */
@Route(path = RouterPath.Performance.ContentProvider)
class ContentProviderActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            """
            ContentProvider 启动初始化与性能分析：
            • 早期无侵入模式：利用 ContentProvider.onCreate 优先时序自动捕获 Context；
            • 冷启动性能瓶颈：多 Provider 串行反射与 Binder 开销显著增加冷启动白屏；
            • 现代优化方案：收拢聚合至单个 Provider，或迁移至 Jetpack App Startup。
            """.trimIndent()
        )
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 检查 AutoInitSdk 自动初始化状态与元数据",
            "2. 深度剖析：Android 进程启动与 ContentProvider 执行时序链",
            "3. 通过 ContentResolver 查询 Provider 共享数据 (query)",
            "4. 通过 ContentResolver 写入新数据项 (insert)",
            "5. 性能反思：多 ContentProvider 对冷启动的负面影响与治理"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> checkAutoInitSdkState()
            1 -> showStartupSequenceAnalysis()
            2 -> queryProviderData()
            3 -> insertProviderData()
            4 -> showArchitectureEvolution()
        }
    }

    /**
     * 1. 检查 AutoInitSdk 自动初始化状态
     */
    private fun checkAutoInitSdkState() {
        appendLog("── 正在查询 AutoInitSdk 状态 ──")
        val isInit = AutoInitSdk.isInitialized
        val time = AutoInitSdk.initTimestamp
        val thread = AutoInitSdk.initThreadName
        val hasContext = AutoInitSdk.appContext != null

        appendLog("• isInitialized = $isInit")
        appendLog("• initTimestamp = $time (进程启动早期执行)")
        appendLog("• initThreadName = $thread (主线程)")
        appendLog("• appContext 获取成功 = $hasContext (${AutoInitSdk.appContext?.packageName})")
        if (isInit) {
            appendLog("✓ SDK 已由 AutoInitProvider 在 App 启动早期无感知初始化完成！")
        } else {
            appendLog("✗ SDK 尚未完成初始化")
        }
    }

    /**
     * 2. 深度剖析 Android 启动时序
     */
    private fun showStartupSequenceAnalysis() {
        appendLog(
            """
            ── Android 应用进程拉起时序链（ActivityThread）──
            1. Linux fork 产生新应用进程；
            2. ActivityThread.main() 启动主线程 Looper；
            3. ActivityThread.attach() 与 AMS (ActivityManagerService) 建立 Binder 通信；
            4. ActivityThread.handleBindApplication() 开始装载应用：
               ├─ a. 创建 LoadedApk 与 AppContext；
               ├─ b. 创建 Instrumentation 与 Application 实例（调用 Application.attachBaseContext）；
               ├─ c. 【关键】：ActivityThread.installContentProviders() 实例化并依次调用所有在 AndroidManifest 中注册的 ContentProvider.onCreate()；
               ├─ d. 调用 Application.onCreate()；
            5. ActivityThread.performLaunchActivity() 启动主入口 Activity (onCreate -> onStart -> onResume)。

            结论：ContentProvider.onCreate 执行时机 介于 Application.attachBaseContext 与 Application.onCreate 之间！
            """.trimIndent()
        )
    }

    /**
     * 3. 通过 ContentResolver 查询数据
     */
    private fun queryProviderData() {
        appendLog("── 执行 ContentResolver.query ──")
        try {
            val cursor = contentResolver.query(
                AutoInitProvider.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            cursor?.use {
                val count = it.count
                appendLog("✓ 查询成功，共 $count 条数据：")
                val idIndex = it.getColumnIndex("_id")
                val nameIndex = it.getColumnIndex("item_name")
                while (it.moveToNext()) {
                    val id = if (idIndex != -1) it.getLong(idIndex) else -1
                    val name = if (nameIndex != -1) it.getString(nameIndex) else "unknown"
                    appendLog("  [$id] $name")
                }
            } ?: appendLog("✗ 查询返回 null，请检查 Provider 声明与 Authority 配置")
        } catch (e: Exception) {
            appendLog("✗ 查询异常: ${e.message}")
        }
    }

    /**
     * 4. 插入新数据
     */
    private fun insertProviderData() {
        appendLog("── 执行 ContentResolver.insert ──")
        try {
            val values = ContentValues().apply {
                put("item_name", "动态添加项 #${System.currentTimeMillis() % 10000}")
            }
            val uri = contentResolver.insert(AutoInitProvider.CONTENT_URI, values)
            appendLog("✓ 插入成功，返回 Uri: $uri")
            appendLog("提示：可再次点击「查询 Provider 共享数据」查看更新结果")
        } catch (e: Exception) {
            appendLog("✗ 插入异常: ${e.message}")
        }
    }

    /**
     * 5. 性能反思与 App Startup 演进对比
     */
    private fun showArchitectureEvolution() {
        appendLog(
            """
            ── ContentProvider 自动初始化的性能利弊 ──
            【优势】：
            1. 零配置接入（Zero-config）：第三方库引入即用，无需业务调用 init()；
            2. 无需侵入 Application.onCreate()。

            【性能与工程缺陷】：
            1. 拖慢冷启动：每个 Provider 在系统框架层都需要反射实例化与 Binder 注册，多个第三方库各自带一个 Provider，冷启动白屏时间成倍增加；
            2. 执行顺序不可控：Android 无法保证不同 AAR 间 ContentProvider 的执行先后顺序，有依赖关系的 SDK 难以协调；
            3. 无法延迟加载：所有 Provider 都在应用启动时无条件立即执行，无法按需懒加载。

            【现代治理方案】：
            Jetpack 官方推出 App Startup，使用一个唯一的 InitializationProvider 统一托管所有 SDK 初始化，通过 DAG 拓扑排序解决依赖顺序并支持按需懒加载（详见下方 App Startup 示例）。
            """.trimIndent()
        )
    }
}
