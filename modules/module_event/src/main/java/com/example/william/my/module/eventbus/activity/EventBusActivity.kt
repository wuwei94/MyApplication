package com.example.william.my.module.eventbus.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicRecyclerActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.eventbus.EventBusHelper
import com.example.william.my.module.eventbus.event.GlobalEvent
import com.example.william.my.module.eventbus.event.StickyEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 延迟发送    有序接收    Sticky    生命感知    可跨进程    线程分发
 * ❌        ✅        ✅        ❌        ❌        ✅
 */
@Route(path = RouterPath.Event.EventBus)
class EventBusActivity : BasicRecyclerActivity() {

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "registerEventBus",
            "postEventBus",
            "postStickyEventBus",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                registerEventBus()
            }

            1 -> {
                EventBusHelper.postEvent(
                    GlobalEvent("EventBus post by Activity")
                )
            }

            2 -> {
                EventBusHelper.postStickyEvent(
                    StickyEvent("EventBus postSticky by Activity")
                )
            }

        }
    }

    private fun registerEventBus() {
        if (!EventBusHelper.isRegistered(this@EventBusActivity)) {
            EventBusHelper.register(this@EventBusActivity)
            showEventMessage("EventBus register")
        } else {
            EventBusHelper.unregister(this@EventBusActivity)
            showEventMessage("EventBus unregister")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGlobalEvent(event: GlobalEvent) {
        showEventMessage(event.message)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onStickyEvent(event: StickyEvent) {
        showEventMessage(event.message)
    }
}