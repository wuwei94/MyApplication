package com.example.william.my.module.markdown.activity

import android.os.Bundle
import android.text.Spanned
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.markdown.databinding.MarkdownActivityHighlightBinding
import com.example.william.my.module.markdown.grammar.MyGrammarLocator
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * Prism4j 多语言代码语法高亮示例
 *
 * Markwon 结合 Prism4j 词法分析器，在 Android 原生端实现纯 Java/Kotlin 离线代码语法高亮。
 *
 * 核心特性与技术亮点：
 * 1. 多语言语法表覆盖：Kotlin、Java、Python、JavaScript、TypeScript、JSON、SQL、Bash、C/C++ 等
 * 2. 丰富的主题色彩：支持 Prism4jThemeDarkula（暗黑主题）与 Prism4jThemeDefault（明亮主题）
 * 3. 异步染色解析：展示如何将 CPU 密集的 AST 构建与词法正则匹配派发到后台协程（Dispatchers.Default），避免主线程掉帧
 * 4. 原生 Spannable：语法着色结果直接以 ForegroundColorSpan 渲染在 TextView 中，内存极轻、无 WebView 损耗
 *
 * https://github.com/noties/Prism4j
 */
@Route(path = RouterPath.Markdown.MarkwonHighlight)
class MarkwonHighlightActivity : BasicLayoutActivity() {

    private lateinit var mHighlightBinding: MarkdownActivityHighlightBinding
    private lateinit var mTextView: TextView

    private lateinit var mPrism4j: Prism4j
    private lateinit var mDarkulaMarkwon: Markwon
    private lateinit var mLightMarkwon: Markwon

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        mHighlightBinding = MarkdownActivityHighlightBinding.inflate(LayoutInflater.from(this))
        mTextView = mHighlightBinding.markdownHighlightTextView
        setView(mHighlightBinding.root)

        initPrism4j()

