package com.example.william.my.module.arch.activity

import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import com.example.william.my.core.base.activity.BaseActivity
import com.example.william.my.core.base.fragment.BaseFragment
import com.example.william.my.module.arch.utils.obtainViewModel
import com.example.william.my.module.arch.viewmodel.ArticleViewModel

/**
 * ArticleActivity — ViewModel 使用示例
 *
 * 演示 ViewModel 的多种创建方式和使用场景。
 *
 * 核心特性：
 * 1. 多种创建方式：支持 by viewModels()、ViewModelProvider.Factory、obtainViewModel 等
 * 2. 生命周期感知：ViewModel 在配置更改时保留数据
 * 3. Fragment 共享：支持 Activity 和 Fragment 共享 ViewModel
 * 4. 作用域控制：支持不同作用域的 ViewModel
 *
 * 创建方式：
 * 1. by viewModels()：最常用的方式，自动创建 ViewModel
 * 2. ViewModelProvider.Factory：自定义工厂，支持构造函数参数
 * 3. obtainViewModel()：自定义扩展函数，支持共享 ViewModel
 *
 * 基本用法：
 * ```kotlin
 * // 方式一：by viewModels()
 * private val viewModel: MyViewModel by viewModels()
 *
 * // 方式二：自定义工厂
 * private val viewModel: MyViewModel by viewModels {
 *     MyViewModel.Factory
 * }
 *
 * // 方式三：共享 ViewModel
 * private val viewModel: MyViewModel by viewModels({ requireParentFragment() })
 * ```
 *
 * 适用场景：
 * - MVVM 架构
 * - 数据持久化
 * - Activity 和 Fragment 数据共享
 */
class ArticleActivity : BaseActivity() {

    private val viewModels: ArticleViewModel by viewModels()

    private val mViewModel by viewModels<ArticleViewModel> {
        ArticleViewModel.Factory
    }

    private val mViewModel2 by viewModels<ArticleViewModel> {
        ArticleViewModel.Factory2
    }

    private val viewModel: ArticleViewModel =
        obtainViewModel(ArticleViewModel::class.java)

    class ArticleFragment : BaseFragment() {

        private val viewModel: ArticleViewModel =
            obtainViewModel(ArticleViewModel::class.java)

        private val viewModels: ArticleViewModel by viewModels()

        class ArticleFragment : BaseFragment() {

            private val viewModels: ArticleViewModel by viewModels({ requireParentFragment() })

        }
    }
}