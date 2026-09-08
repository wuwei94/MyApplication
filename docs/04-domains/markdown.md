# Markdown 富文本渲染与 AI 流式交互开发指南

> 本文档系统梳理 `:modules:module_markdown` 的 **Markdown 富文本渲染与 AI 流式交互**体系：从 Markwon 原生 Spannable 渲染、Prism4j 多语言语法高亮，到流式打字机与未闭合语法容错引擎，最终落地为「RecyclerView Payload 局部增量刷新 + 智能吸底」的完整 AI 聊天实战界面。模块 Activity 清单见 [模块总览](../05-catalog/modules.md)。

---

## 一、演进体系总览

模块按四条递进阶段组织，每阶段独立可演示、可组合复用（引擎层纯 Kotlin，无页面耦合）：

| 阶段 | 能力 | 关键类 / 依赖 | 示例路由 |
| :--- | :--- | :--- | :--- |
| 1 | Markwon 基础与扩展语法渲染（标题 / 引用 / 表格 / 任务清单 / HTML / 图片 / 主题） | `Markwon`、`TablePlugin`、`TaskListPlugin`、`HtmlPlugin`、`GlideImagesPlugin` | `/Markdown/MarkwonBasic` |
| 2 | Prism4j 多语言代码语法高亮（Darkula / Default 双主题，异步染色） | `Prism4j` + `MyGrammarLocator`、`SyntaxHighlightPlugin` | `/Markdown/MarkwonHighlight` |
| 3 | 流式打字机与未闭合语法容错（动态调速 + 单遍状态机补齐） | `TypewriterEngine`、`MarkdownStreamFixer`、`GfmTablePlugin` | `/Markdown/StreamTypewriter` |
| 4 | AI 流式对话完整实战（Payload 局部刷新 + 智能吸底 + 停止生成） | `ChatAdapter`、`ChatMessage`（复用阶段 2/3 引擎） | `/Markdown/AiChat` |

