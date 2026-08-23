package com.example.william.my.module.ui_library.activity.loadsir

import android.os.Bundle
import android.view.LayoutInflater
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.ui_library.R
import com.example.william.my.module.ui_library.loadsir.DefaultCallback
import com.example.william.my.module.ui_library.loadsir.ErrorCallback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir

/**
 * LoadSir — 多状态页面管理框架
 *
 * LoadSir 是一个轻量级的多状态页面管理库，统一管理加载中、空页面、错误页、内容页。
 *
 * 核心特性：
 * 1. 无侵入：目标视图无侵入解耦包裹，不修改原有业务布局
 * 2. 多状态管理：统一管理加载中、空页面、错误页、内容页
 * 3. 重试机制：支持重试点击事件，方便错误恢复
 * 4. 状态转换器：支持自定义状态转换逻辑
 */
@Route(path = RouterPath.UiLibrary.LoadSir)
class LoadSirActivity : BasicLayoutActivity() {

    private lateinit var loadService: LoadService<Any>

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initTargetContent()
    }

    private fun initTargetContent() {
        val targetContentView = LayoutInflater.from(this)
            .inflate(R.layout.ui_library_layout_loadsir_target, mContainer, false)

        // 注册 LoadSir，无侵入包裹目标 View
        loadService = LoadSir.getDefault().register(targetContentView) {
            // 点击重试回调
            loadService.showSuccess()
        }

        // 将 LoadSir 包装后的 root 挂载至上方展示容器
        setView(loadService.loadLayout)
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 切换为默认/加载中状态（DefaultCallback）",
            "2. 切换为错误重试状态（ErrorCallback）",
            "3. 恢复真实内容/成功状态（showSuccess）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> loadService.showCallback(DefaultCallback::class.java)
            1 -> loadService.showCallback(ErrorCallback::class.java)
            2 -> loadService.showSuccess()
        }
    }
}
