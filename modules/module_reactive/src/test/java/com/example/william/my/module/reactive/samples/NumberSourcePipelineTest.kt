package com.example.william.my.module.reactive.samples

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 错误恢复与重试类操作符的单测：用 [FakeNumberSource] 驱动上游的失败时机。
 *
 * 这类操作符依赖上游的**订阅语义**（冷流可重复订阅），用字面量 Flow 无法覆盖失败场景，
 * 因此统一走手写 Fake：既能注入异常，也能通过 [FakeNumberSource.subscriptionCount] 断言重试次数。
 */
class NumberSourcePipelineTest {

    @Test
    fun catchFlow_upstreamFailsMidway_emitsValueThenFallback() = runTest {
        val source = FakeNumberSource(values = listOf(1, 2), failAfterEmitting = 1)

        catchFlow(source).test {
            assertEquals(1, awaitItem())
            assertEquals(FALLBACK_NUMBER, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun catchFlow_emptyUpstream_completesWithoutEmitting() = runTest {
        catchFlow(FakeNumberSource()).test {
            awaitComplete()
        }
    }

    @Test
    fun catchFlow_customFallback_emitsGivenFallback() = runTest {
        val source = FakeNumberSource(values = listOf(1), failAfterEmitting = 1)

        catchFlow(source, fallback = 0).test {
            assertEquals(1, awaitItem())
            assertEquals(0, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun retryFlow_firstTwoSubscriptionsFail_emitsValuesOnThirdSubscription() = runTest {
        val source = FakeNumberSource(values = listOf(7), failureCount = 2)

        retryFlow(source, maxAttempts = 2).test {
            assertEquals(7, awaitItem())
            awaitComplete()
        }
        assertEquals(3, source.subscriptionCount)
    }

    @Test
    fun retryFlow_allSubscriptionsFail_errorsAfterAttemptsExhausted() = runTest {
        val source = FakeNumberSource(values = listOf(1), failAfterEmitting = 0)

        retryFlow(source, maxAttempts = 2).test {
            assertEquals(FakeNumberSource.FAKE_ERROR_MESSAGE, awaitError().message)
        }
        assertEquals(3, source.subscriptionCount)
    }
}
