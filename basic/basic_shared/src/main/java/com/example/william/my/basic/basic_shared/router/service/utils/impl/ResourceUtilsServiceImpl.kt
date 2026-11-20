package com.example.william.my.basic.basic_shared.router.service.utils.impl

import com.blankj.utilcode.util.ResourceUtils
import com.example.william.my.basic.basic_shared.router.service.utils.ResourceUtilsService
import javax.inject.Inject

/**
 * 资源读取服务实现
 */
class ResourceUtilsServiceImpl @Inject constructor() : ResourceUtilsService {

    override fun getAssets(assetsFilePath: String): String = ResourceUtils.readAssets2String(assetsFilePath)
}
