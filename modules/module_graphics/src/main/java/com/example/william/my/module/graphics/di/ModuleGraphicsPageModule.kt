package com.example.william.my.module.graphics.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.graphics.GraphicsMainActivity
import com.example.william.my.module.graphics.activity.RenderEffectActivity
import com.example.william.my.module.graphics.activity.RenderScriptActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_graphics 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleGraphicsPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Graphics.RenderEffect)
    fun provideRenderEffectActivity(): Class<out Activity> = RenderEffectActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Graphics.RenderScript)
    fun provideRenderScriptActivity(): Class<out Activity> = RenderScriptActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Graphics.Main)
    fun provideGraphicsMainActivity(): Class<out Activity> = GraphicsMainActivity::class.java
}
