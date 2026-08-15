package com.example.william.my.core.retrofit.rx.api

import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

class SingleExtensionsTest {

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
        RxJavaPlugins.reset()
    }

    @Test
    fun withNetworkDefaultsConvertsBusinessFailureToApiException() {
        val response = RetrofitResponse.of<String>(2001, "业务失败", null)

        Single.just(response)
            .withNetworkDefaults()
            .test()
            .assertError { error ->
                error is ApiException &&
                    error.code == 2001 &&
                    error.message == "业务失败"
            }
    }
}
