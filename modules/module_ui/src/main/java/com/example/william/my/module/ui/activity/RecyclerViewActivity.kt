package com.example.william.my.module.ui.activity

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.core.base.recyclerview.itemdecoration.RItemDecorationItemSpacing
import com.example.william.my.core.base.utils.DisplayUtils
import com.example.william.my.module.ui.R
import com.example.william.my.module.ui.adapter.RecyclerAdapter
import com.example.william.my.module.ui.databinding.UiActivityRecyclerViewBinding

/**
 * RecyclerView 示例
 *
 * LayoutManager -> ItemDecoration -> ItemAnimator -> Adapter -> LayoutAnimation -> SnapHelper
 *
 * 布局管理器（LayoutManager）：
 * - LinearLayoutManager: 线性布局，单列
 * - GridLayoutManager: 网格布局，多列
 * - StaggeredGridLayoutManager: 瀑布流布局，列宽/行高不等
 *
 * 装饰器（ItemDecoration）：
 * - RItemDecorationItemSpacing: Item 间距
 * - DividerItemDecoration: 分割线
 *
 * SnapHelper：
 * - LinearSnapHelper: 支持快速滑动，像 ViewPager 一样每次滑动一页
 * - PagerSnapHelper: 限制一次只能滑动一页，不能快速滑动
 */
@Route(path = RouterPath.UI.RecyclerView)
class RecyclerViewActivity : BaseVBActivity<UiActivityRecyclerViewBinding>() {

    override fun getViewBinding(): UiActivityRecyclerViewBinding {
        return UiActivityRecyclerViewBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        //固定大小，item 尺寸不变时跳过重新测量
        mBinding.recycleView.setHasFixedSize(true)
        //布局管理器（必须最先设置）
        initLayoutManager(LayoutManagerType.LINEAR)
        //装饰器（影响测量，应在 Adapter 之前）
        initItemDecoration()
        //项动画（add/remove/move/change）
        initItemAnimator()
        //适配器（数据绑定）
        initAdapter()
        //入场动画（需要 Adapter 已设置）
        initLayoutAnimation()
        //SnapHelper（对齐方式，依赖 LayoutManager）
        initSnapHelper(SnapHelperType.LINEAR)
    }

    private enum class LayoutManagerType { LINEAR, GRID, STAGGERED }

    /**
     * 布局管理器设置
     * - LINEAR: LinearLayoutManager，线性布局，单列
     * - GRID: GridLayoutManager，网格布局，多列
     * - STAGGERED: StaggeredGridLayoutManager，瀑布流布局，列宽/行高不等
     */
    private fun initLayoutManager(type: LayoutManagerType) {
        val manager = when (type) {
            LayoutManagerType.LINEAR -> LinearLayoutManager(this)
            LayoutManagerType.GRID -> GridLayoutManager(this, 4)
            LayoutManagerType.STAGGERED -> StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
                gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
            }
        }
        mBinding.recycleView.layoutManager = manager
    }

    /**
     * 装饰器设置
     * - DividerItemDecoration: 分割线
     */
    private fun initItemDecoration() {
        mBinding.recycleView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(this@RecyclerViewActivity, R.drawable.ui_divider)!!)
            }
        )
    }

    /**
     * 项动画
     * - DefaultItemAnimator: 默认动画，处理 add/remove/move/change
     */
    private fun initItemAnimator() {
        mBinding.recycleView.itemAnimator = DefaultItemAnimator()
    }

    /**
     * 适配器设置
     * - setHasStableIds(true): 启用稳定 ID，配合 notifyItemChanged 的 payload 使用
     */
    private fun initAdapter() {
        val data = (1..59).map { "POSITION $it" }.toMutableList()
        val adapter = RecyclerAdapter(data)
        adapter.setHasStableIds(true)
        mBinding.recycleView.adapter = adapter
    }

    /**
     * 列表项入场动画
     * - ORDER_NORMAL: 顺序显示
     * - ORDER_REVERSE: 倒序显示
     * - ORDER_RANDOM: 随机显示
     * - delay: 每个 item 动画间隔（0.2 表示间隔 0.2 倍动画时长）
     */
    private fun initLayoutAnimation() {
        val controller = LayoutAnimationController(
            AnimationUtils.loadAnimation(this, R.anim.ui_anim_recycler_item_left)
        )
        controller.order = LayoutAnimationController.ORDER_NORMAL
        controller.delay = 0.2f
        mBinding.recycleView.layoutAnimation = controller
    }

    private enum class SnapHelperType { NONE, LINEAR, PAGER }

    /**
     * SnapHelper 设置
     * - NONE: 不使用 SnapHelper
     * - LINEAR: LinearSnapHelper，支持快速滑动，像吸附效果
     * - PAGER: PagerSnapHelper，一次只能滑动一页，像翻页效果
     */
    private fun initSnapHelper(type: SnapHelperType) {
        when (type) {
            SnapHelperType.LINEAR -> LinearSnapHelper().attachToRecyclerView(mBinding.recycleView)
            SnapHelperType.PAGER -> PagerSnapHelper().attachToRecyclerView(mBinding.recycleView)
            SnapHelperType.NONE -> { }
        }
    }
}
