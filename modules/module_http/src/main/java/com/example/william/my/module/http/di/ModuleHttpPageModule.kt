package com.example.william.my.module.http.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.http.HttpMainActivity
import com.example.william.my.module.http.httpurl.activity.HttpURLActivity
import com.example.william.my.module.http.ktor.activity.KtorActivity
import com.example.william.my.module.http.ktor.activity.KtorClientActivity
import com.example.william.my.module.http.okhttp.activity.OkHttpActivity
import com.example.william.my.module.http.retrofit.activity.RetrofitCallActivity
import com.example.william.my.module.http.retrofit.activity.RetrofitCallDslActivity
import com.example.william.my.module.http.retrofit.activity.RetrofitCoroutineActivity
import com.example.william.my.module.http.retrofit.activity.RetrofitCoroutineDslActivity
import com.example.william.my.module.http.retrofit.activity.RetrofitRxActivity
import com.example.william.my.module.http.retrofit.activity.RetrofitRxDslActivity
import com.example.william.my.module.http.rx_request.activity.RxDownloadActivity
import com.example.william.my.module.http.rx_request.activity.RxRequestActivity
import com.example.william.my.module.http.rx_request.activity.RxUploadActivity
import com.example.william.my.module.http.volley.activity.VolleyActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_http 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleHttpPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.Main)
    fun provideHttpMainActivity(): Class<out Activity> = HttpMainActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.HttpURL)
    fun provideHttpURLActivity(): Class<out Activity> = HttpURLActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.Ktor)
    fun provideKtorActivity(): Class<out Activity> = KtorActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.KtorClient)
    fun provideKtorClientActivity(): Class<out Activity> = KtorClientActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.OkHttp)
    fun provideOkHttpActivity(): Class<out Activity> = OkHttpActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.RetrofitCall)
    fun provideRetrofitCallActivity(): Class<out Activity> = RetrofitCallActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.RetrofitCallDsl)
    fun provideRetrofitCallDslActivity(): Class<out Activity> = RetrofitCallDslActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.RetrofitCoroutine)
    fun provideRetrofitCoroutineActivity(): Class<out Activity> = RetrofitCoroutineActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.RetrofitCoroutineDsl)
    fun provideRetrofitCoroutineDslActivity(): Class<out Activity> = RetrofitCoroutineDslActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.RetrofitRx)
    fun provideRetrofitRxActivity(): Class<out Activity> = RetrofitRxActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.RetrofitRxDsl)
    fun provideRetrofitRxDslActivity(): Class<out Activity> = RetrofitRxDslActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.RxDownload)
    fun provideRxDownloadActivity(): Class<out Activity> = RxDownloadActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.RxRequest)
    fun provideRxRequestActivity(): Class<out Activity> = RxRequestActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.RxUpload)
    fun provideRxUploadActivity(): Class<out Activity> = RxUploadActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Http.Volley)
    fun provideVolleyActivity(): Class<out Activity> = VolleyActivity::class.java
}
