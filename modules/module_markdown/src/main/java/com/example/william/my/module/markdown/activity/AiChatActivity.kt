package com.example.william.my.module.markdown.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.markdown.chat.adapter.ChatAdapter
import com.example.william.my.module.markdown.chat.model.ChatMessage
import com.example.william.my.module.markdown.databinding.MarkdownActivityChatBinding
import com.example.william.my.module.markdown.engine.TypewriterEngine
import com.example.william.my.module.markdown.grammar.MyGrammarLocator
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * AI 流式对话完整实战界面 (AiChatActivity)
 *
 * 核心技术组合：
 * 1. RecyclerView Payload 局部增量刷新：流式出字仅更新当前 AI 气泡的 TextView，完全避免 ViewHolder 重新绘制与闪烁；
 * 2. 智能吸底平滑滚动：用户手势上滑查看历史时自动暂停吸底，并展示“回到最新内容”悬浮按钮；
 * 3. 打字机动态流控 (TypewriterEngine) + 未闭合语法容错 (MarkdownStreamFixer)；
 * 4. Prism4j 多语言代码语法着色 + Markdown 富文本排版；
 * 5. 全生命周期管理：支持「发送」与「停止生成」即时切换、消息一键全文复制。
 */
@Route(path = RouterPath.Markdown.AiChat)
class AiChatActivity : BaseVBActivity<MarkdownActivityChatBinding>() {

    private lateinit var mMarkwon: Markwon
    private lateinit var mAdapter: ChatAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    private val mEngine = TypewriterEngine()
    private var mMockStreamJob: Job? = null
    private var mCurrentAiMessageIndex = -1
    private var mIsGenerating = false
    private var mAutoScrollEnabled = true

    override fun getViewBinding(): MarkdownActivityChatBinding = MarkdownActivityChatBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initHeader()
        initMarkwon()
        initRecyclerView()
        initListeners()
        initEngine()

