package com.example.william.my.module.widget_thirdparty.activity.widget

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.widget_thirdparty.adapter.SwipeRecyclerAdapter
import com.example.william.my.module.widget_thirdparty.databinding.WidgetThirdpartyActivitySwipeLayoutBinding

/**
 * SwipeLayout — 滑动布局控件
 *
 * SwipeLayout 是一个支持滑动操作的布局库，常用于列表项的滑动删除、编辑等操作。
 *
 * 核心特性：
 * 1. 多方向滑动：支持左滑、右滑、上滑、下滑
 * 2. 丰富的自定义：支持自定义滑动菜单、滑动阻力
 * 3. 与 RecyclerView 集成：完美支持列表滑动操作
 * 4. 动画流畅：平滑的滑动动画效果
 *
 * 基本用法：
 * ```kotlin
 * // XML 中使用
 * <com.daimajia.swipe.SwipeLayout
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content">
 *     <!-- 底层菜单（滑动后显示） -->
 *     <LinearLayout
 *         android:layout_width="match_parent"
 *         android:layout_height="wrap_content" />
 *     <!-- 表层内容 -->
 *     <LinearLayout
 *         android:layout_width="match_parent"
 *         android:layout_height="wrap_content" />
 * </com.daimajia.swipe.SwipeLayout>
 * ```
 *
 * 适用场景：
 * - 列表项滑动删除、收藏
 * - 消息列表滑动操作
 * - 任何需要滑动交互的场景
 *
 * https://github.com/daimajia/AndroidSwipeLayout
 */
@Route(path = RouterPath.WidgetThirdparty.SwipeLayout)
class SwipeLayoutActivity : BaseVBActivity<WidgetThirdpartyActivitySwipeLayoutBinding>() {

    override fun getViewBinding(): WidgetThirdpartyActivitySwipeLayoutBinding {
        return WidgetThirdpartyActivitySwipeLayoutBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initRecycleView()
    }

    private fun initRecycleView() {
        val data = arrayListOf<String>()
        for (i in 0..19) {
            data.add("item : " + (i + 1))
        }
        mBinding.swipeRecycleView.adapter = SwipeRecyclerAdapter(data)
    }
}
