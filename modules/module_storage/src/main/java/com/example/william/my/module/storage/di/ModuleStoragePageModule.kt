package com.example.william.my.module.storage.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.storage.StorageMainActivity
import com.example.william.my.module.storage.datastore.activity.DataStoreActivity
import com.example.william.my.module.storage.mmkv.activity.MMKVActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_storage 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleStoragePageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Storage.DataStore)
    fun provideDataStoreActivity(): Class<out Activity> = DataStoreActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Storage.MMKV)
    fun provideMMKVActivity(): Class<out Activity> = MMKVActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Storage.Main)
    fun provideStorageMainActivity(): Class<out Activity> = StorageMainActivity::class.java
}
