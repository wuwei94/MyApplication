package com.example.william.my.basic.basic_repo.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserData(
    var id: String = "",
    var email: String = "",
    var nickname: String = "",
) : Parcelable

@Parcelize
data class LoginData(
    var data: UserData
) : Parcelable
