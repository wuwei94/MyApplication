package com.example.william.my.application.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.navigator.PageInterceptor
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

/**
 * 全局页面路由与拦截器多重绑定声明
 *
 * 允许各业务模块通过 `@IntoMap @StringKey(RouterPath.Xxx)` 与 `@IntoSet`
 * 在编译期自动向全局注册页面与拦截器，无需依赖第三方字节码插件。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NavigatorModule {

    @Multibinds
    abstract fun bindPageRoutes(): Map<String, @JvmSuppressWildcards Class<out Activity>>

    @Multibinds
    abstract fun bindPageInterceptors(): Set<PageInterceptor>
}
