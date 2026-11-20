package com.example.william.my.module.anim.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.anim.AnimMainActivity
import com.example.william.my.module.anim.activity.system.AnimatorSetActivity
import com.example.william.my.module.anim.activity.system.ObjectAnimatorActivity
import com.example.william.my.module.anim.activity.system.TransitionFirstActivity
import com.example.william.my.module.anim.activity.system.TransitionSecondActivity
import com.example.william.my.module.anim.activity.system.ValueAnimatorActivity
import com.example.william.my.module.anim.activity.thirdparty.LottieActivity
import com.example.william.my.module.anim.activity.thirdparty.PagCompositionActivity
import com.example.william.my.module.anim.activity.thirdparty.PagImageActivity
import com.example.william.my.module.anim.activity.thirdparty.PagLayerActivity
import com.example.william.my.module.anim.activity.thirdparty.PagPlaybackActivity
import com.example.william.my.module.anim.activity.thirdparty.PagTextActivity
import com.example.william.my.module.anim.activity.thirdparty.RiveActivity
import com.example.william.my.module.anim.activity.thirdparty.SvgaActivity
import com.example.william.my.module.anim.activity.thirdparty.SvgaImageActivity
import com.example.william.my.module.anim.activity.thirdparty.VapActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_anim 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleAnimPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.AnimatorSet)
    fun provideAnimatorSetActivity(): Class<out Activity> = AnimatorSetActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.ObjectAnimator)
    fun provideObjectAnimatorActivity(): Class<out Activity> = ObjectAnimatorActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.Transition)
    fun provideTransitionFirstActivity(): Class<out Activity> = TransitionFirstActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.Transition2)
    fun provideTransitionSecondActivity(): Class<out Activity> = TransitionSecondActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.ValueAnimator)
    fun provideValueAnimatorActivity(): Class<out Activity> = ValueAnimatorActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.Lottie)
    fun provideLottieActivity(): Class<out Activity> = LottieActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.PagComposition)
    fun providePagCompositionActivity(): Class<out Activity> = PagCompositionActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.PagImage)
    fun providePagImageActivity(): Class<out Activity> = PagImageActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.PagLayer)
    fun providePagLayerActivity(): Class<out Activity> = PagLayerActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.PagPlayback)
    fun providePagPlaybackActivity(): Class<out Activity> = PagPlaybackActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.PagText)
    fun providePagTextActivity(): Class<out Activity> = PagTextActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.Rive)
    fun provideRiveActivity(): Class<out Activity> = RiveActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.Svga)
    fun provideSvgaActivity(): Class<out Activity> = SvgaActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.SvgaImage)
    fun provideSvgaImageActivity(): Class<out Activity> = SvgaImageActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.Vap)
    fun provideVapActivity(): Class<out Activity> = VapActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Anim.Main)
    fun provideAnimMainActivity(): Class<out Activity> = AnimMainActivity::class.java
}
