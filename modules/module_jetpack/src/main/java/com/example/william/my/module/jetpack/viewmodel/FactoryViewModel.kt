package com.example.william.my.module.jetpack.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository

/**
 * FactoryViewModel — ViewModel 自定义工厂构建演示
 *
 * 演示 Lifecycle 2.5.0+ 引入的 [CreationExtras] 机制与有参 ViewModel 的现代工厂创建方式：
 *
 * 核心特性：
 * 1. 无状态工厂：通过 [CreationExtras] 容器按需获取上下文参数，Factory 本身无需持有状态或构造传参，可直接作为单例；
 * 2. 状态恢复集成：通过 `extras.createSavedStateHandle()` 直接创建 [SavedStateHandle]，彻底取代旧的 `AbstractSavedStateViewModelFactory`；
 * 3. DSL 构建：支持官方推荐的 [viewModelFactory] 与 [initializer] 声明式 DSL 构建语法。
 */
class FactoryViewModel(
    private val repository: ArticleRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        /**
         * 方式一：实现 [ViewModelProvider.Factory] 接口并重写带 [CreationExtras] 的 `create` 方法
         *
         * [CreationExtras] 包含了框架自动注入的上下文参数（如 APPLICATION_KEY、DEFAULT_ARGS_KEY 等）。
         */
        val StandardFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // 1. 从 CreationExtras 中提取系统级依赖 Application
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                // 2. 利用扩展函数直接从 CreationExtras 创建绑定当前生命周期的 SavedStateHandle
                val savedStateHandle = extras.createSavedStateHandle()

                // 3. 通过服务定位器获取数据仓库
                val repository = ServiceLocator.provideArticleRepository(application)
                return FactoryViewModel(
                    repository = repository,
                    savedStateHandle = savedStateHandle
                ) as T
            }
        }

        /**
         * 方式二：使用官方推荐的 [viewModelFactory] DSL 扩展与 [initializer] 构建
         *
         * [initializer] 代码块内部的 `this` 即为 [CreationExtras]，可直接调用其扩展函数。
         */
        val DslFactory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // 1. 直接通过 this[APPLICATION_KEY] 获取 Application
                val application =
                    checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                // 2. 直接调用 createSavedStateHandle() 扩展函数创建 SavedStateHandle
                val savedStateHandle = createSavedStateHandle()

                // 3. 构造 ViewModel 实例并返回
                val repository = ServiceLocator.provideArticleRepository(application)
                FactoryViewModel(
                    repository = repository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}
