package com.example.william.my.module.router

import android.app.Activity
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.router.arouter.activity.ARouterActivity
import com.example.william.my.module.router.custom_router.activity.CustomRouterActivity
import com.example.william.my.module.router.custom_router.activity.CustomRouterTargetActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * 组件化路由横评入口 — 导航到 ARouter 与自研无插件路由对比示例。
 */
@Route(path = RouterPath.Router.Main)
class RouterMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 插件式/注解式路由横评 ──", ""))
        items.add(RouterItem("ARouter（经典框架：KAPT + @Route）", RouterPath.Router.ARouter))
        items.add(RouterItem("CustomRouter（自研无插件：Hilt @IntoMap + 责任链）", RouterPath.Router.CustomRouter))
        return items
    }
}

/**
 * 通过 Hilt Multibinding 在编译期自动向全局 [AppPageRegistry] 注册本模块入口与自研路由示例页。
 */
@Module
@InstallIn(SingletonComponent::class)
object RouterPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Router.Main)
    fun provideRouterMain(): Class<out Activity> = RouterMainActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Router.ARouter)
    fun provideARouter(): Class<out Activity> = ARouterActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Router.CustomRouter)
    fun provideCustomRouter(): Class<out Activity> = CustomRouterActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Router.CustomRouterTarget)
    fun provideCustomRouterTarget(): Class<out Activity> = CustomRouterTargetActivity::class.java
}
