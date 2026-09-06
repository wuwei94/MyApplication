package com.example.william.my.basic.basic_repo.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 登录用户信息数据
 */
@Parcelize
data class LoginData(
    val id: String = "",
    val email: String = "",
    val nickname: String = "",
) : Parcelable
