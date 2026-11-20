package com.example.william.my.module.widget.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.widget.WidgetMainActivity
import com.example.william.my.module.widget.activity.AppBarActivity
import com.example.william.my.module.widget.activity.DialogActivity
import com.example.william.my.module.widget.activity.FlexBoxActivity
import com.example.william.my.module.widget.activity.PopWindowActivity
import com.example.william.my.module.widget.activity.RecyclerViewActivity
import com.example.william.my.module.widget.activity.RecyclerViewNestedActivity
import com.example.william.my.module.widget.activity.ViewFlipperActivity
import com.example.william.my.module.widget.activity.ViewPager2Activity
import com.example.william.my.module.widget.activity.ViewPagerActivity
import com.example.william.my.module.widget.activity.WebViewActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_widget 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleWidgetPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Widget.Appbar)
    fun provideAppBarActivity(): Class<out Activity> = AppBarActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Widget.Dialog)
    fun provideDialogActivity(): Class<out Activity> = DialogActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Widget.FlexBox)
    fun provideFlexBoxActivity(): Class<out Activity> = FlexBoxActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Widget.PopWindow)
    fun providePopWindowActivity(): Class<out Activity> = PopWindowActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Widget.RecyclerView)
    fun provideRecyclerViewActivity(): Class<out Activity> = RecyclerViewActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Widget.RecyclerViewNested)
    fun provideRecyclerViewNestedActivity(): Class<out Activity> = RecyclerViewNestedActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Widget.ViewFlipper)
    fun provideViewFlipperActivity(): Class<out Activity> = ViewFlipperActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Widget.ViewPager2)
    fun provideViewPager2Activity(): Class<out Activity> = ViewPager2Activity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Widget.ViewPager)
    fun provideViewPagerActivity(): Class<out Activity> = ViewPagerActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Widget.WebView)
    fun provideWebViewActivity(): Class<out Activity> = WebViewActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Widget.Main)
    fun provideWidgetMainActivity(): Class<out Activity> = WidgetMainActivity::class.java
}
