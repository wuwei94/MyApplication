package com.example.william.my.module.router.arouter.service

import com.alibaba.android.arouter.facade.template.IProvider

/**
 * ARouter 独立演示用 IProvider 服务接口（仅用于 `module_router` 横评对比）。
 */
interface ARouterDemoService : IProvider {

    fun getMessage(): String
}
