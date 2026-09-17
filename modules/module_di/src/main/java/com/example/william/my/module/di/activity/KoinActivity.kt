package com.example.william.my.module.di.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.di.koin.model.KoinAnalyticsTracker
import com.example.william.my.module.di.koin.model.KoinOrderProcessor
import com.example.william.my.module.di.koin.model.KoinPaymentService
import com.example.william.my.module.di.koin.model.KoinScopedSession
import com.example.william.my.module.di.koin.model.KoinUserProfileSession
import com.example.william.my.module.di.koin.module.koinSampleModule
import com.example.william.my.module.di.koin.viewmodel.KoinSampleViewModel
import org.koin.android.ext.android.get
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

/**
 * Koin — Kotlin 优先的运行时依赖注入框架
 *
 * 核心机制与避坑点：
 * 1. 纯 Kotlin DSL：无注解处理器，依赖在运行时按图解析，缺少编译期缺失依赖检查，需靠启动自检兜底
 * 2. 容器启动：Application 层 startKoin { androidContext(...) } 一次；页面/测试场景用 loadKoinModules 热加载示例模块，避免重复 startKoin
 * 3. 实例模型：singleOf 全局复用同一实例，factoryOf 每次 get() 新建；viewModelOf 绑定 ViewModelStore 生命周期
 * 4. 限定与作用域：named(...) 区分同接口多实现；parametersOf 在 get() 时传入运行时参数；自定义 Scope 需手动 close() 释放所持实例
 *
 * 官方参考：
 * https://insert-koin.io/
 */
@Route(path = RouterPath.Di.Koin)
class KoinActivity : BasicResponseActivity() {

    // 1. 懒加载注入单例与工厂对象
    private val analyticsTracker: KoinAnalyticsTracker by inject()
    private val orderProcessor: KoinOrderProcessor by inject()

    // 2. 具名限定符注入不同实现
    private val aliPayService: KoinPaymentService by inject(qualifier = named("AliPay"))
    private val weChatPayService: KoinPaymentService by inject(qualifier = named("WeChatPay"))

    // 3. ViewModel 注入
    private val koinSampleViewModel: KoinSampleViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        // 确保 Koin 容器已初始化并加载示例模块
        ensureKoinInitialized()
        super.onCreate(savedInstanceState)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("Koin 依赖注入实战\n\n演示 Kotlin DSL 声明、singleOf/factoryOf、接口绑定、具名限定符、动态传参、ViewModel及Scope")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 基础注入 (singleOf 单例 vs factoryOf 工厂)",
        "2. 具名限定符与接口绑定 (named AliPay / WeChatPay)",
        "3. 动态运行时参数注入 (parametersOf)",
        "4. Koin ViewModel 注入 (viewModelOf by viewModel)",
        "5. Scope 局部作用域创建与生命周期销毁",
        "6. 全量依赖与 Koin 容器状态验证",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                // 验证单例 vs 工厂
                val tracker2: KoinAnalyticsTracker = get()
                val orderProcessor2: KoinOrderProcessor = get()
                appendLog("[1. 单例 singleOf]")
                appendLog("   • 实例 1 hash=${analyticsTracker.hashCode()}, info=${analyticsTracker.logEvent("click_btn")}")
                appendLog("   • 实例 2 hash=${tracker2.hashCode()} (相同实例 = ${analyticsTracker === tracker2})")
                appendLog("[1. 工厂 factoryOf]")
                appendLog("   • 实例 1 hash=${orderProcessor.hashCode()}, info=${orderProcessor.processOrder(101)}")
                appendLog("   • 实例 2 hash=${orderProcessor2.hashCode()} (不同实例 = ${orderProcessor !== orderProcessor2})")
            }
            1 -> {
                appendLog("[2. 具名限定符注入]")
                appendLog("   • AliPay:   ${aliPayService.pay(88.0)}")
                appendLog("   • WeChatPay:${weChatPayService.pay(66.0)}")
            }
            2 -> {
                // 运行时传入动态参数
                val userSession: KoinUserProfileSession = get { parametersOf("VIP_User_9527") }
                appendLog("[3. 动态传参注入] ${userSession.getSessionDetails()}")
            }
            3 -> {
                val count = koinSampleViewModel.incrementAndGet()
                appendLog("[4. Koin ViewModel] 当前计数 = $count (${koinSampleViewModel.getViewModelInfo()})")
            }
            4 -> {
                // 创建自定义 Scope
                val scopeId = "custom_scope_${System.currentTimeMillis()}"
                val customScope = getKoin().createScope(scopeId, named("CustomSessionScope"))
                val scopedSession: KoinScopedSession = customScope.get()
                appendLog("[5. Scope 作用域] 创建 Scope [id=$scopeId]")
                appendLog("   • 获取 Scoped 实例: hash=${scopedSession.hashCode()}, msg=${scopedSession.info}")
                customScope.close()
                appendLog("   • Scope 已关闭并销毁所持实例")
            }
            5 -> {
                appendLog("[6. Koin 容器状态]")
                appendLog("   • Koin 实例: ${getKoin()}")
                appendLog("   • Tracker: ${analyticsTracker.logEvent("verify_all")}")
                appendLog("   • ViewModel: ${koinSampleViewModel.getViewModelInfo()}")
            }
        }
    }

    private fun ensureKoinInitialized() {
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidLogger(Level.ERROR)
                androidContext(applicationContext)
                modules(koinSampleModule)
            }
        } else {
            loadKoinModules(koinSampleModule)
        }
    }
}
