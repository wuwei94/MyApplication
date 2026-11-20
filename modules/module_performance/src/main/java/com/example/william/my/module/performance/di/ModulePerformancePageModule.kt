package com.example.william.my.module.performance.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.performance.PerformanceMainActivity
import com.example.william.my.module.performance.activity.AnchorTaskActivity
import com.example.william.my.module.performance.activity.AppStartupActivity
import com.example.william.my.module.performance.activity.AsyncLayoutInflaterActivity
import com.example.william.my.module.performance.activity.BaselineProfilesActivity
import com.example.william.my.module.performance.activity.ConcatAdapterActivity
import com.example.william.my.module.performance.activity.ContentProviderActivity
import com.example.william.my.module.performance.activity.DiffUtilActivity
import com.example.william.my.module.performance.activity.IdleHandlerActivity
import com.example.william.my.module.performance.activity.JankStatsActivity
import com.example.william.my.module.performance.activity.LruCacheActivity
import com.example.william.my.module.performance.activity.RecycledViewPoolActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_performance 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModulePerformancePageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Performance.AnchorTask)
    fun provideAnchorTaskActivity(): Class<out Activity> = AnchorTaskActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Performance.AppStartup)
    fun provideAppStartupActivity(): Class<out Activity> = AppStartupActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Performance.AsyncLayoutInflater)
    fun provideAsyncLayoutInflaterActivity(): Class<out Activity> = AsyncLayoutInflaterActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Performance.BaselineProfiles)
    fun provideBaselineProfilesActivity(): Class<out Activity> = BaselineProfilesActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Performance.ConcatAdapter)
    fun provideConcatAdapterActivity(): Class<out Activity> = ConcatAdapterActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Performance.ContentProvider)
    fun provideContentProviderActivity(): Class<out Activity> = ContentProviderActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Performance.DiffUtil)
    fun provideDiffUtilActivity(): Class<out Activity> = DiffUtilActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Performance.IdleHandler)
    fun provideIdleHandlerActivity(): Class<out Activity> = IdleHandlerActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Performance.JankStats)
    fun provideJankStatsActivity(): Class<out Activity> = JankStatsActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Performance.LruCache)
    fun provideLruCacheActivity(): Class<out Activity> = LruCacheActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Performance.RecycledViewPool)
    fun provideRecycledViewPoolActivity(): Class<out Activity> = RecycledViewPoolActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Performance.Main)
    fun providePerformanceMainActivity(): Class<out Activity> = PerformanceMainActivity::class.java
}
