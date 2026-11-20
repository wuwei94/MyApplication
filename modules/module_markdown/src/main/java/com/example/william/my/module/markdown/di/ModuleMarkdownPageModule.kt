package com.example.william.my.module.markdown.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.markdown.MarkdownMainActivity
import com.example.william.my.module.markdown.activity.AiChatActivity
import com.example.william.my.module.markdown.activity.MarkwonBasicActivity
import com.example.william.my.module.markdown.activity.MarkwonHighlightActivity
import com.example.william.my.module.markdown.activity.StreamTypewriterActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_markdown 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleMarkdownPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Markdown.AiChat)
    fun provideAiChatActivity(): Class<out Activity> = AiChatActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Markdown.MarkwonBasic)
    fun provideMarkwonBasicActivity(): Class<out Activity> = MarkwonBasicActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Markdown.MarkwonHighlight)
    fun provideMarkwonHighlightActivity(): Class<out Activity> = MarkwonHighlightActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Markdown.StreamTypewriter)
    fun provideStreamTypewriterActivity(): Class<out Activity> = StreamTypewriterActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Markdown.Main)
    fun provideMarkdownMainActivity(): Class<out Activity> = MarkdownMainActivity::class.java
}
