package com.example.william.my.module.arch.mvp.presenter

import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.module.arch.mvp.contract.ArticleContract

/**
 * 文章列表 Presenter
 *
 * 响应 UI 层操作，从数据仓库获取数据并更新视图。
 */
class ArticlePresenter(
    private val repository: ArticleRepository,
    private var view: ArticleContract.View?
) : ArticleContract.Presenter {

    override fun loadArticle(page: Int) {
        repository.getArticleCallback(
            page,
            object : ArticleRepository.LoadArticleCallback {
                override fun onArticleLoaded(articles: List<ArticleDetailData>) {
                    view?.showArticle(articles)
                }

                override fun onDataNotAvailable() {
                    view?.showNoArticle()
                }
            })
    }

    override fun start() {

    }

    override fun clear() {
        view = null
    }

    override fun queryData() {

    }
}
