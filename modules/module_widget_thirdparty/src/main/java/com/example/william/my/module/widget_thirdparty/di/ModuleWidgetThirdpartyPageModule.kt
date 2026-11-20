package com.example.william.my.module.widget_thirdparty.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.widget_thirdparty.WidgetThirdpartyMainActivity
import com.example.william.my.module.widget_thirdparty.activity.loadsir.LoadSirActivity
import com.example.william.my.module.widget_thirdparty.activity.loadsir.LoadSirFragmentActivity
import com.example.william.my.module.widget_thirdparty.activity.picker.CityPickerActivity
import com.example.william.my.module.widget_thirdparty.activity.picker.PickerViewActivity
import com.example.william.my.module.widget_thirdparty.activity.picker.PictureSelectorActivity
import com.example.william.my.module.widget_thirdparty.activity.widget.BannerActivity
import com.example.william.my.module.widget_thirdparty.activity.widget.BarrageActivity
import com.example.william.my.module.widget_thirdparty.activity.widget.CountdownActivity
import com.example.william.my.module.widget_thirdparty.activity.widget.PhotoViewActivity
import com.example.william.my.module.widget_thirdparty.activity.widget.RealtimeBlurViewActivity
import com.example.william.my.module.widget_thirdparty.activity.widget.ShadowLayoutActivity
import com.example.william.my.module.widget_thirdparty.activity.widget.SwipeLayoutActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_widget_thirdparty 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleWidgetThirdpartyPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetThirdparty.LoadSir)
    fun provideLoadSirActivity(): Class<out Activity> = LoadSirActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetThirdparty.LoadSirFragment)
    fun provideLoadSirFragmentActivity(): Class<out Activity> = LoadSirFragmentActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetThirdparty.CityPicker)
    fun provideCityPickerActivity(): Class<out Activity> = CityPickerActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetThirdparty.PickerView)
    fun providePickerViewActivity(): Class<out Activity> = PickerViewActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetThirdparty.PictureSelector)
    fun providePictureSelectorActivity(): Class<out Activity> = PictureSelectorActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetThirdparty.Banner)
    fun provideBannerActivity(): Class<out Activity> = BannerActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetThirdparty.Barrage)
    fun provideBarrageActivity(): Class<out Activity> = BarrageActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetThirdparty.CountdownView)
    fun provideCountdownActivity(): Class<out Activity> = CountdownActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetThirdparty.PhotoView)
    fun providePhotoViewActivity(): Class<out Activity> = PhotoViewActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetThirdparty.RealtimeBlurView)
    fun provideRealtimeBlurViewActivity(): Class<out Activity> = RealtimeBlurViewActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetThirdparty.ShadowLayout)
    fun provideShadowLayoutActivity(): Class<out Activity> = ShadowLayoutActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetThirdparty.SwipeLayout)
    fun provideSwipeLayoutActivity(): Class<out Activity> = SwipeLayoutActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.WidgetThirdparty.Main)
    fun provideWidgetThirdpartyMainActivity(): Class<out Activity> = WidgetThirdpartyMainActivity::class.java
}
