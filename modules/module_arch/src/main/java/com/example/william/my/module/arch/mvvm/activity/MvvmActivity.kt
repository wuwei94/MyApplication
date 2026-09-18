package com.example.william.my.module.arch.mvvm.activity

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseFragmentActivity
import com.example.william.my.module.arch.mvvm.fragment.MvvmFragment

/**
 * MVVM — Model-View-ViewModel 架构模式
 *
 * MVVM 是 Android 官方推荐的架构模式，通过 ViewModel 和 LiveData 实现数据绑定。
 *
 * 核心组件：
 * 1. Model：数据层，负责数据获取和业务逻辑
 * 2. View：视图层，负责 UI 展示和用户交互
 * 3. ViewModel：视图模型层，负责连接 Model 和 View，持有 UI 状态
 *
 * 核心机制与避坑点：
 * 1. 数据绑定：ViewModel 通过 LiveData 自动更新 View
 * 2. 生命周期感知：ViewModel 在配置更改时保留数据
 * 3. 关注点分离：各层职责清晰，便于测试和维护
 * 4. 官方支持：Android Jetpack 提供完整支持
 *
 * https://developer.android.com/topic/libraries/architecture
 */
@Route(path = RouterPath.Arch.MVVM)
class MvvmActivity : BaseFragmentActivity() {

    override fun setFragment(): Fragment = MvvmFragment()
}
