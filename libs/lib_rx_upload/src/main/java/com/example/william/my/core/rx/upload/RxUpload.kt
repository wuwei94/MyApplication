package com.example.william.my.core.rx.upload

import com.example.william.my.core.rx.upload.builder.RxUploadBuilder

/** Rx 文件上传入口。 */
object RxUpload {

    @JvmStatic
    fun builder(): RxUploadBuilder = RxUploadBuilder()
}
