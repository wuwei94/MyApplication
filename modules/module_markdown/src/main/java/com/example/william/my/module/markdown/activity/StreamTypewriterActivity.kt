package com.example.william.my.module.markdown.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.markdown.databinding.MarkdownActivityTypewriterBinding
import com.example.william.my.module.markdown.engine.MarkdownStreamFixer
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 流式 Markdown 打字机与未闭合语法容错示例
 *
 * 核心技术组合：
 * 1. TypewriterEngine：动态自适应出字速率控制（积压加速 + 标点呼吸停顿 + 暂停/跳过）；
 * 2. MarkdownStreamFixer：实时检测未闭合的 ``` 代码块与行内富文本标签，虚拟闭合防止界面跳动闪烁；
 * 3. Prism4j + Markwon：代码块语法着色与原生 Spannable 高性能渲染；
 * 4. Channel.CONFLATED 顺序流控：保证单调递增渲染与丝滑吸底。
 */
@Route(path = RouterPath.Markdown.StreamTypewriter)
class StreamTypewriterActivity : BasicLayoutActivity() {

    private lateinit var mTypewriterBinding: MarkdownActivityTypewriterBinding
    private lateinit var mTextView: TextView
    private lateinit var mScrollView: NestedScrollView

    private lateinit var mMarkwon: Markwon
    private val mEngine = TypewriterEngine()
    private var mMockNetworkJob: Job? = null

    // 顺序流控通道：汇聚高频推流，保证单调递增渲染与零竞态闪烁
    private val mRenderChannel = Channel<Pair<String, Boolean>>(Channel.CONFLATED)
    private var mRenderJob: Job? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        mTypewriterBinding = MarkdownActivityTypewriterBinding.inflate(LayoutInflater.from(this))
        mTextView = mTypewriterBinding.markdownTypewriterTextView
        mScrollView = mTypewriterBinding.nestedScrollView
        setView(mTypewriterBinding.root)

        initMarkwon()
        initRenderPipeline()
        initEngine()

        // 默认开始突发推流演示
        startBurstStreamDemo()
    }

    private fun initMarkwon() {
        val prism4j = Prism4j(MyGrammarLocator())
        val darkulaTheme = Prism4jThemeDarkula.create()

        val tableTheme = TableTheme.Builder()
            .tableBorderWidth(dpToPx(1))
            .tableBorderColor(0x33888888.toInt())
            .tableCellPadding(dpToPx(10))
            .tableHeaderRowBackgroundColor(0x18888888.toInt())
            .tableEvenRowBackgroundColor(0x08888888.toInt())
            .build()

        val tableWidthProvider: () -> Int = {
            if (mTextView.width > 0) {
                mTextView.width - mTextView.paddingLeft - mTextView.paddingRight
            } else {
                resources.displayMetrics.widthPixels - dpToPx(32)
            }
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

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density + 0.5f).toInt()

    private fun initRenderPipeline() {
        mRenderJob?.cancel()
        mRenderJob = lifecycleScope.launch(Dispatchers.Default) {
            for ((rawText, isFinished) in mRenderChannel) {
                // 1. 进行流式未闭合语法修复与呼吸光标追加
                val fixedMarkdown = MarkdownStreamFixer.fix(rawText, appendCursor = !isFinished)

                // 2. 解析 AST 并生成 Spannable
                val node = mMarkwon.parse(fixedMarkdown)
                val spanned = mMarkwon.render(node)

                // 3. 提交主线程渲染
                withContext(Dispatchers.Main) {
                    mTextView.text = spanned
                    // 4. 吸底滚动（直接计算位移，避免 fullScroll 动画打断引起震颤）
                    scrollToBottom()
                }
            }
        }
    }

    private fun renderMarkdownSequentially(rawText: String, isFinished: Boolean) {
        mRenderChannel.trySend(rawText to isFinished)
    }

    private fun scrollToBottom() {
        mScrollView.post {
            val child = mScrollView.getChildAt(0) ?: return@post
            val scrollRange = child.bottom - mScrollView.height + mScrollView.paddingBottom
            if (scrollRange > 0) {
                mScrollView.scrollTo(0, scrollRange)
            }
        }
    }

    private fun initEngine() {
        mEngine.setOnTextUpdateListener { rawText, isFinished ->
            renderMarkdownSequentially(rawText, isFinished)
        }

        mEngine.setOnMetricsListener { backlog, speedMs, state ->
            lifecycleScope.launch(Dispatchers.Main) {
                mTypewriterBinding.tvEngineStatus.text = state.name
                mTypewriterBinding.tvBufferBacklog.text = "$backlog 字"
                mTypewriterBinding.tvCurrentSpeed.text = "$speedMs ms/字"

                // 状态色彩更新
                mTypewriterBinding.tvEngineStatus.setTextColor(
                    when (state) {
                        TypewriterEngine.State.TYPING -> 0xFF4CAF50.toInt()
                        TypewriterEngine.State.PAUSED -> 0xFFFF9800.toInt()
                        TypewriterEngine.State.COMPLETED -> 0xFF2196F3.toInt()
                        TypewriterEngine.State.IDLE -> 0xFF9E9E9E.toInt()
                    },
                )
            }
        }
    }

    override fun buildList(): ArrayList<String> {
        val list = arrayListOf<String>()
        list.add("1. 突发推流 (Burst Stream - 大段 Markdown 自适应加速)")
        list.add("2. 平缓推流 (Smooth Stream - 逐字细腻呼吸感)")
        list.add("3. 代码块补全验证 (Auto-close Fixer - 流式语法不崩溃)")
        list.add("4. 标点节奏停顿演示 (Punctuation Rhythm)")
        list.add("5. 暂停 / 恢复 (Pause & Resume)")
        list.add("6. 一键跳过 / 立即完成 (Skip to Finish)")
        list.add("7. 重置清空 (Reset)")
        return list
    }

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> startBurstStreamDemo()
            1 -> startSmoothStreamDemo()
            2 -> startCodeFixerDemo()
            3 -> startPunctuationRhythmDemo()
            4 -> togglePauseResume()
            5 -> mEngine.skipToFinish()
            6 -> resetAll()
        }
    }

    /**
     * 1. 突发推流演示
     */
    private fun startBurstStreamDemo() {
        resetAll()
        mEngine.start(lifecycleScope)

        val chunks = listOf(
            "# 现代 AI 流式交互核心原理\n\n",
            "在真实的大模型 SSE 推流场景中，服务端推送往往是**突发性的**（Burst）。\n\n",
            "例如，大模型可能思考 200ms 后一次性吐出数十个字符：\n",
            "> 打字机引擎通过监测缓冲区积压量（Backlog），自动在 16ms ~ 36ms 之间动态切换出字速率，保证出字既有呼吸感，又不会落后网络推流！\n\n",
            "### 核心性能指标对比：\n\n",
            "| 模式 | 队列积压 | 出字节奏与策略 |\n",
            "| :--- | :--- | :--- |\n",
            "| **极速冲刺** | > 60 字符 | 18ms / 3字（冲刺追赶） |\n",
            "| **快速推进** | 20~60 字符 | 24ms / 2字（紧跟推流） |\n",
            "| **呼吸出字** | < 20 字符 | 36ms / 1字（细腻呼吸） |\n\n",
            "接下来是带有语法高亮的多行代码块演示：\n\n",
            "```kotlin\n",
            "class StreamEngine {\n",
            "    fun feed(chunk: String) {\n",
            "        buffer.append(chunk)\n",
            "    }\n",
            "}\n",
            "```\n\n",
            "整个流式过程自然丝滑，毫无界面闪烁！",
        )

        mMockNetworkJob = lifecycleScope.launch {
            for (chunk in chunks) {
                mEngine.feed(chunk)
                delay(120) // 模拟网络突发间隔
            }
            mEngine.complete()
        }
    }

    /**
     * 2. 平缓细腻推流演示
     */
    private fun startSmoothStreamDemo() {
        resetAll()
        mEngine.start(lifecycleScope)

        val text = "这是一个平缓细腻的打字机演示。网络推流以均匀的小节奏推送到客户端，打字机保持在 35ms 左右的稳定出字速度，光标在末尾优雅地闪烁。"

        mMockNetworkJob = lifecycleScope.launch {
            for (c in text) {
                mEngine.feed(c.toString())
                delay(30)
            }
            mEngine.complete()
        }
    }

    /**
     * 3. 复杂语法与嵌套补全演示
     */
    private fun startCodeFixerDemo() {
        resetAll()
        mEngine.start(lifecycleScope)

        val codeChunks = listOf(
            "### 流式语法自动补全与容错测试\n\n",
            "在逐字输出时，观察以下未闭合语法是否从第 1 字符起就保持完整稳定：\n\n",
            "1. **多行代码块实时高亮**：\n\n",
            "```kotlin\n",
            "suspend fun fetchStream(): Flow<String> = flow {\n",
            "    emit(\"Zero Jitter Markdown\")\n",
            "}\n",
            "```\n\n",
            "2. **行内语法与嵌套样式**：\n\n",
            "- 行内代码：`val response = api.call()` 即时闭合\n",
            "- 粗斜体嵌套：***这是加粗且斜体的流式文本*** 实时生效\n",
            "- 删除线：~~已废弃的旧版本逻辑~~ 稳定划线\n\n",
            "> MarkdownStreamFixer 单遍状态机保证了在闭合标签到达前，AST 语法树始终完整无闪烁！",
        )

        mMockNetworkJob = lifecycleScope.launch {
            for (chunk in codeChunks) {
                for (c in chunk) {
                    mEngine.feed(c.toString())
                    delay(22)
                }
                delay(80)
            }
            mEngine.complete()
        }
    }

    /**
     * 4. 标点呼吸停顿演示
     */
    private fun startPunctuationRhythmDemo() {
        resetAll()
        mEngine.start(lifecycleScope)

        val rhythmSentences = listOf(
            "### 标点呼吸停顿与节奏演示\n\n",
            "大模型的回答需要自然的节奏感。\n\n",
            "你看！遇到逗号时，会有约 150ms 的自然换气微停顿；\n",
            "遇到句号、感叹号与问号时？停顿会延长至约 280ms！\n\n",
            "段落换行也是一样。\n\n",
            "这种富有层次的呼吸节奏，让 AI 显得更加生动，就像是在实时思考一样！",
        )

        mMockNetworkJob = lifecycleScope.launch {
            for (sentence in rhythmSentences) {
                for (char in sentence) {
                    mEngine.feed(char.toString())
                    delay(25)
                }
            }
            mEngine.complete()
        }
    }

    private var mIsPaused = false

    private fun togglePauseResume() {
        mIsPaused = !mIsPaused
        if (mIsPaused) {
            mEngine.pause()
            Toast.makeText(this, "打字机已暂停", Toast.LENGTH_SHORT).show()
        } else {
            mEngine.resume()
            Toast.makeText(this, "打字机已继续", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetAll() {
        mMockNetworkJob?.cancel()
        mMockNetworkJob = null
        mEngine.reset()
        mTextView.text = ""
        mScrollView.scrollTo(0, 0)
        mIsPaused = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mMockNetworkJob?.cancel()
        mRenderJob?.cancel()
        mEngine.reset()
    }
}
