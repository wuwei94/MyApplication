package com.example.william.my.core.base.arch.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * Flow 类型的协程 UseCase 基类
 *
 * 与 RxJava 版（[com.example.william.my.core.base.arch.rx.SingleObserverUseCase]）的职责差异：
 * RxJava 版通过 execute(observer) 回调订阅并需手动 clear() 释放 Disposable；
 * Flow 版返回冷流，由收集方（如 viewModelScope）控制收集与取消，作用域终止即自动停止，无需手工释放；
 * 参数经 invoke 操作符随调用传入，避免可变状态在多次调用间残留。
 */
abstract class FlowUseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    protected abstract fun buildUseCaseFlow(parameters: P): Flow<R>

    operator fun invoke(parameters: P): Flow<R> = buildUseCaseFlow(parameters).flowOn(coroutineDispatcher)
}
