package com.example.william.my.basic.basic_shared.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.category.CategoryActivity
import com.example.william.my.basic.basic_shared.category.DirectoryActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * shared 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object SharedPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Category_Main)
    fun provideCategoryActivity(): Class<out Activity> = CategoryActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Directory_Main)
    fun provideDirectoryActivity(): Class<out Activity> = DirectoryActivity::class.java
}
