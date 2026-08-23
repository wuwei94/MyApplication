package com.example.william.my.module.mavericks.counter

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseFragmentActivity

/**
 * Mavericks Counter — 计数器示例
 *
 * 使用 Mavericks 框架实现的计数器示例，演示 MVI 架构的基本用法。
 *
 * 核心特性：
 * 1. 不可变状态：使用 data class 定义不可变状态
 * 2. 状态更新：使用 setState 更新状态
 * 3. UI 绑定：自动绑定状态到 UI
 * 4. 简单易用：代码简洁，易于理解
 *
 * https://airbnb.io/mavericks/
 */
@Route(path = RouterPath.Mavericks.Counter)
class CounterActivity : BaseFragmentActivity() {

    override fun setFragment(): Fragment {
        return CounterFragment()
    }
}
