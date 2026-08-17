package com.example.william.my.module.eventbus.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.eventbus.livedata.LiveEventBus
import com.example.william.my.module.eventbus.event.GlobalEvent
import com.example.william.my.module.eventbus.event.StickyEvent

/**
 * LiveData 事件总线示例
 *
 * 演示基于 LiveData + ViewModel 实现的事件总线。
 *
 * 特性对比：
 * 延迟发送: ✅ | 有序接收: ✅ | Sticky: ✅ | 生命周期感知: ✅ | 可跨进程: ✅ | 线程分发: ❌
 *
 * 特性说明：
 * LiveData 具备原生生命周期感知能力，页面销毁后自动取消订阅，无需手动解绑。
 */
@Route(path = RouterPath.Event.LiveEventBus)
class LiveEventBusActivity : BasicResponseActivity() {

    private var isObserving = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("LiveEventBus 示例\n\n请点击下方按钮注册监听或发送事件")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "开启监听 (Observe)",
            "发送普通事件 (Post Event)",
            "发送粘性事件 (Post Sticky Event)",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> observeLiveEventBus()
            1 -> {
                appendLog("发送普通事件：GlobalEvent")
                LiveEventBus.postEvent(
                    this,
                    GlobalEvent("LiveEventBus post by Activity")
                )
            }
            2 -> {
                appendLog("发送粘性事件：StickyEvent")
                LiveEventBus.postEvent(
                    this,
                    StickyEvent("LiveEventBus postSticky by Activity")
                )
            }
        }
    }

    private fun observeLiveEventBus() {
        if (isObserving) {
            appendLog("LiveEventBus 已处于监听状态")
            return
        }
        LiveEventBus.observeEvent<GlobalEvent>(this) {
            appendLog("收到普通事件：${it.message}")
        }
        LiveEventBus.observeEvent<StickyEvent>(this, isSticky = true) {
            appendLog("收到粘性事件：${it.message}")
        }
        isObserving = true
        appendLog("已开启 LiveEventBus 监听")
    }
}