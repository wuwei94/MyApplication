package com.example.william.my.module.reactive.samples

import app.cash.turbine.test
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * RxJava 3 操作符示例的单测。
 *
 * 通过 `kotlinx-coroutines-rx3` 的 [asFlow] 把 Observable 转成 Flow，
 * 与 [FlowOperatorSamplesTest] 共用 Turbine 一套断言风格，便于两个框架横向对照。
 * 同样不引入任何 Mock 框架，输入端直接用 `Observable.just / range / concat` 字面量。
 */
class RxJavaOperatorSamplesTest {

    @Test
    fun createObservable_range1To3_emitsAllThenCompletes() = runTest {
        createObservable().asFlow().test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun mapObservable_lowercaseInput_emitsUppercase() = runTest {
        mapObservable(Observable.just("apple", "banana")).asFlow().test {
            assertEquals("APPLE", awaitItem())
            assertEquals("BANANA", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun flatMapObservable_nestedObservables_emitsExpandedValues() = runTest {
        flatMapObservable(Observable.just(1, 2)).asFlow().test {
            assertEquals(1, awaitItem())
            assertEquals(10, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(20, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun bufferObservable_chunkSizeTwo_emitsPackedLists() = runTest {
        bufferObservable(Observable.just(1, 2, 3)).asFlow().test {
            assertEquals(listOf(1, 2), awaitItem())
            assertEquals(listOf(3), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun filterObservable_duplicates_emitsFirstThreeAfterDistinct() = runTest {
        // 1,2,2,3,4,1,5 → 全局去重 1,2,3,4,5 → 过滤 >1 得 2,3,4,5 → take(3) 得 2,3,4
        filterObservable(Observable.just(1, 2, 2, 3, 4, 1, 5)).asFlow().test {
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            assertEquals(4, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun zipObservable_equalLengthSources_pairsByIndex() = runTest {
        zipObservable(Observable.just("Android", "Kotlin"), Observable.just("14", "2.0"))
            .asFlow()
            .test {
                assertEquals("Android @ 14", awaitItem())
                assertEquals("Kotlin @ 2.0", awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun concatObservable_twoSources_emitsFirstThenSecond() = runTest {
        concatObservable(Observable.just("A", "B"), Observable.just("C", "D"))
            .asFlow()
            .test {
                assertEquals("A", awaitItem())
                assertEquals("B", awaitItem())
                assertEquals("C", awaitItem())
                assertEquals("D", awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun onErrorReturnObservable_upstreamFailure_emitsValueThenFallback() = runTest {
        onErrorReturnObservable(flakyObservable(), FALLBACK_NUMBER_RX).asFlow().test {
            assertEquals(1, awaitItem())
            assertEquals(FALLBACK_NUMBER_RX, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun retryObservable_permanentFailure_resubscribesUntilAttemptsExhausted() = runTest {
        retryObservable(flakyObservable(), maxAttempts = 2).asFlow().test {
            assertEquals(1, awaitItem())
            assertEquals(1, awaitItem())
            assertEquals(1, awaitItem())
            assertEquals(FLAKY_ERROR_MESSAGE, awaitError().message)
        }
    }

    /** 每次订阅都先发射 1 再 onError，模拟持续失败的上游 */
    private fun flakyObservable(): Observable<Int> = Observable.concat(
        Observable.just(1),
        Observable.error<Int>(IllegalStateException(FLAKY_ERROR_MESSAGE)),
    )

    private companion object {
        const val FLAKY_ERROR_MESSAGE = "上游持续失败"
    }
}
