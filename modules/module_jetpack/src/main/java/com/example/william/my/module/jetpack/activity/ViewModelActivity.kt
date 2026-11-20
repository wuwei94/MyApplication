package com.example.william.my.module.jetpack.activity

import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.william.my.core.base.ui.activity.BaseActivity
import com.example.william.my.core.base.ui.fragment.BaseFragment
import com.example.william.my.module.jetpack.viewmodel.FactoryViewModel
import com.example.william.my.module.jetpack.viewmodel.SimpleViewModel

/**
 * ViewModel — 多种构建方式与作用域共享演示
 *
 * 演示 ViewModel 从底层基础到现代 DSL 的多种实例化与作用域共享方式。
 *
 * 核心机制与避坑点：
 * 1. Activity 层实例化：无参 ViewModel 使用默认工厂 `by viewModels()`，有参构造须传入自定义标准 Factory 或现代 DSL Factory
 * 2. Fragment 层作用域共享：支持自身作用域（`by viewModels()`）、宿主 Activity 共享（`by activityViewModels()`）及父子 Fragment 共享（`requireParentFragment()`）
 *
 * 官方参考：
 * https://developer.android.google.cn/topic/libraries/architecture/viewmodel
 */
class ViewModelActivity : BaseActivity() {

    /**
     * 1. 默认工厂方式：使用系统默认的 ViewModelProvider.Factory
     *
     * 注意：默认工厂仅能创建"无参构造"的 ViewModel，因此这里用 [SimpleViewModel] 演示；
     * 有参构造的 ViewModel（如 [FactoryViewModel]）必须使用下方自定义 Factory 方式。
     */
    private val viewModelByDefault: SimpleViewModel by viewModels()

    /**
     * 2. 标准接口工厂方式：通过重写 ViewModelProvider.Factory 接口注入参数
     */
    private val viewModelByStandardFactory: FactoryViewModel by viewModels {
        FactoryViewModel.StandardFactory
    }

    /**
     * 3. 现代 DSL 工厂方式：通过 viewModelFactory { initializer { ... } } 声明式注入参数
     */
    private val viewModelByDslFactory: FactoryViewModel by viewModels {
        FactoryViewModel.DslFactory
    }

    /**
     * 视图模型示例 Fragment
     *
     * 演示 ViewModel 在 Fragment 中的使用。
     */
    class ViewModelFragment : BaseFragment() {

        /**
         * 1. Fragment 自身生命周期作用域：by viewModels() + 自定义 Factory
         */
        private val viewModelByDefault: FactoryViewModel by viewModels {
            FactoryViewModel.DslFactory
        }

        /**
         * 2. 宿主 Activity 作用域共享：与宿主 Activity 及同 Activity 下的其他 Fragment 共享同一实例
         */
        private val viewModelByActivity: FactoryViewModel by activityViewModels {
            FactoryViewModel.DslFactory
        }

        class ViewModelChildFragment : BaseFragment() {

            /**
             * 3. 父子 Fragment 作用域共享：以父 Fragment 作为 ViewModelStoreOwner
             */
            private val viewModelByParent: FactoryViewModel by viewModels(
                ownerProducer = { requireParentFragment() },
                factoryProducer = { FactoryViewModel.DslFactory },
            )
        }
    }
}
