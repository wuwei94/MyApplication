package com.example.william.my.basic.basic_shared.router.service.utils.impl

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ResourceUtils
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.router.service.utils.ResourceUtilsService

/**
 * 资源读取路由服务实现
 */
@Route(path = RouterPath.Service.ResourceUtilsService)
class ResourceUtilsServiceImpl : ResourceUtilsService {

    override fun getAssets(assetsFilePath: String): String = ResourceUtils.readAssets2String(assetsFilePath)

    override fun init(context: Context) {}
}
