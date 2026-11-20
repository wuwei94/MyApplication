package com.example.william.my.module.gpuimage.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.gpuimage.GpuImageMainActivity
import com.example.william.my.module.gpuimage.adjust.GpuImageAdjustActivity
import com.example.william.my.module.gpuimage.camera.GpuImageCameraFilterActivity
import com.example.william.my.module.gpuimage.codec.GpuImageCodecRecordActivity
import com.example.william.my.module.gpuimage.effect.GpuImageCameraEffectActivity
import com.example.william.my.module.gpuimage.filter.GpuImageFilterActivity
import com.example.william.my.module.gpuimage.group.GpuImageGroupActivity
import com.example.william.my.module.gpuimage.photo.GpuImagePhotoActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_gpuimage 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleGpuimagePageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.GpuImage.Adjust)
    fun provideGpuImageAdjustActivity(): Class<out Activity> = GpuImageAdjustActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.GpuImage.CameraFilter)
    fun provideGpuImageCameraFilterActivity(): Class<out Activity> = GpuImageCameraFilterActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.GpuImage.CodecRecord)
    fun provideGpuImageCodecRecordActivity(): Class<out Activity> = GpuImageCodecRecordActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.GpuImage.CameraEffect)
    fun provideGpuImageCameraEffectActivity(): Class<out Activity> = GpuImageCameraEffectActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.GpuImage.Filter)
    fun provideGpuImageFilterActivity(): Class<out Activity> = GpuImageFilterActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.GpuImage.Main)
    fun provideGpuImageMainActivity(): Class<out Activity> = GpuImageMainActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.GpuImage.Group)
    fun provideGpuImageGroupActivity(): Class<out Activity> = GpuImageGroupActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.GpuImage.Photo)
    fun provideGpuImagePhotoActivity(): Class<out Activity> = GpuImagePhotoActivity::class.java
}
