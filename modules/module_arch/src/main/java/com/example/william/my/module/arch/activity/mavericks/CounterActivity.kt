package com.example.william.my.module.arch.activity.mavericks

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseFragmentActivity
import com.example.william.my.module.arch.mavericks.counter.CounterFragment

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
 * 基本用法：
 * ```kotlin
 * // 定义状态
 * data class CounterState(val count: Int = 0) : MavericksState
 *
 * // 定义 ViewModel
 * class CounterViewModel(initialState: CounterState) : MavericksViewModel<CounterState>(initialState) {
 *     fun increment() {
 *         setState { copy(count = count + 1) }
 *     }
 *     fun decrement() {
 *         setState { copy(count = count - 1) }
 *     }
 * }
 *
 * // 在 Fragment 中使用
 * class CounterFragment : Fragment(), MavericksView {
 *     private val viewModel: CounterViewModel by fragmentViewModel()
 *
 *     override fun invalidate() {
 *         // 更新 UI
 *         textView.text = viewModel.state.count.toString()
 *     }
 * }
 * ```
 *
 * 适用场景：
 * - 学习 MVI 架构
 * - 简单状态管理
 * - 计数器、开关等简单功能
 *
 * https://airbnb.io/mavericks/
 */
@Route(path = RouterPath.Arch.Counter)
class CounterActivity : BaseFragmentActivity() {

    override fun setFragment(): Fragment {
        return CounterFragment()
    }
}