        // 默认展示 Kotlin / Java 高亮
        showKotlinJavaCode()
    }

    private fun initPrism4j() {
        mPrism4j = Prism4j(MyGrammarLocator())

        val tableTheme = TableTheme.Builder()
            .tableBorderWidth(dpToPx(1))
            .tableBorderColor(0x33888888.toInt())
            .tableCellPadding(dpToPx(8))
            .tableHeaderRowBackgroundColor(0x18888888.toInt())
            .tableEvenRowBackgroundColor(0x08888888.toInt())
            .build()

        // 1. Darkula 暗黑代码主题
        val darkulaTheme = Prism4jThemeDarkula.create()
        mDarkulaMarkwon = Markwon.builder(this)
            .usePlugin(CorePlugin.create())
            .usePlugin(TablePlugin.create(tableTheme))
            .usePlugin(SyntaxHighlightPlugin.create(mPrism4j, darkulaTheme))
            .build()

        // 2. Default 明亮代码主题
        val defaultTheme = Prism4jThemeDefault.create()
        mLightMarkwon = Markwon.builder(this)
            .usePlugin(CorePlugin.create())
            .usePlugin(TablePlugin.create(tableTheme))
            .usePlugin(SyntaxHighlightPlugin.create(mPrism4j, defaultTheme))
            .build()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }

    override fun buildList(): ArrayList<String> {
        val list = arrayListOf<String>()
        list.add("1. Kotlin & Java 高阶语法高亮（协程 / 泛型 / 注解）")
        list.add("2. Python & JS / TS（装饰器 / 异步 / JSON）")
        list.add("3. SQL & Linux Shell / Bash 脚本")
        list.add("4. C / C++ 系统编程（宏 / 模板 / 指针）")
        list.add("5. 暗黑主题模式 (Prism4jThemeDarkula)")
        list.add("6. 明亮主题模式 (Prism4jThemeDefault)")
        list.add("7. 异步后台协程高亮与耗时评测")
        return list
    }

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> showKotlinJavaCode()
            1 -> showPythonJsCode()
            2 -> showSqlBashCode()
            3 -> showCppCode()
            4 -> showDarkulaThemeComparison()
            5 -> showLightThemeComparison()
            6 -> showAsyncBenchmark()
        }
    }

    /**
     * 1. Kotlin & Java 示例
     */
    private fun showKotlinJavaCode() {
        val markdown = """
            # Kotlin & Java 语法高亮
            
            Prism4j 能够精准识别语言关键字、函数调用、注解、字符串插值与标点符号。
            
            ### Kotlin 协程与 Flow 数据流
            ```kotlin
            @OptIn(ExperimentalCoroutinesApi::class)
            class ChatViewModel(
                private val repository: SseRepository
            ) : ViewModel() {
            
                private val _messageFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
                val messageFlow: StateFlow<List<ChatMessage>> = _messageFlow.asStateFlow()
            
                fun sendMessage(prompt: String) = viewModelScope.launch(Dispatchers.IO) {
                    val userMsg = ChatMessage(id = UUID.randomUUID().toString(), content = prompt, isUser = true)
                    _messageFlow.update { it + userMsg }
            
                    repository.streamChat(prompt)
                        .flowOn(Dispatchers.IO)
                        .catch { e -> Log.e("Chat", "Stream failed", e) }
                        .collect { chunk ->
                            // 动态追加 AI 回复片段
                            updateAssistantMessage(chunk)
                        }
                }
            }
            ```
            
            ### Java 并发与反射示例
            ```java
            public class ThreadPoolManager {
                private static final int CORE_POOL_SIZE = 4;
                private static final int MAX_POOL_SIZE = 8;
                private final ExecutorService mExecutor;
            
                public ThreadPoolManager() {
                    this.mExecutor = new ThreadPoolExecutor(
                        CORE_POOL_SIZE,
                        MAX_POOL_SIZE,
                        60L, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(128),
                        new ThreadFactoryBuilder().setNameFormat("worker-%d").build()
                    );
                }
            
                public <T> CompletableFuture<T> submitTask(Callable<T> task) {
                    return CompletableFuture.supplyAsync(() -> {
                        try {
                            return task.call();
                        } catch (Exception e) {
                            throw new CompletionException(e);
                        }
                    }, mExecutor);
                }
            }
            ```
        """.trimIndent()

        mDarkulaMarkwon.setMarkdown(mTextView, markdown)
    }

    /**
     * 2. Python & JS 示例
     */
    private fun showPythonJsCode() {
        val markdown = """
            # Python & JavaScript 语法高亮
            
            ### Python 异步与类型提示 (FastAPI Server)
            ```python
            from typing import AsyncGenerator
            from fastapi import FastAPI, HTTPException
            from fastapi.responses import StreamingResponse
            import asyncio
            
            app = FastAPI(title="AI Stream Server")
            
            async def generate_chat_stream(prompt: str) -> AsyncGenerator[str, None]:
                chunks = ["你好！", "这是", "由 Fast", "API 推送", "的流式 Markdown", "内容。"]
                for chunk in chunks:
                    await asyncio.sleep(0.08)  # 模拟大模型推理延迟
                    yield f"data: {chunk}\n\n"
                yield "data: [DONE]\n\n"
            
            @app.post("/v1/chat/completions")
            async def chat_endpoint(prompt: str):
                if not prompt:
                    raise HTTPException(status_code=400, detail="Prompt cannot be empty")
                return StreamingResponse(generate_chat_stream(prompt), media_type="text/event-stream")
            ```
            
            ### JavaScript / TypeScript 现代语法
            ```javascript
            // ES2022 异步流式解析器
            export class StreamDecoder {
                #buffer = '';
                
                constructor(onChunkReceived) {
                    this.onChunk = onChunkReceived;
                }
                
                async processStream(readableStream) {
                    const reader = readableStream.getReader();
                    const decoder = new TextDecoder('utf-8');
                    
                    try {
                        while (true) {
                            const { done, value } = await reader.read();
                            if (done) break;
                            
                            const text = decoder.decode(value, { stream: true });
                            this.onChunk?.(text);
                        }
                    } finally {
                        reader.releaseLock();
                    }
                }
            }
            ```
        """.trimIndent()

        mDarkulaMarkwon.setMarkdown(mTextView, markdown)
    }

    /**
     * 3. SQL & Bash 示例
     */
    private fun showSqlBashCode() {
        val markdown = """
            # SQL & Linux Shell 脚本高亮
            
            ### SQL 复杂查询与聚合
            ```sql
            -- 查询过去 30 天内各模型请求吞吐量与平均延迟
            SELECT 
                model_name,
                COUNT(request_id) AS total_requests,
                ROUND(AVG(latency_ms), 2) AS avg_latency,
                SUM(prompt_tokens + completion_tokens) AS total_tokens
            FROM 
                ai_request_logs
            WHERE 
                created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                AND status_code = 200
            GROUP BY 
                model_name
            HAVING 
                total_requests > 100
            ORDER BY 
                total_tokens DESC
            LIMIT 10;
            ```
            
            ### Linux Shell / Bash 自动化脚本
            ```bash
            #!/usr/bin/env bash
            set -euo pipefail
            
            # 自动化 Gradle 依赖与产物构建
            PROJECT_DIR="$(cd "$(dirname "${'$'}{BASH_SOURCE[0]}")" && pwd)"
            OUTPUT_DIR="${'$'}{PROJECT_DIR}/build/outputs/apk/release"
            
            echo "[INFO] Starting clean build for module_markdown..."
            ./gradlew :modules:module_markdown:assembleRelease --no-daemon
            
            if [ -d "${'$'}OUTPUT_DIR" ]; then
                echo "[SUCCESS] Build artifact found at: ${'$'}OUTPUT_DIR"
                ls -lh "${'$'}OUTPUT_DIR"/*.apk
            else
                echo "[ERROR] Output directory not found!" >&2
                exit 1
            fi
            ```
        """.trimIndent()

        mDarkulaMarkwon.setMarkdown(mTextView, markdown)
    }

    /**
     * 4. C / C++ 示例
     */
    private fun showCppCode() {
        val markdown = """
            # C / C++ 底层系统与 NDK 编程
            
            ```cpp
            #include <iostream>
            #include <memory>
            #include <vector>
            #include <thread>
            #include <mutex>
            
            // 基于 RAII 与智能指针的线程安全缓存池
            template <typename T>
            class SafeResourcePool {
            public:
                void push(std::unique_ptr<T> resource) {
                    std::lock_guard<std::mutex> lock(mMtx);
                    mPool.push_back(std::move(resource));
                }
            
                std::unique_ptr<T> pop() {
                    std::lock_guard<std::mutex> lock(mMtx);
                    if (mPool.empty()) {
                        return nullptr;
                    }
                    auto item = std::move(mPool.back());
                    mPool.pop_back();
                    return item;
                }
            
            private:
                std::vector<std::unique_ptr<T>> mPool;
                std::mutex mMtx;
            };
            
            int main() {
                auto pool = std::make_unique<SafeResourcePool<int>>();
                pool->push(std::make_unique<int>(1024));
                std::cout << "Resource initialized successfully." << std::endl;
                return 0;
            }
            ```
        """.trimIndent()

        mDarkulaMarkwon.setMarkdown(mTextView, markdown)
    }

    /**
     * 5. 暗黑主题模式
     */
    private fun showDarkulaThemeComparison() {
        val markdown = """
            # 暗黑代码主题 (Prism4jThemeDarkula)
            
            当前正在使用 **Prism4jThemeDarkula**，适合暗黑模式或代码气泡背景：
            
            ```kotlin
            // 经典 IntelliJ IDEA Darkula 配色
            data class UserProfile(
                val userId: Long,
                val username: String,
                val email: String,
                val isVip: Boolean = false
            ) {
                fun getDisplayName(): String = if (isVip) "👑 ${'$'}username" else username
            }
            ```
            
            ```json
            {
              "status": "success",
              "code": 200,
              "data": {
                "id": 10086,
                "name": "DeepSeek-V3",
                "tokens": 4096
              }
            }
            ```
        """.trimIndent()

        mDarkulaMarkwon.setMarkdown(mTextView, markdown)
    }

    /**
     * 6. 明亮主题模式
     */
    private fun showLightThemeComparison() {
        val markdown = """
            # 明亮代码主题 (Prism4jThemeDefault)
            
            当前正在使用 **Prism4jThemeDefault**，适合浅色卡片背景：
            
            ```kotlin
            // 经典 GitHub Light 配色
            data class UserProfile(
                val userId: Long,
                val username: String,
                val email: String,
                val isVip: Boolean = false
            ) {
                fun getDisplayName(): String = if (isVip) "👑 ${'$'}username" else username
            }
            ```
            
            ```json
            {
              "status": "success",
              "code": 200,
              "data": {
                "id": 10086,
                "name": "DeepSeek-V3",
                "tokens": 4096
              }
            }
            ```
        """.trimIndent()

        mLightMarkwon.setMarkdown(mTextView, markdown)
    }

    /**
     * 7. 异步协程后台高亮与耗时评测
     */
    private fun showAsyncBenchmark() {
        val heavyMarkdown = """
            # 异步协程后台高亮解析评测
            
            > 在流式输出长代码块时，如果每帧在主线程重新构建 AST 并进行正则词法染色，极易引起掉帧。
            > **推荐最佳实践**：在 `Dispatchers.Default` 后台协程构建 `Spanned`，然后无缝切换回主线程绑定。
            
            ```kotlin
            suspend fun renderMarkdownAsync(context: Context, rawText: String): Spanned = withContext(Dispatchers.Default) {
                // 1. 在后台线程构建 Node AST 并执行 Prism 语法染色
                val markwon = MarkwonSingleton.getInstance(context)
                val node = markwon.parse(rawText)
                
                // 2. 生成原生 SpannableString
                markwon.render(node)
            }
            
            // 在 UI 层主线程极速赋值（耗时 < 1ms，0 掉帧）：
            lifecycleScope.launch {
                val spanned = renderMarkdownAsync(this@MarkwonHighlightActivity, heavyMarkdown)
                textView.text = spanned
            }
            ```
            
            ```python
            # 性能监控数据结构
            class PerformanceMetrics:
                def __init__(self, parse_time_ms: float, render_time_ms: float):
                    self.parse_time_ms = parse_time_ms
                    self.render_time_ms = render_time_ms
                    self.total_time_ms = parse_time_ms + render_time_ms
            ```
        """.trimIndent()

        lifecycleScope.launch {
            var parseTime: Long
            var spanned: Spanned

            val totalTime = measureTimeMillis {
                // 1. 在后台线程执行 CPU 密集型解析与 Prism4j 正则高亮
                spanned = withContext(Dispatchers.Default) {
                    parseTime = measureTimeMillis {
                        val node = mDarkulaMarkwon.parse(heavyMarkdown)
                        mDarkulaMarkwon.render(node)
                    }
                    mDarkulaMarkwon.toMarkdown(heavyMarkdown)
                }
            }

            // 2. 主线程极速渲染
            mDarkulaMarkwon.setParsedMarkdown(mTextView, spanned)

            Toast.makeText(
                this@MarkwonHighlightActivity,
                "后台异步解析耗时: ${totalTime}ms (主线程零卡顿)",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
