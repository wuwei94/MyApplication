package com.example.william.my.module.markdown.activity

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.markdown.databinding.MarkdownActivityBasicBinding
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin

/**
 * Markwon 基础与扩展 Markdown 渲染示例
 *
 * Markwon 是基于 commonmark-java 的 Android 原生 Markdown 渲染引擎，通过解析 Markdown AST
 * 生成 Android 原生 Spannable 富文本直接赋给 TextView，具备极高的渲染性能和可扩展性。
 *
 * 核心特性与扩展插件：
 * 1. 基础语法：标题（H1~H6）、粗体、斜体、引用块（Blockquote）、无序/有序多级列表、水平分割线
 * 2. GFM 扩展表格 (TablePlugin)：标准 Markdown 表格渲染，支持多列与对齐方式
 * 3. 任务清单 (TaskListPlugin)：支持 GFM 规范的任务复选框列表（`- [x] Task`）
 * 4. 删除线 (StrikethroughPlugin)：支持 `~~文本~~` 划线语法
 * 5. HTML 标签 (HtmlPlugin)：支持常见 HTML 标签渲染（如 `<u>`, `<span>`, `<b>`, `<font>`）
 * 6. 图片加载 (GlideImagesPlugin / ImagePlugin)：异步加载并在 TextView 中内联呈现图片
 * 7. 主题与样式定制 (MarkwonTheme)：引用条颜色/宽度、列表圆点尺寸、代码块背景与文字颜色等
 * 8. 交互拦截：自定义链接点击拦截与自定义事件响应
 *
 * https://github.com/noties/Markwon
 */
@Route(path = RouterPath.Markdown.MarkwonBasic)
class MarkwonBasicActivity : BasicLayoutActivity() {

    private lateinit var mMarkdownBinding: MarkdownActivityBasicBinding
    private lateinit var mTextView: TextView

    private lateinit var mDefaultMarkwon: Markwon
    private lateinit var mCustomThemeMarkwon: Markwon

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        // 绑定上方视图容器
        mMarkdownBinding = MarkdownActivityBasicBinding.inflate(LayoutInflater.from(this))
        mTextView = mMarkdownBinding.markdownTextView
        setView(mMarkdownBinding.root)

        initMarkwon()

