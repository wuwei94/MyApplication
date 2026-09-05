package com.example.william.my.module.arch.mvi

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseFragmentActivity

/**
 * MVI — Model-View-Intent 架构模式
 *
 * MVI 是一种响应式架构模式，基于单向数据流和不可变状态。
 *
 * 核心组件：
 * 1. Model：不可变的状态对象，代表 UI 的完整状态
 * 2. View：视图层，根据状态渲染 UI，发出用户意图
 * 3. Intent：用户意图，代表用户想要执行的操作
 *
 * 核心特性：
 * 1. 单向数据流：数据从 Model → View → Intent 单向流动
 * 2. 不可变状态：状态不可变，每次更新都创建新状态
 * 3. 响应式编程：基于 RxJava 或 Kotlin Flow
 * 4. 易于调试：状态变化可追踪，便于调试
 *
 * 基本用法：
 * ```kotlin
 * // 状态
 * data class MyState(
 *     val isLoading: Boolean = false,
 *     val data: String? = null,
 *     val error: String? = null
 * )
 *
 * // 意图
 * sealed class MyIntent {
 *     object LoadData : MyIntent()
 *     object Refresh : MyIntent()
 * }
 *
 * // ViewModel
 * class MyViewModel : ViewModel() {
 *     val state: StateFlow<MyState> = intentFlow
 *         .flatMapLatest { intent ->
 *             when (intent) {
 *                 is MyIntent.LoadData -> loadData()
 *                 is MyIntent.Refresh -> refresh()
 *             }
 *         }
 *         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), MyState())
 * }
 * ```
 *
 * 适用场景：
 * - 复杂的 UI 状态管理
 * - 需要响应式编程的场景
 * - 需要可预测状态管理的场景
 */
@Route(path = RouterPath.Arch.MVI)
class MviActivity : BaseFragmentActivity() {

    override fun setFragment(): Fragment = MviFragment()
}
