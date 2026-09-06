package com.example.william.my.module.reactive.samples

import app.cash.turbine.test
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Kotlin Flow 操作符示例的单测：用 Turbine 逐项断言发射序列与终止事件。
 *
 * 约定：
 * - 一个测试只锁定一条操作符语义，方法名 `被测对象_输入或场景_预期结果`；
 * - 必须消费完整事件流（awaitComplete / awaitError），否则 Turbine 会报 Unconsumed events；
 * - 上游用 `flowOf` 字面量或 [FakeNumberSource]，不引入任何 Mock 框架。
 */
class FlowOperatorSamplesTest {

    @Test
    fun createFlow_range1To3_emitsAllThenCompletes() = runTest {
        createFlow().test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun mapFlow_lowercaseInput_emitsUppercase() = runTest {
        mapFlow(flowOf("apple", "banana")).test {
            assertEquals("APPLE", awaitItem())
            assertEquals("BANANA", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun flatMapConcatFlow_nestedFlows_emitsExpandedValuesInOrder() = runTest {
        flatMapConcatFlow(flowOf(1, 2)).test {
            assertEquals(1, awaitItem())
            assertEquals(10, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(20, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun bufferFlow_threeValues_emitsSameValues() = runTest {
        bufferFlow(flowOf(1, 2, 3)).test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun filterFlow_duplicatesAndSmallValues_emitsFirstThreeAfterDistinct() = runTest {
        // 1,2,2,3,4,1,5 → 相邻去重 1,2,3,4,1,5 → 过滤 >1 得 2,3,4,5 → take(3) 得 2,3,4
        filterFlow(flowOf(1, 2, 2, 3, 4, 1, 5)).test {
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            assertEquals(4, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun zipFlow_equalLengthSources_pairsByIndex() = runTest {
        zipFlow(flowOf("Android", "Kotlin"), flowOf("14", "2.0")).test {
            assertEquals("Android @ 14", awaitItem())
            assertEquals("Kotlin @ 2.0", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun zipFlow_longerFirstSource_dropsUnpairedRemainder() = runTest {
        zipFlow(flowOf("A", "B", "C"), flowOf("1")).test {
            assertEquals("A @ 1", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun combineFlow_interleavedSources_emitsOnEveryLatestPair() = runTest {
        val numbers = numbersWithDelay()
        val letters = lettersWithDelay()

        combineFlow(numbers, letters).test {
            assertEquals("1a", awaitItem())
            assertEquals("2a", awaitItem())
            assertEquals("2b", awaitItem())
            assertEquals("3b", awaitItem())
            awaitComplete()
        }
    }

    /**
     * 两个流的发射时序：numbers 在 0/50/100ms 发射，letters 在 25/75ms 发射。
     * 显式错开是为了让 combine 的「任一方更新即用双方最新值组合」语义可被稳定断言。
     */
    private fun numbersWithDelay() = flow {
        emit(1)
        kotlinx.coroutines.delay(50)
        emit(2)
        kotlinx.coroutines.delay(50)
        emit(3)
    }

    private fun lettersWithDelay() = flow {
        kotlinx.coroutines.delay(25)
        emit("a")
        kotlinx.coroutines.delay(50)
        emit("b")
    }
}