底层渲染选型：**Markwon**（基于 commonmark-java 解析 Markdown AST → 生成 Android 原生 `Spannable` 直接赋给 `TextView`），对比 WebView / Compose RichText 等方案见 [阶段一](#二阶段-1markwon-基础渲染) 末尾选型表。

---

## 二、阶段 1：Markwon 基础渲染（`MarkwonBasicActivity`）

页面演示 7 组示例：基础排版、GFM 表格、任务清单、HTML 标签与删除线、超链接与图片、自定义主题、综合长文档。核心是把「插件组合 + 主题定制」显式暴露出来：

| 能力 | 插件 | 说明 |
| :--- | :--- | :--- |
| 基础语法 | `CorePlugin` | 标题 H1~H6、粗斜体、引用块、多级列表、分割线 |
| GFM 表格 | `TablePlugin.create(tableTheme)` | 多列对齐、表头/斑马纹背景通过 `TableTheme` 定制 |
| 任务清单 | `TaskListPlugin` | `- [x] Task` 复选框语法 |
| 删除线 / HTML | `StrikethroughPlugin`、`HtmlPlugin` | `~~文本~~`、`<u>/<font color>/<sub>/<sup>` 等轻量标签 |
| 图片 | `GlideImagesPlugin` | 异步下载 + 内联渲染 + 缓存 |
| 交互拦截 | `AbstractMarkwonPlugin` | 自定义 `linkResolver` 拦截链接点击 |
| 主题定制 | `MarkwonTheme.Builder` | 引用条颜色/宽度、代码块背景与文字色、列表圆点 |

```kotlin
val markwon = Markwon.builder(this)
    .usePlugin(CorePlugin.create())
    .usePlugin(TablePlugin.create(tableTheme))
    .usePlugin(TaskListPlugin.create(this))
    .usePlugin(StrikethroughPlugin.create())
    .usePlugin(HtmlPlugin.create())
    .usePlugin(GlideImagesPlugin.create(this))
    // 自定义主题：绿色引用条 + 暗色代码块
    .usePlugin(object : AbstractMarkwonPlugin() {
        override fun configureTheme(builder: MarkwonTheme.Builder) { ... }
    })
    .build()
markwon.setMarkdown(textView, markdownContent)
```

**渲染方案选型**（页面示例中的对比口径）：Markwon 走原生 Spannable，内存极低、适合 AI 聊天与轻量富文本；WebView 排版自由度高但跨进程 IPC 开销大、流式场景体验差；Compose RichText 适合纯 Compose 页面内嵌；RichTextView 仅适合特殊复合控件定制场景。

---

## 三、阶段 2：Prism4j 多语言代码高亮（`MarkwonHighlightActivity`）

Prism4j 是 Web 端 Prism 的 JVM/Android 移植，配合 `MyGrammarLocator`（模块内自定义 `GrammarLocator`）在端侧**离线**完成词法染色：

* **语言覆盖**：clike 基类 + Kotlin / Java / Python / JavaScript / TypeScript / JSON / SQL / Bash / C / C++ / Markdown / HTML，并注册别名（`kt`/`kts`、`js`/`ts`、`py`、`sh`、`c++`/`hpp` 等）；
* **主题**：`Prism4jThemeDarkula`（暗黑）与 `Prism4jThemeDefault`（明亮）经 `SyntaxHighlightPlugin` 接入 Markwon，`ForegroundColorSpan` 直绘于 TextView；
* **异步染色实践**：页面第 7 组示例展示「在 `Dispatchers.Default` 构建 AST 与正则匹配 → 切回主线程极速赋值」，并用 `measureTimeMillis` 实测耗时，规避流式长代码块在主线程每帧重建导致的掉帧。

```kotlin
val prism4j = Prism4j(MyGrammarLocator())
val markwon = Markwon.builder(this)
    .usePlugin(SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDarkula.create()))
    .build()
```

---

## 四、阶段 3：流式打字机与语法容错（`StreamTypewriterActivity`）

本阶段解决「大模型突发推流」下的两个核心问题——**出字节奏**与**未闭合语法闪烁**，并引入顺序渲染管线，是阶段 4 聊天页的引擎来源。

### 1. `TypewriterEngine`：动态自适应流控

以「待出字符积压量（backlog）」为反馈信号三档调速，配合标点呼吸停顿，兼顾追赶效率与阅读呼吸感：

| 积压量 | 步长 | 出字间隔 | 策略 |
| :--- | :--- | :--- | :--- |
| `> 80` 字符 | 一次 2 字 | 20ms | 积压追赶（约 100 字/秒） |
| `30 ~ 80` | 一次 1 字 | 22ms | 适度加速（约 45 字/秒） |
| `< 30` | 一次 1 字 | 30ms | 基础轻快出字 |

标点停顿：句末重标点（`。！？.!?\n`）追加 160ms，句中轻标点（`，、；：,;:`）追加 80ms，模拟真实思考呼吸感。状态机 `IDLE / TYPING / PAUSED / COMPLETED`，对外暴露 `feed()` 追加、`complete()` 结束、`pause()/resume()`、`skipToFinish()` 一键出完、`reset()`，并可通过 metrics 监听实时回显积压量与当前速度。

### 2. `MarkdownStreamFixer`：单遍扫描状态机补齐未闭合语法

流式逐字输出时，` ```kotlin`、`**加粗**`、`~~删除线~~` 等语法在闭合符到达前是不完整的，直接渲染会导致 AST 反复坍塌、界面闪烁。修复器以 **O(N) 单遍线性扫描 + LIFO 栈** 处理：

* **上下文隔离**：多行代码块内忽略行内符号；行内代码内忽略粗体/斜体/删除线；严格跳过 `\` 转义；
* **栈式闭合**：对 `***` / `**` / `*` / `~~` 等嵌套标记，用栈记录开启顺序，扫描结束时按 LIFO **逆序虚拟补齐**；
* **特殊处理**：代码块未闭合则追加换行与 ` ``` `；表格分隔行（如 `| :--- |`）末尾不插呼吸光标，避免破坏 GFM 表格 AST；呼吸光标 `▍` 插在真实输入末尾、虚拟闭合符之前，呈现打字机效果。

### 3. 渲染管线与流式表格

`Channel(CONFLATED)` 汇聚高频推流（丢弃中间态、只保留最新），后台协程统一执行「修复 → 解析 → 渲染 Spannable → 切主线程赋值 + 吸底」，保证单调递增渲染与零竞态闪烁。表格使用自研 **`GfmTablePlugin`**（`StreamTableRowSpan` 自定义 Span + `widthProvider` 动态测宽），使 GFM 表格宽度贴合气泡/TextView 实际可用宽度，替代标准 `TablePlugin` 的固定排版。

---

## 五、阶段 4：AI 聊天完整实战（`AiChatActivity`）

把前三个阶段引擎拼装为完整 IM 形态的聊天界面（`BaseVBActivity` + RecyclerView + 底部输入 + 预设快捷提示词 + 欢迎语），核心架构决策：

| 痛点 | 本页做法 | 关键代码点 |
| :--- | :--- | :--- |
| 高频全量刷新卡顿 | **Payload 局部增量刷新**：关闭 `itemAnimator`，流式出字期间优先驱动活跃 `AssistantViewHolder.updateStreamContent()` 原地更新，ViewHolder 不可见时才走 `notifyItemChanged(pos, PAYLOAD_STREAM_CONTENT)` | `ChatAdapter.onBindViewHolder` 按 payload 分发 |
| 长消息吸底回弹 Bug | 禁用 `smoothScrollToPosition`（其 SNAP_TO_START 会把长消息弹回卡片顶部）；改为**物理增量吸底**——测量末条 item 底部溢出量 `bottomDiff`，用 `scrollBy/smoothScrollBy` 精确下移 | `scrollBottomDelta(smooth)` |
| 用户翻看历史被打断 | 仅在 `SCROLL_STATE_DRAGGING`（手指主动拖拽）时暂停吸底，展示「回到最新」悬浮按钮；惯性/程序滚动不误触 | `OnScrollListener` |
| 消息生命周期 | `ChatMessage.Status`：`SENDING → STREAMING → COMPLETED / FAILED`（停止生成）；用户/AI 两条消息**原子批量插入**避免列表两次高度跳跃 | `PAYLOAD_STATUS` 刷新状态位 |
| 文本复用 | 每条 AI 气泡持有一键全文复制 | `ClipboardManager` |

推流与引擎：点击发送后创建用户消息与 AI 占位消息 → `TypewriterEngine.start()` + 模拟推流协程（每次 2 字符、40ms 间隔、260ms 首字 TTFT）→ `MarkdownStreamFixer` 修复 + `Markwon`（含 `GfmTablePlugin`、Prism4j 高亮）渲染。停止生成 = 取消推流协程 + `skipToFinish()` 兜底出完 + 消息标记 `FAILED`。

---

## 六、工程示例索引

入口：`MarkdownMainActivity`（路由 `/Markdown/Main`，按四阶段分组导航）。完整 Activity 与路由清单见 [modules.md](../05-catalog/modules.md)。

| 类 | 归属 | 职责 |
| :--- | :--- | :--- |
| `MarkwonBasicActivity` / `MarkwonHighlightActivity` | activity | 阶段 1/2 演示页 |
| `TypewriterEngine` / `MarkdownStreamFixer` | engine | 阶段 3 流控与语法容错引擎（纯 Kotlin，可复用） |
| `GfmTablePlugin` / `StreamTableRowSpan` | plugin | 流式表格 Span 渲染 |
| `MyGrammarLocator` | grammar | Prism4j 语言语法表注册 |
| `ChatAdapter` / `ChatMessage` | chat | 阶段 4 聊天列表与消息模型 |
| `AiChatActivity` | activity | 阶段 4 完整实战界面 |

> 联动阅读：SSE 数据来源与逐 Token 增量解析见 [sse.md](sse.md)；长连接传输层（WebSocket / TCP）见 [socket.md](socket.md)。
