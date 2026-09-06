package com.example.william.my.module.arch.mvp.contract

import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.core.base.arch.mvp.IBasePresenter
import com.example.william.my.core.base.arch.mvp.IBaseView

/**
 * 文章业务契约接口
 *
 * 定义 View 与 Presenter 之间的交互契约。
 */
interface ArticleContract {

    /**
     * 文章页 View 契约
     */
    interface View : IBaseView<Presenter> {

        fun showArticle(articles: List<ArticleDetailData>)

        fun showNoArticle()
    }

    /**
     * 文章页 Presenter 契约
     */
    interface Presenter : IBasePresenter {

        fun loadArticle(page: Int)
    }
}
