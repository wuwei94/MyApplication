package com.example.william.my.module.jetpack.viewmodel

import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseActivity
import com.example.william.my.core.base.fragment.BaseFragment

/**
 * ArticleActivity — ViewModel 多种构建方式演示
 *
 * 演示 ViewModel 从底层基础到现代 DSL 的多种实例化与作用域共享方式。
 *
 * 【Activity 层实例化方式】
 * 1. viewModelByDefault: 默认工厂方式（by viewModels()），仅适用于无参构造的 ViewModel（如 [SimpleViewModel]）；
 * 2. viewModelByStandardFactory: 标准接口工厂方式（by viewModels { StandardFactory }）；
 * 3. viewModelByDslFactory: 现代 DSL 工厂方式（by viewModels { DslFactory }）。
 *
 * 【Fragment 层实例化与作用域共享方式】
 * 1. viewModelByDefault: Fragment 自身生命周期作用域（by viewModels() + Factory）；
 * 2. viewModelByActivity: 与宿主 Activity 共享 ViewModel（by activityViewModels() + Factory）；
 * 3. viewModelByParent: 父子 Fragment 间作用域共享（by viewModels({ requireParentFragment() }) + Factory）。
 */
@Route(path = RouterPath.Jetpack.ViewModel)
class ArticleActivity : BaseActivity() {

    /**
     * 1. 默认工厂方式：使用系统默认的 ViewModelProvider.Factory
     *
     * 注意：默认工厂仅能创建"无参构造"的 ViewModel，因此这里用 [SimpleViewModel] 演示；
     * 有参构造的 ViewModel（如 [ArticleViewModel]）必须使用下方自定义 Factory 方式。
     */
    private val viewModelByDefault: SimpleViewModel by viewModels()

    /**
     * 2. 标准接口工厂方式：通过重写 ViewModelProvider.Factory 接口注入参数
     */
    private val viewModelByStandardFactory: ArticleViewModel by viewModels {
        ArticleViewModel.StandardFactory
    }

    /**
     * 3. 现代 DSL 工厂方式：通过 viewModelFactory { initializer { ... } } 声明式注入参数
     */
    private val viewModelByDslFactory: ArticleViewModel by viewModels {
        ArticleViewModel.DslFactory
    }

    class ArticleFragment : BaseFragment() {

        /**
         * 1. Fragment 自身生命周期作用域：by viewModels() + 自定义 Factory
         */
        private val viewModelByDefault: ArticleViewModel by viewModels {
            ArticleViewModel.DslFactory
        }

        /**
         * 2. 宿主 Activity 作用域共享：与宿主 Activity 及同 Activity 下的其他 Fragment 共享同一实例
         */
        private val viewModelByActivity: ArticleViewModel by activityViewModels {
            ArticleViewModel.DslFactory
        }

        class ArticleChildFragment : BaseFragment() {

            /**
             * 3. 父子 Fragment 作用域共享：以父 Fragment 作为 ViewModelStoreOwner
             */
            private val viewModelByParent: ArticleViewModel by viewModels(
                ownerProducer = { requireParentFragment() },
                factoryProducer = { ArticleViewModel.DslFactory }
            )
        }
    }
}
