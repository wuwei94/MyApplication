package com.example.william.my.module.arch.mvp.contract

import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.core.base.presenter.IBasePresenter
import com.example.william.my.core.base.view.IBaseView

/**
 * 文章业务契约接口
 *
 * 定义 View 与 Presenter 之间的交互契约。
 */
interface ArticleContract {

    interface View : IBaseView<Presenter> {

        fun showArticle(articles: List<ArticleDetailData>)

        fun showNoArticle()
    }

    interface Presenter : IBasePresenter {

        fun loadArticle(page: Int)
    }
}
