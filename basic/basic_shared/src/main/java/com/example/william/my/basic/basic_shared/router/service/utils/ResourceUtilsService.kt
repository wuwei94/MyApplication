package com.example.william.my.basic.basic_shared.router.service.utils

import com.alibaba.android.arouter.facade.template.IProvider

interface ResourceUtilsService : IProvider {
    fun getAssets(assetsFilePath: String): String
}