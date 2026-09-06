package com.example.william.my.basic.basic_shared.router.service.utils

import com.alibaba.android.arouter.facade.template.IProvider

/**
 * 资源读取路由服务接口
 */
interface ResourceUtilsService : IProvider {
    fun getAssets(assetsFilePath: String): String
}
