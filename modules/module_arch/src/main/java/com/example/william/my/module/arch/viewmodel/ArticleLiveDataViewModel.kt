/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.william.my.module.arch.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.core.retrofit.exception.ExceptionHandler
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.module.arch.usecase.ArticleUseCase
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import kotlinx.coroutines.launch

class ArticleLiveDataViewModel(
    private val repository: ArticleRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _articleResponse = MutableLiveData<RetrofitResponse<ArticleData>>()
    val articleResponse: LiveData<RetrofitResponse<ArticleData>>
        get() = _articleResponse

    /**
     * 协程挂起函数：手动执行并 postValue
     */
    fun loadArticle(page: Int) {
        viewModelScope.launch {
            _articleResponse.value = RetrofitResponse.loading()
            try {
                val response = repository.getArticleSuspend(page)
                _articleResponse.value = response
            } catch (e: Exception) {
                _articleResponse.value = RetrofitResponse.error(e.message ?: "网络请求失败")
            }
        }
    }

    /**
     * RxJava3 Single：手动订阅并 postValue
     */
    fun loadArticle2(page: Int) {
        val disposable = repository.getArticleSingle(page)
            .subscribe({ response ->
                _articleResponse.postValue(response)
            }, { error ->
                _articleResponse.postValue(RetrofitResponse.error(error.message ?: "网络请求失败"))
            })
        compositeDisposable.add(disposable)
    }

    private val articleUseCase: ArticleUseCase = ArticleUseCase(repository)

    /**
     * UseCase
     */
    fun loadArticle3(page: Int) {
        articleUseCase.setPage(page)
        articleUseCase.execute(object :
            DisposableSingleObserver<RetrofitResponse<ArticleData>>() {
            override fun onSuccess(response: RetrofitResponse<ArticleData>) {
                _articleResponse.postValue(response)
            }

            override fun onError(e: Throwable) {
                val exception = ExceptionHandler.handleException(e)
                _articleResponse.postValue(RetrofitResponse.error(exception.message))
            }
        })
    }

    /**
     * 销毁时调用
     */
    override fun onCleared() {
        super.onCleared()
        articleUseCase.clear()
        compositeDisposable.clear()
    }
}

