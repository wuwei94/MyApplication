package com.example.william.my.module.opensource.activity.utils

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.opensource.loadsir.DefaultCallback
import com.example.william.my.module.opensource.loadsir.ErrorCallback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir

/**
 * LoadSir — 多状态页面管理框架
 *
 * LoadSir 是一个轻量级的多状态页面管理库，统一管理加载中、空页面、错误页、内容页。
 *
 * 核心特性：
 * 1. 无侵入：目标视图无侵入解耦包裹，不修改原有布局
 * 2. 多状态管理：统一管理加载中、空页面、错误页、内容页
 * 3. 重试机制：支持重试点击事件，方便错误恢复
 * 4. 状态转换器：支持自定义状态转换逻辑
 *
 * 基本用法：
 * ```kotlin
 * // 注册目标视图
 * val loadService = LoadSir.getDefault().register(targetView) {
 *     // 重试点击回调
 *     loadService.showSuccess()
 * }
 *
 * // 切换状态
 * loadService.showSuccess()                          // 显示内容
 * loadService.showCallback(LoadingCallback::class.java)  // 显示加载中
 * loadService.showCallback(ErrorCallback::class.java)    // 显示错误
 * ```
 *
 * 适用场景：
 * - 网络请求加载状态管理
 * - 列表空数据、错误状态展示
 * - 任何需要多状态切换的场景
 *
 * https://github.com/kingja/LoadSir
 */
@Route(path = RouterPath.OpenSource.LoadSir)
class LoadSirActivity : BasicResponseActivity() {

    private lateinit var loadService: LoadService<Any>

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 LoadSir 多状态页面切换（Default / Error / Success）")
        initLoadService()
    }

    private fun initLoadService() {
        loadService = LoadSir.getDefault().register(mBinding.basicsResponse) {
            appendLog("【LoadSir】触发重试回调（onReload），正在恢复内容...")
            loadService.showSuccess()
        }
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "切换为默认状态（DefaultCallback）",
            "切换为错误状态（ErrorCallback）",
            "恢复内容/成功状态（showSuccess）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                loadService.showCallback(DefaultCallback::class.java)
                appendLog("【LoadSir】已切换为 DefaultCallback 状态")
            }

            1 -> {
                loadService.showCallback(ErrorCallback::class.java)
                appendLog("【LoadSir】已切换为 ErrorCallback 错误状态")
            }

            2 -> {
                loadService.showSuccess()
                appendLog("【LoadSir】已恢复为 Success 内容状态")
            }
        }
    }
}