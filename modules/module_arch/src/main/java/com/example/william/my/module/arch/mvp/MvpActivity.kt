package com.example.william.my.module.arch.mvp

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseFragmentActivity

/**
 * MVP — Model-View-Presenter 架构模式
 *
 * MVP 是一种经典的架构模式，通过 Presenter 将 View 和 Model 解耦。
 *
 * 核心组件：
 * 1. Model：数据层，负责数据获取和业务逻辑
 * 2. View：视图层，负责 UI 展示，实现 View 接口
 * 3. Presenter：表示层，负责连接 View 和 Model，处理业务逻辑
 *
 * 核心特性：
 * 1. 关注点分离：各层职责清晰，便于测试
 * 2. 接口驱动：View 和 Presenter 通过接口通信
 * 3. 易于测试：Presenter 可独立于 Android 框架测试
 * 4. 灵活性高：可根据需求调整各层职责
 *
 * 基本用法：
 * ```kotlin
 * // View 接口
 * interface MyView {
 *     fun showData(data: String)
 *     fun showError(error: String)
 * }
 *
 * // Presenter
 * class MyPresenter(private val view: MyView) {
 *     fun loadData() {
 *         // 加载数据
 *         view.showData("Hello MVP")
 *     }
 * }
 *
 * // Activity 实现 View 接口
 * class MyActivity : AppCompatActivity(), MyView {
 *     private val presenter = MyPresenter(this)
 *
 *     override fun showData(data: String) {
 *         // 更新 UI
 *     }
 * }
 * ```
 *
 * 适用场景：
 * - 需要高度可测试性的场景
 * - 复杂的业务逻辑处理
 * - 团队协作开发
 */
@Route(path = RouterPath.Arch.MVP)
class MvpActivity : BaseFragmentActivity() {

    override fun setFragment(): Fragment {
        return MvpFragment()
    }
}
