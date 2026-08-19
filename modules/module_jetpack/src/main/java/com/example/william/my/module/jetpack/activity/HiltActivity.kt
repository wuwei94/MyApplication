package com.example.william.my.module.jetpack.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Inject
import javax.inject.Qualifier

/**
 * Hilt — Android 依赖注入框架
 *
 * Hilt 是 Google 推荐的 Android 依赖注入框架，基于 Dagger 构建。
 *
 * 核心特性：
 * 1. 简化配置：自动完成 Dagger 的复杂配置
 * 2. 生命周期感知：自动管理依赖的生命周期
 * 3. 标准化：统一的依赖注入方式，便于团队协作
 * 4. 测试友好：便于单元测试和集成测试
 *
 * 核心注解：
 * 1. @HiltAndroidApp：Application 类注解，启用 Hilt
 * 2. @AndroidEntryPoint：Activity/Fragment/Service 注解，启用依赖注入
 * 3. @Inject：构造函数或字段注入
 * 4. @Module：定义依赖提供模块
 * 5. @InstallIn：指定模块安装的组件
 * 6. @Provides：提供依赖实例
 * 7. @Binds：绑定接口到实现
 * 8. @Qualifier：限定符，区分同类型依赖
 *
 * 基本用法：
 * ```kotlin
 * // 1. Application 注解
 * @HiltAndroidApp
 * class MyApp : Application()
 *
 * // 2. Activity 注解
 * @AndroidEntryPoint
 * class MyActivity : AppCompatActivity() {
 *     @Inject lateinit var myService: MyService
 * }
 *
 * // 3. 构造函数注入
 * class MyService @Inject constructor() {
 *     fun doWork() { ... }
 * }
 *
 * // 4. 模块提供
 * @Module
 * @InstallIn(ActivityComponent::class)
 * object AppModule {
 *     @Provides
 *     fun provideMyService(): MyService {
 *         return MyService()
 *     }
 * }
 * ```
 *
 * 适用场景：
 * - 依赖注入
 * - 解耦组件
 * - 单元测试
 *
 * https://dagger.dev/hilt/
 */
@Route(path = RouterPath.Jetpack.Hilt)
@AndroidEntryPoint // 将依赖项注入 Android 类
class HiltActivity : BasicResponseActivity() {

    @Inject
    lateinit var driver: Driver

    @Inject
    lateinit var engine: Engine // 使用 @Binds 注入实例

    @Inject
    lateinit var providesData: ProvidesData // 使用 @Provides 注入实例

    @Inject
    lateinit var exampleService: ExampleServiceImpl // 为同一类型提供多个绑定

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 Dagger Hilt 依赖注入（@Inject / @Binds / @Provides / @Qualifier）")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "构造函数注入（@Inject Driver）",
            "接口绑定注入（@Binds Engine）",
            "第三方类提供（@Provides ProvidesData）",
            "限定符多实例绑定（@Qualifier Auth / Other）",
            "全量依赖注入验证"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> appendLog("【@Inject 构造函数注入】${driver.getInfo()}")
            1 -> appendLog("【@Binds 接口绑定注入】${engine.getInfo()}")
            2 -> appendLog("【@Provides 模块注入】${providesData.getInfo()}")
            3 -> appendLog("【@Qualifier 限定符注入】${exampleService.getInfo()}")
            4 -> {
                appendLog("【Hilt 注入校验】Driver: ${driver.getInfo()}")
                appendLog("【Hilt 注入校验】Engine: ${engine.getInfo()}")
                appendLog("【Hilt 注入校验】ProvidesData: ${providesData.getInfo()}")
                appendLog("【Hilt 注入校验】ExampleService: ${exampleService.getInfo()}")
            }
        }
    }
}

/**
 * 构造函数的依赖注入
 */
class Driver @Inject constructor() {

    fun getInfo(): String = "Driver 实例通过 @Inject constructor() 成功注入"

    fun println() {
        Utils.logcat("Hilt", "@Inject")
    }
}

/**
 * 接口的依赖注入
 */
interface Engine {
    fun getInfo(): String
    fun println()
}

class EngineImpl @Inject constructor() : Engine {

    override fun getInfo(): String = "EngineImpl 实例通过 @Binds 成功绑定到 Engine 接口"

    override fun println() {
        Utils.logcat("Hilt", "@Binds")
    }
}

@Module // Hilt 模块
@InstallIn(ActivityComponent::class)
abstract class EngineModule {

    /**
     * 使用@Binds注入接口实例
     *
     * 带有注解的函数会向 Hilt 提供以下信息：
     *      函数返回类型会告知 Hilt 该函数提供哪个接口的实例。
     *      函数参数会告知 Hilt 要提供哪种实现。
     */
    @Binds
    abstract fun bindEngine(engineImpl: EngineImpl): Engine
}

/**
 * 第三方类的依赖注入
 */
class ProvidesData {

    fun getInfo(): String = "ProvidesData 实例通过 @Provides Module 成功构建注入"

    fun println() {
        Utils.logcat("Hilt", "@Provides")
    }
}

@Module // Hilt 模块
@InstallIn(ActivityComponent::class)
object ProvidesModule {

    /**
     * 使用 @Provides 注入实例
     *
     * 带有注解的函数会向 Hilt 提供以下信息：
     *      函数返回类型会告知 Hilt 函数提供哪个类型的实例。
     *      函数参数会告知 Hilt 相应类型的依赖项。
     *      函数主体会告知 Hilt 如何提供相应类型的实例。每当需要提供该类型的实例时，Hilt 都会执行函数主体。
     */
    @Provides
    fun provideProvidesData(): ProvidesData {
        return ProvidesData()
    }
}

/**
 * 为同一类型提供多个依赖注入
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Auth

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Other

/**
 * 为同一类型提供多个绑定
 */
@Module // Hilt 模块
@InstallIn(ActivityComponent::class)
object MultipleModule {

    @Auth
    @Provides
    fun provideAuth(): String {
        return "Auth"
    }

    @Other
    @Provides
    fun provideOther(): String {
        return "Other"
    }
}

class ExampleServiceImpl @Inject constructor(
    @param:Auth private val auth: String, // Auth
    @param:Other private val other: String // Other
) {

    fun getInfo(): String = "ExampleService 携带限定符: @Auth='$auth', @Other='$other'"

    fun println() {
        Utils.logcat("Hilt", "@Qualifier: $auth,$other")
    }
}
