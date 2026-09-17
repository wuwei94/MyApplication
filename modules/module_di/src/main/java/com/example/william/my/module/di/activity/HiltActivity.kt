package com.example.william.my.module.di.activity

import android.os.Bundle
import androidx.activity.viewModels
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.di.hilt.entrypoint.NonHiltComponentHelper
import com.example.william.my.module.di.hilt.model.HiltActivityScopedHelper
import com.example.william.my.module.di.hilt.model.HiltApiService
import com.example.william.my.module.di.hilt.model.HiltContextHelper
import com.example.william.my.module.di.hilt.model.HiltGlobalSingleton
import com.example.william.my.module.di.hilt.model.HiltNetworkClient
import com.example.william.my.module.di.hilt.model.HiltStorageService
import com.example.william.my.module.di.hilt.model.HiltTransientHelper
import com.example.william.my.module.di.hilt.model.HiltUserRepository
import com.example.william.my.module.di.hilt.qualifier.DevApi
import com.example.william.my.module.di.hilt.qualifier.ProdApi
import com.example.william.my.module.di.hilt.viewmodel.HiltSampleViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Hilt — 基于 Dagger 2 的 Android 官方依赖注入框架
 *
 * 核心机制与避坑点：
 * 1. 编译期依赖图：@Inject / @Provides / @Binds 在编译期静态校验并生成工厂代码；Application 必须标注 @HiltAndroidApp 作为组件根
 * 2. 生命周期组件：预置 SingletonComponent / ActivityRetainedComponent / ActivityComponent / ViewModelComponent，@InstallIn 目标决定实例存活范围
 * 3. Android 集成：@AndroidEntryPoint / @HiltViewModel 开启注入；@Qualifier 区分同类型多实现；@ApplicationContext 与 @ActivityContext 限定上下文
 * 4. 动态入口点：非 @AndroidEntryPoint 类（工具类、拦截器等）通过 @EntryPoint + EntryPointAccessors 按组件手动获取依赖
 *
 * 官方参考：
 * https://dagger.dev/hilt/
 */
@Route(path = RouterPath.Di.Hilt)
@AndroidEntryPoint
class HiltActivity : BasicResponseActivity() {

    // 1. 字段注入：通过 @Inject 构造函数生成的对象
    @Inject
    lateinit var userRepository: HiltUserRepository

    // 2. 接口注入：通过 @Binds 绑定的具体实现
    @Inject
    lateinit var storageService: HiltStorageService

    // 3. 模块提供：通过 @Provides 构建的第三方类
    @Inject
    lateinit var networkClient: HiltNetworkClient

    // 4. 自定义限定符注入：区分不同环境的服务配置
    @Inject
    @ProdApi
    lateinit var prodApiService: HiltApiService

    @Inject
    @DevApi
    lateinit var devApiService: HiltApiService

    // 5. 上下文限定符注入
    @Inject
    lateinit var contextHelper: HiltContextHelper

    // 6. 作用域实例
    @Inject
    lateinit var globalSingleton: HiltGlobalSingleton

    @Inject
    lateinit var activityScopedHelper: HiltActivityScopedHelper

    @Inject
    lateinit var transientHelper: HiltTransientHelper

    // 7. ViewModel 注入
    private val hiltViewModel: HiltSampleViewModel by viewModels()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("Hilt 依赖注入实战\n\n涵盖构造注入、接口绑定、第三方构建、限定符、作用域、ViewModel及入口点")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 构造函数注入 (@Inject Constructor)",
        "2. 接口与多态绑定 (@Binds StorageService)",
        "3. 复杂第三方对象构建 (@Provides NetworkClient)",
        "4. 自定义限定符 (@ProdApi / @DevApi)",
        "5. 预置上下文注入 (@ApplicationContext / @ActivityContext)",
        "6. 作用域对比 (@Singleton vs @ActivityScoped vs 无作用域)",
        "7. Hilt ViewModel 注入 (@HiltViewModel by viewModels)",
        "8. 非组件动态入口点 (@EntryPoint & EntryPointAccessors)",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                appendLog("[1. 构造函数注入] ${userRepository.getUserInfo(1001)}")
            }
            1 -> {
                appendLog("[2. 接口绑定注入] ${storageService.saveData("key_token", "jwt_sample_value")}")
            }
            2 -> {
                appendLog("[3. @Provides 模块构建] ${networkClient.request("/api/v1/profile")}")
            }
            3 -> {
                appendLog("[4. 限定符多实现] Prod: ${prodApiService.fetchData()}")
                appendLog("[4. 限定符多实现] Dev:  ${devApiService.fetchData()}")
            }
            4 -> {
                appendLog("[5. 上下文注入] ${contextHelper.getContextInfo()}")
            }
            5 -> {
                appendLog("[6. 作用域对比]")
                appendLog("   • 全局单例 (@Singleton): hash=${globalSingleton.hashCode()}, msg=${globalSingleton.info}")
                appendLog("   • Activity 作用域 (@ActivityScoped): hash=${activityScopedHelper.hashCode()}, msg=${activityScopedHelper.info}")
                appendLog("   • 无作用域 (每次新建): hash=${transientHelper.hashCode()}, msg=${transientHelper.info}")
            }
            6 -> {
                val count = hiltViewModel.incrementAndGet()
                appendLog("[7. Hilt ViewModel] 当前计数值 = $count (${hiltViewModel.getViewModelInfo()})")
            }
            7 -> {
                // 演示在非 @AndroidEntryPoint 类（例如工具类或拦截器）中通过 EntryPoint 获取依赖
                val dynamicHelper = NonHiltComponentHelper(applicationContext)
                appendLog("[8. @EntryPoint 动态获取] ${dynamicHelper.performTask()}")
            }
        }
    }
}
