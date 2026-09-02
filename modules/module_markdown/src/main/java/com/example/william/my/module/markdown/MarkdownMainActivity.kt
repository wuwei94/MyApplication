package com.example.william.my.module.markdown

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Markdown 富文本渲染与 AI 流式交互模块入口
 *
 * 演进体系：
 * 1. Markwon 基础与扩展语法渲染（标题 / 引用 / 表格 / 任务清单 / HTML / 图片 / 主题）
 * 2. Prism4j 多语言代码语法高亮
 * 3. 流式打字机与未闭合语法容错引擎
 * 4. AI 聊天完整实战（RecyclerView Payload 局部增量刷新 + 智能吸底）
 */
@Route(path = RouterPath.Markdown.Main)
class MarkdownMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = arrayListOf<RouterItem>()
        routerItems.add(RouterItem("── 基础与扩展语法 ──", ""))
        routerItems.add(RouterItem("Markwon 基础与扩展渲染", RouterPath.Markdown.MarkwonBasic))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 代码高亮与主题 ──", ""))
        routerItems.add(RouterItem("Prism4j 多语言代码高亮", RouterPath.Markdown.MarkwonHighlight))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── 流式打字机 ──", ""))
        routerItems.add(RouterItem("流式打字机与语法容错（待接入）", ""))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── AI 聊天实战 ──", ""))
        routerItems.add(RouterItem("AI 流式对话完整界面（待接入）", ""))
        return routerItems
    }
}
