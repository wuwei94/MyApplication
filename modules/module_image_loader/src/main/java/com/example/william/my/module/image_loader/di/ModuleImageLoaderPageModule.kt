package com.example.william.my.module.image_loader.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.image_loader.ImageLoaderMainActivity
import com.example.william.my.module.image_loader.coil.activity.CoilActivity
import com.example.william.my.module.image_loader.glide.activity.GlideActivity
import com.example.william.my.module.image_loader.imageloader.activity.LibImageLoaderActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_image_loader 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleImageLoaderPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.ImageLoader.Coil)
    fun provideCoilActivity(): Class<out Activity> = CoilActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.ImageLoader.Glide)
    fun provideGlideActivity(): Class<out Activity> = GlideActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.ImageLoader.LibImageLoader)
    fun provideLibImageLoaderActivity(): Class<out Activity> = LibImageLoaderActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.ImageLoader.Main)
    fun provideImageLoaderMainActivity(): Class<out Activity> = ImageLoaderMainActivity::class.java
}
