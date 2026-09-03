package com.example.william.my.module.arch.mvvm.usecase

import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.core.base.arch.rx.SingleObserverUseCase
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.core.Single

/**
 * 文章业务用例
 *
 * 基于 RxJava Single 封装文章列表请求业务用例。
 */
class ArticleUseCase(private val repository: ArticleRepository) :
    SingleObserverUseCase<RetrofitResponse<ArticleData>>() {

    private var page = 0
    fun setPage(page: Int) {
        this.page = page
    }

    override fun buildUseCaseObservable(): Single<RetrofitResponse<ArticleData>> {
        return repository.getArticleSingle(page)
    }
}
