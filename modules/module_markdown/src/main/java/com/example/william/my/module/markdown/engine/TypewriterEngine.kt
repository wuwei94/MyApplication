package com.example.william.my.module.markdown.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/**
 * 动态自适应打字机流控引擎 (TypewriterEngine)
 *
 * 核心机制：
 * 1. 动态时钟与缓冲区自适应调速（Fluid Adaptive Rate）：
 *    - 当网络突发一大段文字（缓冲区积压 > 50 字符）时，自动提速（8ms/字 或一次出 2~3 字）；
 *    - 当缓冲区平缓（10~50 字符）时，中速推进（15~25ms/字）；
 *    - 当缓冲区空闲（< 10 字符）时，平缓呼吸式出字（35~45ms/字）；
 * 2. 标点呼吸停顿节奏：
 *    - 遇到中文/英文句号、感叹号、问号、逗号或换行符时，增加 30~50ms 轻微停顿，模拟真实大模型思考呼吸感；
 * 3. 完善的生命周期控制：
 *    - 支持即时 feed 追加、complete 结束通知、pause 暂停、resume 恢复、skipToFinish 一键秒出全部以及 reset 重置。
 */
class TypewriterEngine {

    /**
     * 打字机状态
     *
     * 打字机引擎的运行状态。
     */
    enum class State {
        IDLE,
        TYPING,
        PAUSED,
        COMPLETED,
    }

    private val mLock = Any()
    private val mPendingBuffer = StringBuilder()
    private val mOutputBuffer = StringBuilder()

    private var mIsFeedCompleted = false
    private var mState = State.IDLE
    private var mTypingJob: Job? = null

    private var mOnTextUpdateListener: ((text: String, isFinished: Boolean) -> Unit)? = null
    private var mOnMetricsListener: ((backlog: Int, speedMs: Long, state: State) -> Unit)? = null

    /**
     * 设置文本更新监听
     */
    fun setOnTextUpdateListener(listener: (text: String, isFinished: Boolean) -> Unit) {
        mOnTextUpdateListener = listener
    }

    /**
     * 设置流控性能指标监听（积压量、当前速度、引擎状态）
     */
    fun setOnMetricsListener(listener: (backlog: Int, speedMs: Long, state: State) -> Unit) {
        mOnMetricsListener = listener
    }

    /**
     * 启动打字机调度协程
     */
    fun start(scope: CoroutineScope) {
        reset()
        mState = State.TYPING
        mTypingJob = scope.launch(Dispatchers.Default) {
            runLoop()
        }
    }

    /**
     * 接收网络推流 chunk
     */
    fun feed(chunk: String) {
        if (chunk.isEmpty()) return
        synchronized(mLock) {
            mPendingBuffer.append(chunk)
            if (mState == State.IDLE) {
                mState = State.TYPING
            }
        }
    }

    /**
     * 标记网络推流已全部到达
     */
    fun complete() {
        synchronized(mLock) {
            mIsFeedCompleted = true
        }
    }

    /**
     * 暂停打字
     */
    fun pause() {
        synchronized(mLock) {
            if (mState == State.TYPING) {
                mState = State.PAUSED
            }
        }
    }

    /**
     * 恢复打字
     */
    fun resume() {
        synchronized(mLock) {
            if (mState == State.PAUSED) {
                mState = State.TYPING
            }
        }
    }

    /**
     * 一键跳过打字过程，立即将所有积压文字全部显示并标记完成
     */
    fun skipToFinish() {
        val fullText: String
        synchronized(mLock) {
            mOutputBuffer.append(mPendingBuffer)
            mPendingBuffer.clear()
            mIsFeedCompleted = true
            mState = State.COMPLETED
            fullText = mOutputBuffer.toString()
        }
        mTypingJob?.cancel()
        notifyText(fullText, isFinished = true)
        notifyMetrics(0, 0, State.COMPLETED)
    }

    /**
     * 重置清空打字机引擎状态
     */
    fun reset() {
        mTypingJob?.cancel()
        mTypingJob = null
        synchronized(mLock) {
            mPendingBuffer.clear()
            mOutputBuffer.clear()
            mIsFeedCompleted = false
            mState = State.IDLE
        }
        notifyMetrics(0, 0, State.IDLE)
    }

    /**
     * 核心调度循环
     */
    private suspend fun runLoop() {
        while (coroutineContext.isActive) {
            var textToEmit: String? = null
            var isFinished = false
            var currentDelayMs: Long = 30
            var currentBacklog = 0
            var currentState: State

            synchronized(mLock) {
                currentState = mState
                currentBacklog = mPendingBuffer.length

                if (currentState == State.PAUSED) {
                    // 暂停状态，静默等待
                    currentDelayMs = 50
                } else if (mPendingBuffer.isEmpty()) {
                    if (mIsFeedCompleted) {
                        mState = State.COMPLETED
                        currentState = State.COMPLETED
                        isFinished = true
                        textToEmit = mOutputBuffer.toString()
                    } else {
                        currentDelayMs = 20
                    }
                } else {
                    // 自适应出字算法（轻快自然：约 33~45 字/秒，节奏明快利落且清晰可见）
                    val step: Int
                    when {
                        currentBacklog > 80 -> {
                            step = 2
                            currentDelayMs = 20 // 积压追赶模式：约 100 字/秒
                        }
                        currentBacklog > 30 -> {
                            step = 1
                            currentDelayMs = 22 // 适度加速：约 45 字/秒
                        }
                        else -> {
                            step = 1
                            currentDelayMs = 30 // 基础轻快出字：约 33 字/秒（流畅自然，不拖沓）
                        }
                    }

                    // 取出 step 个字符
                    val actualStep = minOf(step, mPendingBuffer.length)
                    val chunk = mPendingBuffer.substring(0, actualStep)
                    mPendingBuffer.delete(0, actualStep)
                    mOutputBuffer.append(chunk)

                    // 检查最后一个字符是否是标点符号，根据标点级别增加富有层次的呼吸停顿
                    val lastChar = chunk.lastOrNull()
                    if (lastChar != null) {
                        val pauseMs = getPunctuationPause(lastChar)
                        if (pauseMs > 0) {
                            currentDelayMs = if (currentBacklog > 50) {
                                currentDelayMs + pauseMs / 3
                            } else {
                                pauseMs
                            }
                        }
                    }

                    textToEmit = mOutputBuffer.toString()
                }
            }

            // 派发回调
            if (textToEmit != null) {
                notifyText(textToEmit, isFinished)
            }
            notifyMetrics(currentBacklog, currentDelayMs, currentState)

            if (isFinished) {
                break
            }

            delay(currentDelayMs)
        }
    }

    /**
     * 根据标点符号级别计算呼吸停顿毫秒数：
     * - 句末重标点（句号、感叹号、问号、换行）：停顿 160ms（轻快思考停顿）
     * - 句中轻标点（逗号、顿号、分号、冒号）：停顿 80ms（自然换气停顿）
     */
    private fun getPunctuationPause(c: Char): Long = when (c) {
        '。', '！', '？', '.', '!', '?', '\n' -> 160L
        '，', '、', '；', '：', ',', ';', ':' -> 80L
        else -> 0L
    }

    private fun notifyText(text: String, isFinished: Boolean) {
        mOnTextUpdateListener?.invoke(text, isFinished)
    }

    private fun notifyMetrics(backlog: Int, speedMs: Long, state: State) {
        mOnMetricsListener?.invoke(backlog, speedMs, state)
    }
}
