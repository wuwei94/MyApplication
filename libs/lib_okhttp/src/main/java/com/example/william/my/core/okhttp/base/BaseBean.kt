package com.example.william.my.core.okhttp.base

import java.io.Serializable

/**
 * 旧版可序列化数据模型基类
 *
 * 仅用于兼容仍通过 Java Serializable 传递的旧模型。新模型应根据 Android 组件传参需求
 * 直接实现 Parcelable，避免将传输方式与网络框架绑定。
 */
@Deprecated("新模型请直接实现 Parcelable")
open class BaseBean : Serializable {
    private companion object {
        private const val serialVersionUID = 1L
    }
}
