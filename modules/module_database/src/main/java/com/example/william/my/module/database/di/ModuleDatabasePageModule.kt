package com.example.william.my.module.database.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.database.DatabaseMainActivity
import com.example.william.my.module.database.objectbox.activity.ObjectBoxActivity
import com.example.william.my.module.database.room.activity.RoomActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_database 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleDatabasePageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Database.Main)
    fun provideDatabaseMainActivity(): Class<out Activity> = DatabaseMainActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Database.ObjectBox)
    fun provideObjectBoxActivity(): Class<out Activity> = ObjectBoxActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Database.Room)
    fun provideRoomActivity(): Class<out Activity> = RoomActivity::class.java
}
