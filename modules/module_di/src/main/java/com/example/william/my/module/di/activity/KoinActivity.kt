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
 * Koin — Kotlin 专用的实用主义依赖注入框架
 *
 * 核心特性：
 * 1. 纯 Kotlin DSL：无注解处理器（无 KAPT / KSP 开销），极速编译
 * 2. 构造器 DSL（Constructor DSL）：通过 singleOf、factoryOf、viewModelOf 极简声明
 * 3. 灵活的作用域（Scope）与动态参数（parametersOf）：支持运行时按需传参
 * 4. 跨平台（KMP）：原生支持 Android、iOS、Desktop、Web 与 Ktor 服务端
 *
 * 常用 DSL：
 * - startKoin { ... }：启动 Koin 容器并配置 Context 与 Modules
 * - single / singleOf：声明单例（Singleton）实例
 * - factory / factoryOf：声明工厂（Factory）实例，每次请求生成新对象
 * - viewModel / viewModelOf：声明与 Android Lifecycle / ViewModelStore 绑定的 ViewModel
 * - bind：将实现类绑定到特定接口
 * - named(...) / qualifier：具名限定符，区分同一接口的多实现
 * - by inject()：属性懒加载注入
 * - get()：即时获取依赖对象
 * - parametersOf(...)：在解析时传递动态运行时参数
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

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 基础注入 (singleOf 单例 vs factoryOf 工厂)",
            "2. 具名限定符与接口绑定 (named AliPay / WeChatPay)",
            "3. 动态运行时参数注入 (parametersOf)",
            "4. Koin ViewModel 注入 (viewModelOf by viewModel)",
            "5. Scope 局部作用域创建与生命周期销毁",
            "6. 全量依赖与 Koin 容器状态验证"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                // 验证单例 vs 工厂
                val tracker2: KoinAnalyticsTracker = get()
                val orderProcessor2: KoinOrderProcessor = get()
                appendLog("【1. 单例 singleOf】")
                appendLog("   • 实例 1 hash=${analyticsTracker.hashCode()}, info=${analyticsTracker.logEvent("click_btn")}")
                appendLog("   • 实例 2 hash=${tracker2.hashCode()} (相同实例 = ${analyticsTracker === tracker2})")
                appendLog("【1. 工厂 factoryOf】")
                appendLog("   • 实例 1 hash=${orderProcessor.hashCode()}, info=${orderProcessor.processOrder(101)}")
                appendLog("   • 实例 2 hash=${orderProcessor2.hashCode()} (不同实例 = ${orderProcessor !== orderProcessor2})")
            }
            1 -> {
                appendLog("【2. 具名限定符注入】")
                appendLog("   • AliPay:   ${aliPayService.pay(88.0)}")
                appendLog("   • WeChatPay:${weChatPayService.pay(66.0)}")
            }
            2 -> {
                // 运行时传入动态参数
                val userSession: KoinUserProfileSession = get { parametersOf("VIP_User_9527") }
                appendLog("【3. 动态传参注入】${userSession.getSessionDetails()}")
            }
            3 -> {
                val count = koinSampleViewModel.incrementAndGet()
                appendLog("【4. Koin ViewModel】当前计数 = $count (${koinSampleViewModel.getViewModelInfo()})")
            }
            4 -> {
                // 创建自定义 Scope
                val scopeId = "custom_scope_${System.currentTimeMillis()}"
                val customScope = getKoin().createScope(scopeId, named("CustomSessionScope"))
                val scopedSession: KoinScopedSession = customScope.get()
                appendLog("【5. Scope 作用域】创建 Scope [id=$scopeId]")
                appendLog("   • 获取 Scoped 实例: hash=${scopedSession.hashCode()}, msg=${scopedSession.info}")
                customScope.close()
                appendLog("   • Scope 已关闭并销毁所持实例")
            }
            5 -> {
                appendLog("【6. Koin 容器状态】")
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
