package com.example.william.my.module.tab.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.tab.TabMainActivity
import com.example.william.my.module.tab.activity.BottomNavigationActivity
import com.example.william.my.module.tab.activity.FlycoTabLayoutActivity
import com.example.william.my.module.tab.activity.FrameLayoutTabActivity
import com.example.william.my.module.tab.activity.TabHostActivity
import com.example.william.my.module.tab.activity.ViewPager2TabActivity
import com.example.william.my.module.tab.activity.ViewPagerTabActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_tab 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleTabPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Tab.BottomNavigation)
    fun provideBottomNavigationActivity(): Class<out Activity> = BottomNavigationActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Tab.FlycoTabLayout)
    fun provideFlycoTabLayoutActivity(): Class<out Activity> = FlycoTabLayoutActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Tab.FrameLayoutTab)
    fun provideFrameLayoutTabActivity(): Class<out Activity> = FrameLayoutTabActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Tab.TabHost)
    fun provideTabHostActivity(): Class<out Activity> = TabHostActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Tab.ViewPager2Tab)
    fun provideViewPager2TabActivity(): Class<out Activity> = ViewPager2TabActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Tab.ViewPagerTab)
    fun provideViewPagerTabActivity(): Class<out Activity> = ViewPagerTabActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Tab.Main)
    fun provideTabMainActivity(): Class<out Activity> = TabMainActivity::class.java
}
