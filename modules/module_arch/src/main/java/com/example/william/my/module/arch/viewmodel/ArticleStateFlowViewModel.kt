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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.module.arch.intent.ArticleIntent
import com.example.william.my.module.arch.intent.ArticleViewState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class ArticleStateFlowViewModel(private val repository: ArticleRepository) :
    ViewModel() {

    val intent = Channel<ArticleIntent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow<ArticleViewState>(ArticleViewState.Loading)
    val state: StateFlow<ArticleViewState>
        get() = _state

    init {
        // 启动一个新的协程
        viewModelScope.launch {
            // 将 Channel 转换为 flow
            intent.consumeAsFlow().collect {
                when (it) {
                    is ArticleIntent.LoadArticleIntent -> loadArticle(it.page)
                }
            }
        }
    }

    private fun loadArticle(page: Int) {
        viewModelScope.launch {
            repository.getArticleFlow(page).collect { response ->
                _state.value = when {
                    response.code == RetrofitResponse.LOADING -> ArticleViewState.Loading
                    response.isSuccess -> {
                        val datas = response.data?.datas ?: emptyList()
                        ArticleViewState.Success(datas)
                    }
                    else -> ArticleViewState.Error(response.message)
                }
            }
        }
    }
}