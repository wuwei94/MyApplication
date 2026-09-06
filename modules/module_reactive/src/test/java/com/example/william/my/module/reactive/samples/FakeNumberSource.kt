package com.example.william.my.module.reactive.samples

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * [NumberSource] 的手写测试替身（Fake）。
 *
 * 与 Mock 框架的区别：这是一个真实可用的内存实现，用例按需要预置发射序列与失败时机，
 * 并主动记录被订阅次数供断言，不需要任何 `verify { }` 桩语句，也不会因为签名变化而静默失效。
 * 项目不引入 MockK / Mockito，所有测试替身都按这个模式手写。
 *
 * @param values 正常订阅时依次发射的序列
 * @param failureCount 前 [failureCount] 次订阅在发射任何数据前直接失败，用于验证 retry 的重新订阅
 * @param failAfterEmitting 成功订阅后在发射指定项数后失败，用于验证 catch 的中途降级；为 null 时正常结束
 * @param emitDelayMillis 每项发射前的间隔，配合 `runTest` 虚拟时间可精确控制时序
 */
class FakeNumberSource(
    private val values: List<Int> = emptyList(),
    private val failureCount: Int = 0,
    private val failAfterEmitting: Int? = null,
    private val emitDelayMillis: Long = 0L,
) : NumberSource {

    /** 上游被订阅的总次数，retry 会重新订阅，可据此断言重试次数 */
    var subscriptionCount = 0
        private set

    override fun numbers(): Flow<Int> = flow {
        val attempt = ++subscriptionCount
        if (attempt <= failureCount) throw IllegalStateException(FAKE_ERROR_MESSAGE)
        values.forEachIndexed { index, value ->
            if (failAfterEmitting != null && index == failAfterEmitting) {
                throw IllegalStateException(FAKE_ERROR_MESSAGE)
            }
            if (emitDelayMillis > 0) delay(emitDelayMillis)
            emit(value)
        }
        if (failAfterEmitting != null && failAfterEmitting >= values.size) {
            throw IllegalStateException(FAKE_ERROR_MESSAGE)
        }
    }

    companion object {
        const val FAKE_ERROR_MESSAGE = "Fake 上游异常"
    }
}
