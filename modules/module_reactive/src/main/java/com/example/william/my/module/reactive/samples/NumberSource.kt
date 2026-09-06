package com.example.william.my.module.reactive.samples

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/**
 * 数字数据源：描述「上游数据从哪来」，与「操作符如何变换」解耦。
 *
 * 错误恢复与重试类操作符强依赖上游的**订阅语义**——冷流每次订阅都会重新执行，
 * 失败后可以被重新订阅。因此这两类示例以数据源为入参，而不是接收一个已经展开的 Flow：
 * - 演示页传入固定序列或易失败序列；
 * - 单元测试传入手写 Fake，注入异常并统计订阅次数，无需引入 Mock 框架。
 */
fun interface NumberSource {
    /** 每次订阅都会重新执行，返回本次订阅的数据流 */
    fun numbers(): Flow<Int>
}

/**
 * 固定序列数据源：演示页默认使用的实现。
 *
 * @param values 每次订阅都会重新发射的完整序列
 */
class StaticNumberSource(private val values: List<Int>) : NumberSource {
    override fun numbers(): Flow<Int> = values.asFlow()
}
