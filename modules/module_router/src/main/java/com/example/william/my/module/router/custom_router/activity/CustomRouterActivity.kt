package com.example.william.my.module.router.custom_router.activity

import android.os.Bundle
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.navigator.AppNavigator
import com.example.william.my.basic.basic_shared.navigator.AppPageRegistry
import com.example.william.my.basic.basic_shared.navigator.PageInterceptor
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.router.service.utils.ResourceUtilsService
import com.example.william.my.module.router.RouterPageModule
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 自研无插件路由示例（Hilt `@IntoMap` + 责任链拦截器）。
 *
 * 对比组：
 * - [com.example.william.my.module.router.arouter.activity.ARouterActivity]（KAPT + @Route + @Autowired + IProvider）
 * - [CustomRouterActivity]（自研无插件 + Hilt @IntoMap + 责任链）
 */
@AndroidEntryPoint
class CustomRouterActivity : BasicResponseActivity() {

    @Inject
    lateinit var resourceService: ResourceUtilsService

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "自研无插件路由示例\n" +
                "不依赖 KAPT 与字节码插件，通过 Hilt @IntoMap 编译期聚合路由表，" +
                "配合责任链拦截器与 Hilt 服务注入完成同构能力。",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 页面跳转（AppNavigator + 携带参数）",
        "2. 注册并触发拦截器链（PageInterceptor）",
        "3. 跨模块服务发现（Hilt 编译期注入）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> navigateWithParams()
            1 -> navigateWithInterceptor()
            2 -> discoverService()
        }
    }

    private fun navigateWithParams() {
        appendLog("→ AppNavigator 发起跳转: ${RouterPath.Router.CustomRouterTarget}")
        val ok = AppNavigator.build(RouterPath.Router.CustomRouterTarget)
            .withString("from", "CustomRouterActivity")
            .withInt("code", 200)
            .navigation(this)
        if (ok) {
            appendLog("✓ 命中 Hilt 路由表并完成跳转: ${RouterPageModule::class.simpleName}")
        } else {
            appendLog("✗ 未在 AppPageRegistry 找到目标页")
        }
    }

    private fun navigateWithInterceptor() {
        val logInterceptor = object : PageInterceptor {
            override val priority: Int = 10

            override fun intercept(chain: PageInterceptor.Chain) {
                if (chain.request.path == RouterPath.Router.CustomRouterTarget) {
                    appendLog("→ 拦截器命中(${chain.request.path})，附加 traceId 后放行")
                    chain.request.extras.putString("traceId", "custom-trace-001")
                }
                chain.proceed(chain.request)
            }
        }
        AppPageRegistry.addInterceptor(logInterceptor)
        AppNavigator.build(RouterPath.Router.CustomRouterTarget)
            .withString("from", "CustomRouterActivity(Intercepted)")
            .withInt("code", 201)
            .navigation(this)
    }

    private fun discoverService() {
        appendLog("→ 读取 Hilt 编译期注入服务: ResourceUtilsService")
        appendLog("✓ 注入成功(零反射): ${resourceService::class.java.simpleName}")
    }
}
