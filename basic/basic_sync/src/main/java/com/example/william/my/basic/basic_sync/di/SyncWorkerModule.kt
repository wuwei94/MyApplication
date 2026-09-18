package com.example.william.my.basic.basic_sync.di

import android.content.Context
import com.example.william.my.basic.basic_datastore.SyncPreferencesDataSource
import com.example.william.my.basic.basic_sync.Sync
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Sync Worker 依赖装配模块
 *
 * 将游标数据源绑定到 [Sync] 进程内单例：Hilt 注入路径与 ServiceLocator 查找路径共享同一
 * [SyncPreferencesDataSource]，避免 Proto DataStore 同文件被多实例打开。
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncWorkerModule {

    @Provides
    @Singleton
    fun provideSyncPreferencesDataSource(
        @ApplicationContext context: Context,
    ): SyncPreferencesDataSource = Sync.provideSyncPreferencesDataSource(context)
}
