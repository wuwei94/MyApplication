package com.example.william.my.module.di.koin.module

import com.example.william.my.module.di.koin.model.KoinAliPayServiceImpl
import com.example.william.my.module.di.koin.model.KoinAnalyticsTracker
import com.example.william.my.module.di.koin.model.KoinOrderProcessor
import com.example.william.my.module.di.koin.model.KoinPaymentService
import com.example.william.my.module.di.koin.model.KoinScopedSession
import com.example.william.my.module.di.koin.model.KoinUserProfileSession
import com.example.william.my.module.di.koin.model.KoinWeChatPayServiceImpl
import com.example.william.my.module.di.koin.viewmodel.KoinSampleViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin 示例模块配置
 */
val koinSampleModule = module {

    // 1. 单例与工厂 Constructor DSL
    singleOf(::KoinAnalyticsTracker)
    factoryOf(::KoinOrderProcessor)

    // 2. 接口绑定与具名限定符
    single<KoinPaymentService>(named("AliPay")) { KoinAliPayServiceImpl() }
    single<KoinPaymentService>(named("WeChatPay")) { KoinWeChatPayServiceImpl() }

    // 3. 动态参数注入
    factory { (userId: String) -> KoinUserProfileSession(userId, get()) }

    // 4. ViewModel 注入
    viewModelOf(::KoinSampleViewModel)

    // 5. 自定义 Scope 声明
    scope(named("CustomSessionScope")) {
        scoped { KoinScopedSession() }
    }
}
