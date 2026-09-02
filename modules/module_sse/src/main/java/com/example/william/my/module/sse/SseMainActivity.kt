package com.example.william.my.module.sse

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * SSE 流式传输（DeepSeek AI 大模型对话）示例入口
 *
 * 演示现代 AI 大模型标准的 POST + SSE 流式响应协议：
 * - OkHttp SSE（普通版本 / RxJava 封装 / Coroutines Flow 封装）
 * - Ktor SSE（普通版本 / Coroutines Flow 封装）
 */
@Route(path = RouterPath.SSE.Main)
class SseMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = arrayListOf<RouterItem>()
        routerItems.add(RouterItem("── OkHttp SSE ──", ""))
        routerItems.add(RouterItem("OkHttp SSE（Listener 回调）", RouterPath.SSE.OkHttpSseClient))
        routerItems.add(RouterItem("OkHttp SSE（RxJava 封装）", RouterPath.SSE.OkHttpSseClientRx))
        routerItems.add(RouterItem("OkHttp SSE（Flow 封装）", RouterPath.SSE.OkHttpSseClientFlow))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── Ktor SSE ──", ""))
        routerItems.add(RouterItem("Ktor SSE（Listener 回调）", RouterPath.SSE.KtorSseClient))
        routerItems.add(RouterItem("Ktor SSE（Flow 封装）", RouterPath.SSE.KtorSseClientFlow))
        return routerItems
    }
}
