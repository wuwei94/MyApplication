package com.example.william.my.module.arch.mavericks

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseFragmentActivity

/**
 * Mavericks — Airbnb 的 MVI 框架
 *
 * Mavericks 是 Airbnb 开源的 MVI 框架，基于 Kotlin 和 ViewModel。
 *
 * 核心特性：
 * 1. MVI 架构：基于 Model-View-Intent 架构模式
 * 2. 不可变状态：使用不可变状态对象，保证数据一致性
 * 3. 简单易用：API 简单，学习成本低
 * 4. 测试友好：易于单元测试
 *
 * 核心组件：
 * 1. MavericksState：不可变状态对象
 * 2. MavericksViewModel：ViewModel 基类
 * 3. MavericksView：View 接口
 *
 * https://airbnb.io/mavericks/
 */
@Route(path = RouterPath.Arch.Mavericks)
class MavericksActivity : BaseFragmentActivity() {

    override fun setFragment(): Fragment {
        return ArticleMavericksFragment()
    }
}
