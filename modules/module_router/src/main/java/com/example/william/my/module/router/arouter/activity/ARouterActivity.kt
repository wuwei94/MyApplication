package com.example.william.my.module.router.arouter.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.alibaba.android.arouter.launcher.ARouter
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.router.arouter.service.ARouterDemoService

/**
 * ARouter — 组件化路由示例（经典第三方框架对照）
 *
 * 核心机制与避坑点：
 * 1. 编译期注解扫描：依赖 KAPT 在编译期扫描 `@Route` / `@Autowired`，生成路由映射表与注入辅助类
 * 2. 运行期反射/服务发现：通过 `IProvider` 实现跨模块服务查找，需在 Application 初始化 `ARouter.init`
 *
 * 对比组：
 * - [ARouterActivity]（KAPT + @Route + @Autowired + IProvider）
 * - [com.example.william.my.module.router.custom_router.activity.CustomRouterActivity]（自研无插件 + Hilt @IntoMap + 责任链）
 *
 * 官方参考：
 * https://github.com/alibaba/ARouter
 */
@Route(path = RouterPath.Router.ARouter)
class ARouterActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        ARouter.init(application)
        showDescription(
            "ARouter 经典路由示例\n" +
                "依赖 KAPT 编译期扫描 @Route / @Autowired 生成路由表，" +
                "并通过 IProvider 反射查找跨模块服务。",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 页面跳转（ARouter.build + 携带参数）",
        "2. 跨模块服务发现（IProvider 反射查找）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> navigateWithParams()
            1 -> discoverService()
        }
    }

    private fun navigateWithParams() {
        appendLog("→ ARouter 发起跳转: ${RouterPath.Router.ARouterTarget}")
        ARouter.getInstance()
            .build(RouterPath.Router.ARouterTarget)
            .withString("from", "ARouterActivity")
            .withInt("code", 200)
            .navigation(
                this,
                object : NavigationCallback {
                    override fun onFound(postcard: Postcard) {
                        appendLog("✓ 命中路由: ${postcard.path}")
                    }

                    override fun onLost(postcard: Postcard) {
                        appendLog("✗ 未找到路由: ${postcard.path}")
                    }

                    override fun onArrival(postcard: Postcard) {
                        appendLog("✓ 到达目标页: ${postcard.path}")
                    }

                    override fun onInterrupt(postcard: Postcard) {
                        appendLog("✗ 被拦截器中断: ${postcard.path}")
                    }
                },
            )
    }

    private fun discoverService() {
        appendLog("→ 查找 ARouter IProvider: ARouterDemoService")
        val service = ARouter.getInstance().navigation(ARouterDemoService::class.java)
        if (service != null) {
            appendLog("✓ 获取服务成功: ${service.getMessage()}")
        } else {
            appendLog("✗ 未找到对应 IProvider 实现")
        }
    }
}
