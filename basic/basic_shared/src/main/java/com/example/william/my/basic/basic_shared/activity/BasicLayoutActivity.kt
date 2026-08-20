package com.example.william.my.basic.basic_shared.activity

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutLayoutRecyclerBinding

/**
 * 布局/视图容器类示例 Activity 基类。
 *
 * 布局结构：
 * - 上方展示：ConstraintLayout 动态视图展示容器（[mContainer] / [mBinding.basicsResponseContainer]）
 * - 下方列表：RecyclerView 操作列表（通过 [buildList] 与 [onRecyclerClick] 触发操作）
 *
 * 约定与规范：
 * 1. 继承类通过 [mContainer]、[setView] 或 [addView] 动态挂载、替换或更新上方区域的 View。
 * 2. 下方统一由 [buildList] + [onRecyclerClick] 触发操作。
 * 3. 适用场景：自定义 View 效果演示、动态布局加载（如 AsyncLayoutInflater）、视图预加载展示等。
 */
abstract class BasicLayoutActivity : BasicControlActivity() {

    protected lateinit var mBinding: SharedLayoutLayoutRecyclerBinding
    protected lateinit var mContainer: ConstraintLayout

    override fun initViewBinding() {
        mBinding = SharedLayoutLayoutRecyclerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mRecycler = mBinding.basicsRecycler
        mContainer = mBinding.basicsResponseContainer
    }

    /**
     * 清空容器并设置新的 View（充满容器）
     */
    protected fun setView(view: View) {
        mContainer.removeAllViews()
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mContainer.addView(view, params)
    }

    /**
     * 向容器中追加 View
     */
    protected fun addView(view: View, params: ViewGroup.LayoutParams? = null) {
        if (params != null) {
            mContainer.addView(view, params)
        } else {
            mContainer.addView(view)
        }
    }

    /**
     * 清空容器中的全部 View
     */
    protected fun clearContainer() {
        mContainer.removeAllViews()
    }
}
