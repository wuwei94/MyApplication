package com.example.william.my.module.arch.mvvm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.core.retrofit.exception.ExceptionHandler
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.module.arch.mvvm.usecase.ArticleUseCase
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import kotlinx.coroutines.launch

/**
 * 文章列表 LiveData ViewModel
 *
 * 演示 MVVM 模式中通过 LiveData 暴露状态，支持协程、RxJava 与 UseCase 方式加载数据。
 */
class ArticleLiveDataViewModel(
    private val repository: ArticleRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _articleResponse = MutableLiveData<RetrofitResponse<ArticleData>>()
    val articleResponse: LiveData<RetrofitResponse<ArticleData>>
        get() = _articleResponse

    /**
     * 默认加载方式：调用协程挂起函数
     */
    fun loadArticle(page: Int) {
        loadArticleBySuspend(page)
    }

    /**
     * 协程挂起函数：手动执行并 postValue
     */
    fun loadArticleBySuspend(page: Int) {
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
    fun loadArticleByRx(page: Int) {
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
     * UseCase：使用独立业务用例执行请求
     */
    fun loadArticleByUseCase(page: Int) {
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

    companion object {
        /**
         * 工厂：通过 [viewModelFactory] DSL 从 [CreationExtras] 获取 Application 并注入仓库
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                )
                ArticleLiveDataViewModel(
                    ServiceLocator.provideArticleRepository(application)
                )
            }
        }
    }
}
