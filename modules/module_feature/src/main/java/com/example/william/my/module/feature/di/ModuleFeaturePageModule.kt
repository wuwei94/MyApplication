package com.example.william.my.module.feature.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.feature.FeatureMainActivity
import com.example.william.my.module.feature.activity.CropActivity
import com.example.william.my.module.feature.activity.MicAnimationActivity
import com.example.william.my.module.feature.activity.TurntableActivity
import com.example.william.my.module.feature.activity.WeakNetworkActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_feature 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleFeaturePageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Feature.Crop)
    fun provideCropActivity(): Class<out Activity> = CropActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Feature.MicAnimation)
    fun provideMicAnimationActivity(): Class<out Activity> = MicAnimationActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Feature.Turntable)
    fun provideTurntableActivity(): Class<out Activity> = TurntableActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Feature.WeakNetwork)
    fun provideWeakNetworkActivity(): Class<out Activity> = WeakNetworkActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Feature.Main)
    fun provideFeatureMainActivity(): Class<out Activity> = FeatureMainActivity::class.java
}