        // 默认载入一条欢迎消息
        showWelcomeMessage()
    }

    private fun initHeader() {
        mBinding.btnClearChat.setOnClickListener {
            if (mIsGenerating) {
                stopGeneration()
            }
            mAdapter.setMessages(emptyList())
            showWelcomeMessage()
            Toast.makeText(this, "对话已清空", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initMarkwon() {
        val prism4j = Prism4j(MyGrammarLocator())
        val darkulaTheme = Prism4jThemeDarkula.create()

        val tableTheme = TableTheme.Builder()
            .tableBorderWidth(dpToPx(1))
            .tableBorderColor(0x33888888)
            .tableCellPadding(dpToPx(8))
            .tableHeaderRowBackgroundColor(0x18888888)
            .tableEvenRowBackgroundColor(0x08888888)
            .build()

        val tableWidthProvider: () -> Int = {
            val rv = mBinding.recyclerViewChat
            val baseWidth = if (rv.width > 0) rv.width else resources.displayMetrics.widthPixels
            // 严格对齐 markdown_item_chat_assistant.xml：外层边距(24dp) + 头像区域(40dp) + 气泡边距(18dp) + 内边距(28dp) = 110dp
            val bubbleHorizontalMargins = dpToPx(110)
            (baseWidth - bubbleHorizontalMargins).coerceAtLeast(100)
        }

        mMarkwon = Markwon.builder(this)
            .usePlugin(CorePlugin.create())
            .usePlugin(com.example.william.my.module.markdown.plugin.GfmTablePlugin.create(tableTheme, tableWidthProvider))
            .usePlugin(TaskListPlugin.create(this))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(SyntaxHighlightPlugin.create(prism4j, darkulaTheme))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .codeBlockBackgroundColor(0xFF282C34.toInt()) // 多行代码块暗黑底色
                        .codeBackgroundColor(0x14000000) // 行内代码浅色背景
                        .codeTextColor(0xFFD81B60.toInt()) // 行内代码高亮粉红色
                }
            })
            .build()
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun initRecyclerView() {
        mAdapter = ChatAdapter(mMarkwon) { content ->
            copyToClipboard(content)
        }

        mLayoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = false
        }

        mBinding.recyclerViewChat.apply {
            layoutManager = mLayoutManager
            adapter = mAdapter
            itemAnimator = null // 关闭全局动画，完全由 Payload 驱动细粒度增量刷新

            // 软键盘弹起导致 RecyclerView 布局高度变化时，如果用户处于吸底状态则自动滚动显示最新内容
            addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom && mAutoScrollEnabled) {
                    scrollBottomDelta(smooth = false)
                }
            }

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        // 仅在用户手指主动触摸拖拽时检测是否向上翻看历史，绝不被惯性/程序滚动误触
                        val canScrollDown = recyclerView.canScrollVertically(1)
                        if (canScrollDown) {
                            mAutoScrollEnabled = false
                            if (mIsGenerating) {
                                mBinding.cardScrollBottom.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val canScrollDown = recyclerView.canScrollVertically(1)
                    if (canScrollDown) {
                        // 视口不在最底部时：若处于生成中且吸底已暂停，显示悬浮按钮
                        if (mIsGenerating && !mAutoScrollEnabled) {
                            mBinding.cardScrollBottom.visibility = View.VISIBLE
                        }
                    } else {
                        // 滑动回最底部时：恢复自动吸底并隐藏悬浮按钮
                        mAutoScrollEnabled = true
                        mBinding.cardScrollBottom.visibility = View.GONE
                    }
                }
            })
        }
    }

    private fun initListeners() {
        // 发送或停止按钮
        mBinding.btnSendOrStop.setOnClickListener {
            if (mIsGenerating) {
                stopGeneration()
            } else {
                val input = mBinding.etChatInput.text.toString().trim()
                if (input.isNotEmpty()) {
                    sendMessage(input)
                    mBinding.etChatInput.setText("")
                }
            }
        }

        // 悬浮回到底部按钮
        mBinding.cardScrollBottom.setOnClickListener {
            mAutoScrollEnabled = true
            mBinding.cardScrollBottom.visibility = View.GONE
            val targetPos = mAdapter.itemCount - 1
            if (targetPos >= 0) {
                mBinding.recyclerViewChat.scrollToPosition(targetPos)
                mBinding.recyclerViewChat.post {
                    scrollBottomDelta(smooth = true)
                }
            }
        }

        // 预设快捷提示词点击
        mBinding.chipPrompt1.setOnClickListener { sendMessage("🚀 请用 Kotlin 写一段基于 SSE 的流式推流解析器") }
        mBinding.chipPrompt2.setOnClickListener { sendMessage("⚡ 如何在 Android 客户端实现 120fps 流式 Markdown 丝滑渲染？") }
        mBinding.chipPrompt3.setOnClickListener { sendMessage("📊 请给出一个 SQL 复杂多表统计与索引调优案例") }
        mBinding.chipPrompt4.setOnClickListener { sendMessage("🏛️ 详细对比 Android 依赖注入 Hilt 与 Koin 的异同与选型建议") }
    }

    private fun initEngine() {
        mEngine.setOnTextUpdateListener { text, isFinished ->
            lifecycleScope.launch(Dispatchers.Main) {
                if (mCurrentAiMessageIndex >= 0) {
                    val message = mAdapter.getMessage(mCurrentAiMessageIndex)
                    if (message != null) {
                        message.content = text
                        message.status = if (isFinished) ChatMessage.Status.COMPLETED else ChatMessage.Status.STREAMING

                        // 核心架构设计：流式出字期间直接驱动活跃 ViewHolder 更新，杜绝 RecyclerView 高频 notifyItemChanged 引起的布局全量重测与视窗抖动
                        val holder = mBinding.recyclerViewChat.findViewHolderForAdapterPosition(mCurrentAiMessageIndex) as? ChatAdapter.AssistantViewHolder
                        if (holder != null) {
                            holder.updateStreamContent(mMarkwon, message)
                            if (isFinished) {
                                holder.updateStatus(message) { copyToClipboard(message.content) }
                            }
                        } else {
                            mAdapter.updateMessage(mCurrentAiMessageIndex, message, ChatMessage.PAYLOAD_STREAM_CONTENT)
                        }

                        if (isFinished) {
                            onGenerationFinished()
                            if (mAutoScrollEnabled) {
                                mBinding.recyclerViewChat.post {
                                    scrollBottomDelta(smooth = true)
                                }
                            }
                        } else {
                            if (mAutoScrollEnabled) {
                                scrollBottomDelta(smooth = false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showWelcomeMessage() {
        val welcome = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatMessage.Role.ASSISTANT,
            content = """
                👋 **您好！我是您的 AI 流式智能助手。**

                本界面完整演示了 Android 端**极致丝滑**的大模型聊天交互：
                - 🚀 **Markwon + Prism4j**：代码块多语言离线语法高亮与 GFM 表格排版；
                - ⚡ **Payload 增量刷新**：流式出字 0 掉帧、0 闪烁；
                - 🎯 **自适应打字机**：大模型突发推流自适应提速 + 标点呼吸停顿；
                - 🛡️ **未闭合语法容错**：代码块在流式生成中语法树不崩塌。

                您可以点击下方的**快捷提示词**或直接在底部输入框提问！
            """.trimIndent(),
            status = ChatMessage.Status.COMPLETED,
        )
        mAdapter.addMessage(welcome)
    }

    private fun sendMessage(prompt: String) {
        if (mIsGenerating) return

        // 1. 创建用户消息与 AI 占位消息
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatMessage.Role.USER,
            content = prompt,
            status = ChatMessage.Status.COMPLETED,
        )
        val aiMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatMessage.Role.ASSISTANT,
            content = "",
            status = ChatMessage.Status.SENDING,
        )

        // 2. 原子性批量插入两条消息（避免分批插入导致列表高度两次跳跃与闪烁）
        mAdapter.addMessages(userMsg, aiMsg)
        mCurrentAiMessageIndex = mAdapter.itemCount - 1

        // 3. 切换状态与输入控制
        mIsGenerating = true
        mAutoScrollEnabled = true
        mBinding.cardScrollBottom.visibility = View.GONE
        mBinding.btnSendOrStop.text = "停止生成"
        mBinding.btnSendOrStop.setBackgroundColor(0xFFE53935.toInt()) // 红色警示
        mBinding.recyclerViewChat.scrollToPosition(mCurrentAiMessageIndex)

        // 4. 启动打字机并模拟推流
        mEngine.start(lifecycleScope)
        startMockAiStream(prompt)
    }

    private fun startMockAiStream(prompt: String) {
        val responseChunks = generateResponseChunks(prompt)

        mMockStreamJob = lifecycleScope.launch(Dispatchers.Default) {
            delay(260) // 模拟大模型首字 TTFT 思考耗时
            for (chunk in responseChunks) {
                // 每次推 2 个字符，间隔 40ms（约 45 字符/秒），轻快敏捷流畅
                var i = 0
                while (i < chunk.length) {
                    val tokenSize = minOf(2, chunk.length - i)
                    val token = chunk.substring(i, i + tokenSize)
                    mEngine.feed(token)
                    i += tokenSize
                    delay(40) // 模拟大模型 Token 推流网络间隔
                }
            }
            mEngine.complete()
        }
    }

    private fun stopGeneration() {
        mMockStreamJob?.cancel()
        mMockStreamJob = null
        mEngine.skipToFinish()

        if (mCurrentAiMessageIndex >= 0) {
            val message = mAdapter.getMessage(mCurrentAiMessageIndex)
            if (message != null) {
                message.status = ChatMessage.Status.FAILED
                mAdapter.updateMessage(mCurrentAiMessageIndex, message, ChatMessage.PAYLOAD_STATUS)
            }
        }
        onGenerationFinished()
        Toast.makeText(this, "已停止生成", Toast.LENGTH_SHORT).show()
    }

    private fun onGenerationFinished() {
        mIsGenerating = false
        mBinding.btnSendOrStop.text = "发送"
        mBinding.btnSendOrStop.setBackgroundColor(0xFF2196F3.toInt()) // 恢复主色
        mBinding.cardScrollBottom.visibility = View.GONE
    }

    /**
     * 物理增量绝对吸底：
     * 精准测量当前最后一条 Item 的底部溢出量 (bottomDiff)，通过相对 scrollBy 驱动视口精确下移。
     * 绝不调用 smoothScrollToPosition(targetPos)，杜绝 Android 默认 SNAP_TO_START 导致长消息回弹到卡片顶部表格的 bug。
     */
    private fun scrollBottomDelta(smooth: Boolean = false) {
        val targetPos = mAdapter.itemCount - 1
        if (targetPos < 0) return

        val lastChild = mLayoutManager.findViewByPosition(targetPos)
        if (lastChild != null) {
            val bottomDiff = lastChild.bottom + mBinding.recyclerViewChat.paddingBottom - mBinding.recyclerViewChat.height
            if (bottomDiff > 0) {
                if (smooth) {
                    mBinding.recyclerViewChat.smoothScrollBy(0, bottomDiff)
                } else {
                    mBinding.recyclerViewChat.scrollBy(0, bottomDiff)
                }
            }
        } else {
            mBinding.recyclerViewChat.scrollToPosition(targetPos)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("MarkdownChat", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }

    /**
     * 根据提问生成对应的多语言与富文本回答片段
     */
    private fun generateResponseChunks(prompt: String): List<String> {
        val trimmed = prompt.trim()
        val lower = trimmed.lowercase()

        // 1. 问候语语义识别
        if (lower in listOf("你好", "您好", "hi", "hello", "在吗", "早", "早上好", "下午好", "晚上好", "hey", "嗨") ||
            lower.startsWith("你好") || lower.startsWith("您好") || lower.startsWith("hello") || lower.startsWith("hi ")
        ) {
            return listOf(
                "👋 **您好！我是您的 AI 流式对话与技术探索助手。**\n\n",
                "很高兴为您服务！本界面支持现代大模型标准流式协议与富文本排版。\n\n",
                "您可以向我提问各种 Android 与技术问题，例如：\n",
                "- 🚀 **Kotlin & 协程**：Flow SSE 流式解析、Channel 通道与生命周期感知；\n",
                "- ⚡ **渲染与架构**：120fps 丝滑 Markdown 渲染、RecyclerView Payload 局部增量刷新；\n",
                "- 🏛️ **依赖注入**：Google Hilt vs Koin 方案深度对比；\n",
                "- 📊 **数据库调优**：SQL 复杂多表统计与索引优化；\n\n",
                "> 请随时在下方输入框提问或点击预设提示词！",
            )
        }

        // 2. 预设技术主题匹配
        if (prompt.contains("SSE", ignoreCase = true) || prompt.contains("推流") || prompt.contains("Flow", ignoreCase = true)) {
            return listOf(
                "### 基于 Kotlin Flow 与 OkHttp 的 SSE 流式解析器\n\n",
                "在 Android 客户端，推荐使用 OkHttp 的 `EventSource` 或直接基于 `ResponseBody.byteStream()` 封装为 Kotlin **冷流（Flow）**：\n\n",
                "```kotlin\n",
                "class SseStreamClient(private val okHttpClient: OkHttpClient) {\n\n",
                "    fun streamChat(prompt: String): Flow<String> = channelFlow {\n",
                "        val request = Request.Builder()\n",
                "            .url(\"https://api.deepseek.com/v1/chat/completions\")\n",
                "            .post(createJsonBody(prompt))\n",
                "            .addHeader(\"Accept\", \"text/event-stream\")\n",
                "            .build()\n\n",
                "        val response = okHttpClient.newCall(request).execute()\n",
                "        val reader = response.body?.charStream()?.buffered() ?: return@channelFlow\n\n",
                "        reader.useLines { lines ->\n",
                "            for (line in lines) {\n",
                "                if (line.startsWith(\"data: \")) {\n",
                "                    val data = line.removePrefix(\"data: \").trim()\n",
                "                    if (data == \"[DONE]\") break\n",
                "                    trySend(parseChunkText(data))\n",
                "                }\n",
                "            }\n",
                "        }\n",
                "    }.flowOn(Dispatchers.IO)\n",
                "}\n",
                "```\n\n",
                "**架构优势**：\n",
                "- 🚀 **背压支持**：Flow 自动支持协程背压与生命周期协作取消；\n",
                "- 🛡️ **内存友好**：逐行流式读取，无 OOM 风险！",
            )
        }

        if (prompt.contains("120fps", ignoreCase = true) || prompt.contains("渲染") || prompt.contains("掉帧") || prompt.contains("流畅")) {
            return listOf(
                "### Android 端 120fps 流式 Markdown 核心优化方案\n\n",
                "要实现极致丝滑的流式富文本体验，必须解决以下四大核心痛点：\n\n",
                "| 瓶颈环节 | 传统做法 | 本方案优化实践 |\n",
                "| :--- | :--- | :--- |\n",
                "| **列表刷新** | `notifyDataSetChanged` | **RecyclerView Payload 局部增量刷新** |\n",
                "| **语法突变** | 随流直出 | **`MarkdownStreamFixer` 虚拟补齐** |\n",
                "| **时钟流控** | 固定 Timer | **`TypewriterEngine` 自适应积压调速** |\n",
                "| **滚动冲突** | 强制跟随 | **手势识别 + 悬浮回底按钮** |\n\n",
                "```kotlin\n",
                "// 在 Adapter 中拦截 Payload 刷新，耗时 < 1ms：\n",
                "override fun onBindViewHolder(holder: ViewHolder, pos: Int, payloads: List<Any>) {\n",
                "    if (payloads.contains(PAYLOAD_STREAM_CONTENT)) {\n",
                "        holder.updateTextOnly(mMarkwon, item)\n",
                "    } else {\n",
                "        holder.bindFull(item)\n",
                "    }\n",
                "}\n",
                "```",
            )
        }

        if (prompt.contains("SQL", ignoreCase = true) || prompt.contains("数据库") || prompt.contains("索引") || prompt.contains("查询")) {
            return listOf(
                "### SQL 复杂多表查询与索引调优实战\n\n",
                "```sql\n",
                "-- 统计各模型在过去 7 天的 P99 响应延迟与 Token 消耗\n",
                "SELECT \n",
                "    m.model_name,\n",
                "    COUNT(r.request_id) AS total_calls,\n",
                "    ROUND(AVG(r.latency_ms), 2) AS avg_latency,\n",
                "    MAX(r.latency_ms) AS max_latency,\n",
                "    SUM(r.tokens_used) AS total_tokens\n",
                "FROM \n",
                "    models m\n",
                "INNER JOIN \n",
                "    request_logs r ON m.id = r.model_id\n",
                "WHERE \n",
                "    r.created_at >= NOW() - INTERVAL 7 DAY\n",
                "GROUP BY \n",
                "    m.model_name\n",
                "HAVING \n",
                "    total_calls > 500\n",
                "ORDER BY \n",
                "    total_tokens DESC;\n",
                "```\n\n",
                "**索引调优建议**：\n",
                "在 `request_logs` 表建立联合索引：`CREATE INDEX idx_model_created ON request_logs(model_id, created_at);`。",
            )
        }

        if (prompt.contains("Hilt", ignoreCase = true) || prompt.contains("Koin", ignoreCase = true) || prompt.contains("依赖注入") || prompt.contains("DI", ignoreCase = true)) {
            return listOf(
                "### Android 依赖注入方案对比：Hilt vs Koin\n\n",
                "| 维度 | Google Hilt | Koin |\n",
                "| :--- | :--- | :--- |\n",
                "| **实现原理** | 基于 Dagger APT/KSP 编译期生成代码 | 纯 Kotlin 运行时反射 / DSL Service Locator |\n",
                "| **编译开销** | 增加注解处理器耗时 | **零编译期开销** |\n",
                "| **运行性能** | **极高**（直接方法调用） | 包含运行时查找轻微损耗 |\n",
                "| **错误排查** | **编译期校验**，不合规直接报错 | 运行时解析，可能抛出 `InstanceCreationException` |\n\n",
                "```kotlin\n",
                "// Hilt 构造注入\n",
                "@HiltViewModel\n",
                "class ChatViewModel @Inject constructor(\n",
                "    private val repo: ChatRepository\n",
                ") : ViewModel()\n",
                "```\n\n",
                "**选型建议**：大型商业 App 首选 **Hilt** 获得编译期安全保障；中小型或快速原型项目推荐 **Koin**。",
            )
        }

        // 3. 通用自定义提问动态回复
        return listOf(
            "### 关于「$trimmed」的分析与解答\n\n",
            "感谢您的提问！针对您提出的 **$trimmed**，为您整理了以下核心分析与实践思路：\n\n",
            "1. **核心要点分析**：\n",
            "   - 在架构设计中，`$trimmed` 应当明确边界职责，避免与 UI 生命周期产生耦合；\n",
            "   - 推荐使用声明式状态流（如 `StateFlow`）与单向数据流（UDF）模式进行数据流转；\n\n",
            "2. **推荐实现范式示例**：\n\n",
            "```kotlin\n",
            "// 针对「$trimmed」的通用设计模式\n",
            "class FeatureHandler {\n",
            "    fun executeTask(param: String): Result<String> {\n",
            "        return runCatching {\n",
            "            // 处理「$trimmed」业务链路\n",
            "            \"Task [$trimmed] completed successfully.\"\n",
            "        }\n",
            "    }\n",
            "}\n",
            "```\n\n",
            "> 💡 **提示**：您可以点击下方快捷提示词进一步探索 SSE 流式推流、Markdown 语法高亮与打字机流控！",
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mMockStreamJob?.cancel()
        mEngine.reset()
    }
}
