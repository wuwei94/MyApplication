package com.example.william.my.basic.basic_shared.router.item

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 路由列表项（名称、路径与跳转参数）
 */
@Parcelize
data class RouterItem(
    val mRouterName: String?,
    val mRouterPath: String?,
    val mParams: HashMap<String, String> = hashMapOf(),
) : Parcelable