        // 默认展示基础排版
        showBasicMarkdown()
    }

    private fun initMarkwon() {
        // 1. 标准全功能 Markwon（核心 + 表格 + 任务清单 + 删除线 + HTML + Glide 图片 + 链接拦截）
        mDefaultMarkwon = Markwon.builder(this)
            .usePlugin(CorePlugin.create())
            .usePlugin(TablePlugin.create(this))
            .usePlugin(TaskListPlugin.create(this))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(HtmlPlugin.create())
            .usePlugin(GlideImagesPlugin.create(this))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder.linkResolver { _, link ->
                        Toast.makeText(this@MarkwonBasicActivity, "点击了链接: $link", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            .build()

        // 2. 自定义主题 Markwon（定制引用条颜色、代码块背景、列表样式）
        mCustomThemeMarkwon = Markwon.builder(this)
            .usePlugin(CorePlugin.create())
            .usePlugin(TablePlugin.create(this))
            .usePlugin(TaskListPlugin.create(this))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(HtmlPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .blockQuoteColor(Color.parseColor("#4CAF50"))
                        .blockQuoteWidth(16)
                        .codeBlockBackgroundColor(Color.parseColor("#263238"))
                        .codeBlockTextColor(Color.parseColor("#80CBC4"))
                        .codeBackgroundColor(Color.parseColor("#E0F2F1"))
                        .codeTextColor(Color.parseColor("#00796B"))
                        .bulletWidth(20)
                }
            })
            .build()
    }

    override fun buildList(): ArrayList<String> {
        val list = arrayListOf<String>()
        list.add("1. 基础排版（标题 / 引用 / 列表 / 分割线）")
        list.add("2. GFM 扩展表格（多列对齐 & 复杂表格）")
        list.add("3. 任务清单 TaskLists（复选框状态）")
        list.add("4. HTML 标签与删除线（颜色 / 下划线 / 强调）")
        list.add("5. 超链接与图片（交互拦截与异步渲染）")
        list.add("6. 自定义主题样式（引用条绿色 / 暗色代码块 / 粗圆点）")
        list.add("7. 综合长文档（技术文档综合实战测试）")
        return list
    }

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> showBasicMarkdown()
            1 -> showTableMarkdown()
            2 -> showTaskListMarkdown()
            3 -> showHtmlAndStrikethroughMarkdown()
            4 -> showLinkAndImageMarkdown()
            5 -> showCustomThemeMarkdown()
            6 -> showComprehensiveMarkdown()
        }
    }

    /**
     * 1. 基础排版示例
     */
    private fun showBasicMarkdown() {
        val markdown = """
            # 一级标题 (H1)
            ## 二级标题 (H2)
            ### 三级标题 (H3)
            #### 四级标题 (H4)
            
            这是普通的正文段落，支持 **加粗文本 (Bold)**、*斜体文本 (Italic)*、***粗斜体 (Bold Italic)*** 以及 `inline code` 行内代码。
            
            ---
            
            > **引用块 (Blockquote)**
            > 这是一个嵌套的引用块段落。
            >> 这是二级嵌套引用内容，常用于 AI 回复的引用来源标注。
            
            ### 无序列表 (Unordered List)
            - 列表项 A：Kotlin 协程与 Flow 数据流
            - 列表项 B：Jetpack 现代架构组件
              - 子列表项 B-1：Lifecycle 生命周期感知
              - 子列表项 B-2：ViewModel 状态管理
            - 列表项 C：Markwon 高性能原生富文本渲染
            
            ### 有序列表 (Ordered List)
            1. 第一步：配置 Markwon Core 插件
            2. 第二步：接入 GFM Table / TaskList 扩展
            3. 第三步：实现打字机流控与代码高亮
        """.trimIndent()

        mDefaultMarkwon.setMarkdown(mTextView, markdown)
    }

    /**
     * 2. GFM 表格示例
     */
    private fun showTableMarkdown() {
        val markdown = """
            ## GFM 扩展表格 (TablePlugin)
            
            Markwon 支持 GitHub Flavored Markdown 规范的表格语法，并支持左对齐、居中和右对齐：
            
            | 方案 | 渲染机制 | 内存开销 | 适用场景 | 性能评级 |
            | :--- | :---: | :---: | :--- | ---: |
            | **Markwon** | 原生 Spannable | 极低 | AI 聊天、文章阅读、轻量富文本 | ⭐⭐⭐⭐⭐ |
            | **WebView** | WebKit 内核 | 较高 | 复杂排版网页、数学公式重度渲染 | ⭐⭐⭐ |
            | **Compose RichText** | Compose Text Layout | 中等 | 纯 Compose 页面内嵌 | ⭐⭐⭐⭐ |
            | **RichTextView** | 自定义 ViewGroup | 较高 | 特殊定制复合控件需求 | ⭐⭐⭐ |
            
            ### 混合格式单元格
            
            | 功能特性 | 支持状态 | 备注说明 |
            | :--- | :---: | :--- |
            | 基础排版 | ✅ | 标题、粗斜体、引用、分割线 |
            | 代码高亮 | ✅ | 配合 Prism4j 插件 |
            | 任务列表 | ✅ | 配合 TaskList 插件 |
            | 内联图片 | ✅ | 配合 Glide 异步加载插件 |
        """.trimIndent()

        mDefaultMarkwon.setMarkdown(mTextView, markdown)
    }

    /**
     * 3. 任务清单示例
     */
    private fun showTaskListMarkdown() {
        val markdown = """
            ## 任务清单 (TaskListPlugin)
            
            Markwon 任务列表插件支持 GFM 复选框语法，呈现直观的待办项状态：
            
            ### 流式 AI 聊天核心演进路线：
            - [x] **阶段 1：Markwon 基础与扩展渲染环境搭建**
              - [x] 引入 `markwon:core`、`ext-tables`、`ext-tasklist` 依赖
              - [x] 搭建 `MarkwonBasicActivity` 基础交互体验
              - [x] 验证表格、任务列表与 HTML 渲染
            - [ ] **阶段 2：Prism4j 代码语法高亮**
              - [ ] 引入 `prism4j` 词法解析引擎
              - [ ] 支持 Kotlin / Java / Python / JS / SQL 染色
              - [ ] 异步后台协程高亮优化
            - [ ] **阶段 3：流式打字机与未闭合语法容错**
              - [ ] 动态自适应 Buffer 出字速率调控
              - [ ] 未闭合 Markdown 标签与代码块自动补齐
              - [ ] 光标呼吸闪烁动效
            - [ ] **阶段 4：AI 聊天完整实战页面**
              - [ ] RecyclerView Payload 局部增量刷新
              - [ ] 智能贴底平滑滚动与手势打断
        """.trimIndent()

        mDefaultMarkwon.setMarkdown(mTextView, markdown)
    }

    /**
     * 4. HTML 标签与删除线示例
     */
    private fun showHtmlAndStrikethroughMarkdown() {
        val markdown = """
            ## HTML 标签 (HtmlPlugin) 与 删除线 (StrikethroughPlugin)
            
            ### 1. 删除线语法
            - 原价：~~¥ 999.00~~，现限时特惠：**¥ 19.90**
            - ~~旧版全量刷新导致的卡顿掉帧~~ ➔ **采用局部 Payload 刷新**
            
            ### 2. 丰富 HTML 标签支持
            Markwon 内置轻量 HTML 标签解析器：
            
            - 下划线：<u>Underline 文本下划线</u>
            - 字体颜色：<font color="#E91E63">粉色文字</font>、<font color="#2196F3">蓝色文字</font>、<font color="#4CAF50">绿色文字</font>
            - 行内强调：<span style="color:#FF5722; font-weight:bold;">橙色粗体 Span 样式</span>
            - 上标与下标：H<sub>2</sub>O（水分子式）、E = mc<sup>2</sup>（质能方程）
            - 换行与分割：使用 `<br>` 进行行内换行<br>下一行文本内容
        """.trimIndent()

        mDefaultMarkwon.setMarkdown(mTextView, markdown)
    }

    /**
     * 5. 超链接与图片示例
     */
    private fun showLinkAndImageMarkdown() {
        val markdown = """
            ## 超链接与图片 (GlideImagesPlugin)
            
            ### 1. 超链接与交互拦截
            Markwon 允许通过 `linkResolver` 自定义链接点击行为，点击下方链接将触发应用内 Toast 拦截反馈：
            
            - 访问 [Markwon 官方 GitHub 仓库](https://github.com/noties/Markwon)
            - 查看 [AndroidJetpack 官方文档](https://developer.android.google.cn/jetpack)
            - 探索 [Kotlin 协程官方指南](https://kotlinlang.org/docs/coroutines-overview.html)
            
            ### 2. 图片内嵌展示
            通过 `GlideImagesPlugin` 插件实现 Markdown 图片异步下载与渲染：
            
            ![Android Logo](https://developer.android.com/static/images/brand/Android_Robot.png)
            
            > 图片加载由 Glide 引擎驱动，支持自动缓存与占位。
        """.trimIndent()

        mDefaultMarkwon.setMarkdown(mTextView, markdown)
    }

    /**
     * 6. 自定义主题样式示例
     */
    private fun showCustomThemeMarkdown() {
        val markdown = """
            ## 自定义主题样式 (MarkwonTheme)
            
            当前正在使用 **自定义绿色主题与暗色代码块配置**：
            
            > **定制化引用条 (Custom Blockquote)**
            > 引用条颜色已配置为 Material Green (#4CAF50)，且宽度加粗至 16px。
            
            ### 列表圆点加粗
            - 列表项 1：圆点宽度放大为 20px
            - 列表项 2：段落间距与字体间距已重新优化
            
            ### 代码块与行内代码样式
            行内代码使用了浅蓝背景与深青文字：`val message = "Hello Markwon"`。
            
            多行代码块配置了暗色背景 (#263238) 与淡青色文字 (#80CBC4)：
            
            ```kotlin
            fun initCustomTheme(builder: MarkwonTheme.Builder) {
                builder
                    .blockQuoteColor(Color.parseColor("#4CAF50"))
                    .blockQuoteWidth(16)
                    .codeBlockBackgroundColor(Color.parseColor("#263238"))
                    .codeBlockTextColor(Color.parseColor("#80CBC4"))
                    .bulletWidth(20)
            }
            ```
        """.trimIndent()

        mCustomThemeMarkwon.setMarkdown(mTextView, markdown)
    }

    /**
     * 7. 综合长文档示例
     */
    private fun showComprehensiveMarkdown() {
        val markdown = """
            # 现代 Android AI 流式客户端设计与架构实践
            
            > 随着生成式 AI（Generative AI）技术的普及，在客户端实现流畅、优雅的流式交互体验成为核心竞争力。
            
            ---
            
            ## 一、 为什么传统的 Markdown 渲染会卡顿？
            
            在传统的文本展示中，页面通常是一次性加载并渲染的。而在 **AI SSE 流式对话场景** 中，网络数据是以每秒数十次的频率持续追加推送的：
            
            1. **频繁 AST 解析**：每次收到 3~5 个字符就重新解析整篇几千字的 Markdown 树，计算复杂度呈 O(N²) 递增。
            2. **UI 频繁重排 (Re-layout)**：TextView 持续计算文本测量与行高，造成主线程掉帧。
            3. **语法闪烁**：当 ````kotlin` 或 `**加粗**` 处于未闭合状态时，解析器会将其渲染为普通文本，闭合瞬间突变为格式化样式。
            
            ---
            
            ## 二、 关键技术选型对比
            
            | 技术方案 | 解析性能 | 扩展性 | 流式支持友好度 | 推荐指数 |
            | :--- | :--- | :--- | :--- | :---: |
            | **Markwon (原生)** | 极高（基于 Spannable） | 极高（插件体系完善） | 优秀（支持 Node 局部渲染） | ⭐⭐⭐⭐⭐ |
            | **Compose RichText** | 较高（Compose 声明式） | 中等 | 中等（需拆分 Block 避免重组） | ⭐⭐⭐⭐ |
            | **WebView / WebKit** | 中等（DOM 操作） | 较高（CSS 高度自由） | 较差（跨进程 IPC 通信开销大） | ⭐⭐⭐ |
            
            ---
            
            ## 三、 演进路线任务清单
            
            - [x] **基础设施搭建**：Markwon 核心与扩展插件支持
            - [ ] **语法高亮集成**：Prism4j 异步词法着色
            - [ ] **打字机动态流控**：自适应 Buffer 队列与标点节奏
            - [ ] **聊天列表实战**：RecyclerView Payload 增量刷新与吸底跟随
            
            ---
            
            ## 四、 核心代码片段示例
            
            ```kotlin
            // Markwon 流式配置与解析示例
            val markwon = Markwon.builder(context)
                .usePlugin(CorePlugin.create())
                .usePlugin(TablePlugin.create(context))
                .usePlugin(TaskListPlugin.create(context))
                .usePlugin(HtmlPlugin.create())
                .build()
                
            // 将解析后的 Spannable 绑定至 TextView
            markwon.setMarkdown(textView, markdownContent)
            ```
            
            点击链接探索更多：[深入学习 Markwon 官方文档](https://noties.io/Markwon/)
        """.trimIndent()

        mDefaultMarkwon.setMarkdown(mTextView, markdown)
    }
}
