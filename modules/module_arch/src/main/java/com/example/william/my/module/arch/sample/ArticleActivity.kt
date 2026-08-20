package com.example.william.my.module.arch.sample

import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.william.my.core.base.activity.BaseActivity
import com.example.william.my.core.base.fragment.BaseFragment
import com.example.william.my.module.arch.mvvm.utils.obtainViewModel

/**
 * ArticleActivity — ViewModel 多种构建方式演示
 *
 * 演示 ViewModel 从底层基础到现代 DSL 的多种实例化与作用域共享方式：
 *
 * 【Activity 层实例化方式】
 * 1. viewModelByExtension: 原生/扩展函数方式（基于 ViewModelProvider(this).get()）；
 * 2. viewModelByDefault: 标准属性委托方式（by viewModels()，使用默认工厂）；
 * 3. viewModelByStandardFactory: 标准接口工厂方式（by viewModels { StandardFactory }）；
 * 4. viewModelByDslFactory: 现代 DSL 工厂方式（by viewModels { DslFactory }）。
 *
 * 【Fragment 层实例化与作用域共享方式】
 * 1. viewModelByExtension: 原生/扩展函数方式（基于 Fragment 的 obtainViewModel() 扩展函数）；
 * 2. viewModelByDefault: Fragment 自身生命周期作用域（by viewModels()）；
 * 3. viewModelByActivity: 与宿主 Activity 共享 ViewModel（by activityViewModels()）；
 * 4. viewModelByParent: 父子 Fragment 间作用域共享（by viewModels({ requireParentFragment() })）。
 */
@Deprecated("ViewModel 多种构建方式演示示例")
class ArticleActivity : BaseActivity() {

    /**
     * 1. 原生/扩展函数方式：基于 ViewModelProvider(this).get() 封装的 obtainViewModel
     */
    private val viewModelByExtension: ArticleViewModel by lazy {
        obtainViewModel(ArticleViewModel::class.java)
    }

    /**
     * 2. 标准属性委托方式：使用系统默认的 ViewModelProvider.Factory
     */
    private val viewModelByDefault: ArticleViewModel by viewModels()

    /**
     * 3. 标准接口工厂方式：通过重写 ViewModelProvider.Factory 接口注入参数
     */
    private val viewModelByStandardFactory: ArticleViewModel by viewModels {
        ArticleViewModel.StandardFactory
    }

    /**
     * 4. 现代 DSL 工厂方式：通过 viewModelFactory { initializer { ... } } 声明式注入参数
     */
    private val viewModelByDslFactory: ArticleViewModel by viewModels {
        ArticleViewModel.DslFactory
    }

    class ArticleFragment : BaseFragment() {

        /**
         * 1. Fragment 扩展函数方式：基于 Fragment.obtainViewModel 扩展函数
         */
        private val viewModelByExtension: ArticleViewModel by lazy {
            obtainViewModel(ArticleViewModel::class.java)
        }

        /**
         * 2. Fragment 自身生命周期作用域：通过 by viewModels() 构建
         */
        private val viewModelByDefault: ArticleViewModel by viewModels()

        /**
         * 3. 宿主 Activity 作用域共享：与宿主 Activity 及同 Activity 下的其他 Fragment 共享同一实例
         */
        private val viewModelByActivity: ArticleViewModel by activityViewModels()

        class ArticleChildFragment : BaseFragment() {

            /**
             * 4. 父子 Fragment 作用域共享：以父 Fragment 作为 ViewModelStoreOwner
             */
            private val viewModelByParent: ArticleViewModel by viewModels({ requireParentFragment() })

        }
    }
}
