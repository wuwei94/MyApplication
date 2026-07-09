package com.example.william.my.basic.basic_repository.bean

import android.os.Parcelable
import com.example.william.my.core.okhttp.data.BaseData
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserData(
    var id: String = "",
    var email: String = "",
    var nickname: String = "",
) : Parcelable, BaseData()

@Parcelize
data class LoginData(
    var data: UserData
) : Parcelable, BaseData()