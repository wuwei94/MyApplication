/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.example.william.my.module.kotlin.data

import com.example.william.my.module.kotlin.data.NetworkResult.Success
import com.google.gson.Gson

/**
 * 持有数据及其加载状态的通用密封结果类。
 */
sealed class NetworkResult<out R> {

    /**
     * 加载成功结果
     */
    data class Success<out T>(val data: T) : NetworkResult<T>()

    /**
     * 加载失败结果
     */
    data class Error(val exception: Exception) : NetworkResult<Nothing>()

    /**
     * 加载中状态
     */
    data object Loading : NetworkResult<Nothing>()

    fun string(): String = when (this) {
        is Success<*> -> "onResponse: " + Gson().toJson(this.data)
        is Error -> "onFailure: " + this.exception.message
        Loading -> "onLoading: " + "加载中……"
    }
}

/**
 * 当 [NetworkResult] 为 [Success] 类型且 [Success.data] 非空时返回 true。
 */
val NetworkResult<*>.succeeded
    get() = this is Success && data != null
