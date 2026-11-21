package com.example.william.my.module.widget_custom.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.widget_custom.WidgetCustomMainActivity
import com.example.william.my.module.widget_custom.activity.AlertDialogActivity
import com.example.william.my.module.widget_custom.activity.BlurViewActivity
import com.example.william.my.module.widget_custom.activity.CustomPopWindowActivity
import com.example.william.my.module.widget_custom.activity.InfiniteImageActivity
import com.example.william.my.module.widget_custom.activity.MarqueeViewActivity
import com.example.william.my.module.widget_custom.activity.NinePatchActivity
import com.example.william.my.module.widget_custom.activity.Sensor3DActivity
import com.example.william.my.module.widget_custom.activity.SpinnerActivity
import com.example.william.my.module.widget_custom.activity.TitleBarActivity
import com.example.william.my.module.widget_custom.activity.VerifyCodeActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_widget_custom 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleWidgetCustomPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetCustom.AlertDialog)
    fun provideAlertDialogActivity(): Class<out Activity> = AlertDialogActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetCustom.BlurView)
    fun provideBlurViewActivity(): Class<out Activity> = BlurViewActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetCustom.CustomPopWindow)
    fun provideCustomPopWindowActivity(): Class<out Activity> = CustomPopWindowActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetCustom.InfiniteImage)
    fun provideInfiniteImageActivity(): Class<out Activity> = InfiniteImageActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetCustom.MarqueeView)
    fun provideMarqueeViewActivity(): Class<out Activity> = MarqueeViewActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetCustom.NinePatch)
    fun provideNinePatchActivity(): Class<out Activity> = NinePatchActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetCustom.Sensor3DView)
    fun provideSensor3DActivity(): Class<out Activity> = Sensor3DActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetCustom.Spinner)
    fun provideSpinnerActivity(): Class<out Activity> = SpinnerActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetCustom.TitleBar)
    fun provideTitleBarActivity(): Class<out Activity> = TitleBarActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetCustom.VerifyCode)
    fun provideVerifyCodeActivity(): Class<out Activity> = VerifyCodeActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetCustom.Main)
    fun provideWidgetCustomMainActivity(): Class<out Activity> = WidgetCustomMainActivity::class.java
}
