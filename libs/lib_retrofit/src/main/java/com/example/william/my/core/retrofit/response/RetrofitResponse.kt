package com.example.william.my.core.retrofit.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Retrofit 业务响应
 *
 * 可用于传递体积较小的响应数据。泛型数据在运行时仍须是 Parcel 支持的类型。
 */
@Parcelize
class RetrofitResponse<T> private constructor(
    /** 状态码。 */
    @SerializedName("errorCode")
    val code: Int,
    /** 描述信息。 */
    @SerializedName("errorMsg")
    val message: String = "",
    /** 数据对象。 */
    @SerializedName("data")
    val data: @RawValue T? = null,
) : Parcelable {

    /**
     * 是否成功(这里约定0)
     */
    val isSuccess: Boolean
        get() = code == SUCCESS

    companion object {
        const val LOADING = -1000
        const val SUCCESS = 0
        const val ERROR = -1

        @JvmStatic
        fun <T> loading(): RetrofitResponse<T> {
            return RetrofitResponse(LOADING)
        }

        @JvmStatic
        @JvmOverloads
        fun <T> success(data: T? = null): RetrofitResponse<T> {
            return RetrofitResponse(SUCCESS, data = data)
        }

        @JvmStatic
        fun <T> error(message: String): RetrofitResponse<T> {
            return RetrofitResponse(ERROR, message)
        }

        /**
         * 使用指定的状态码、消息和数据构建 RetrofitResponse。
         */
        @JvmStatic
        fun <T> of(code: Int, message: String, data: T?): RetrofitResponse<T> {
            return RetrofitResponse(code, message, data)
        }
    }
}
