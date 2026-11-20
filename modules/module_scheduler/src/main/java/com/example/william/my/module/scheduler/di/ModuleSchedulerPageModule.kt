package com.example.william.my.module.scheduler.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.scheduler.SchedulerMainActivity
import com.example.william.my.module.scheduler.activity.JobSchedulerActivity
import com.example.william.my.module.scheduler.activity.SyncWorkerParallelActivity
import com.example.william.my.module.scheduler.activity.WorkManagerActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_scheduler 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleSchedulerPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Scheduler.JobScheduler)
    fun provideJobSchedulerActivity(): Class<out Activity> = JobSchedulerActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Scheduler.SyncWorkerParallel)
    fun provideSyncWorkerParallelActivity(): Class<out Activity> = SyncWorkerParallelActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Scheduler.WorkManager)
    fun provideWorkManagerActivity(): Class<out Activity> = WorkManagerActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Scheduler.Main)
    fun provideSchedulerMainActivity(): Class<out Activity> = SchedulerMainActivity::class.java
}
