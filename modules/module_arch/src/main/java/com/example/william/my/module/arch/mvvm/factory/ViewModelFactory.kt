package com.example.william.my.module.arch.mvvm.factory

import android.annotation.SuppressLint
import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.module.arch.mvi.viewmodel.ArticleStateFlowViewModel
import com.example.william.my.module.arch.mvvm.viewmodel.ArticleLiveDataViewModel
import com.example.william.my.module.arch.sample.ArticleViewModel

/**
 * ViewModel 工厂类
 *
 * 用于向 ViewModel 注入 [ArticleRepository] 依赖。
 */
class ViewModelFactory private constructor(private val articleRepository: ArticleRepository) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(ArticleLiveDataViewModel::class.java) ->
                    ArticleLiveDataViewModel(articleRepository)

                isAssignableFrom(ArticleViewModel::class.java) ->
                    ArticleViewModel(articleRepository, SavedStateHandle())

                isAssignableFrom(ArticleStateFlowViewModel::class.java) ->
                    ArticleStateFlowViewModel(articleRepository)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(app: Application) =
            INSTANCE ?: synchronized(ViewModelFactory::class.java) {
                INSTANCE ?: ViewModelFactory(ServiceLocator.provideArticleRepository(app))
                    .also {
                        INSTANCE = it
                    }
            }

        @VisibleForTesting
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
