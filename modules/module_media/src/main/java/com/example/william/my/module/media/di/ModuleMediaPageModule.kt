package com.example.william.my.module.media.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.media.MediaMainActivity
import com.example.william.my.module.media.activity.MediaPhotoActivity
import com.example.william.my.module.media.activity.MediaVideoActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_media 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleMediaPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Media.Photo)
    fun provideMediaPhotoActivity(): Class<out Activity> = MediaPhotoActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Media.Video)
    fun provideMediaVideoActivity(): Class<out Activity> = MediaVideoActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Media.Main)
    fun provideMediaMainActivity(): Class<out Activity> = MediaMainActivity::class.java
}
