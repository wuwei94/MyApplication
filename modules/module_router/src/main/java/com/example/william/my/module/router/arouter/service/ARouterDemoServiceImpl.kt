package com.example.william.my.module.router.arouter.service

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route

/**
 * ARouter 独立演示用 IProvider 服务实现。
 */
@Route(path = "/Router/ARouterDemoService")
class ARouterDemoServiceImpl : ARouterDemoService {

    override fun getMessage(): String = "ARouterDemoServiceImpl (via IProvider)"

    override fun init(context: Context) {}
}
