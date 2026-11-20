package com.example.william.my.basic.basic_shared.router.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.navigator.AppNavigator
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.core.base.ui.recycler.BaseRecyclerFragment

/**
 * 路由列表 Fragment
 *
 * 展示由 [RouterRecyclerActivity] 传入的路由项列表，点击后通过 [AppNavigator] 跳转。
 */
class RouterRecyclerFragment : BaseRecyclerFragment<RouterItem>() {

    override fun initRecyclerAdapter(): BaseQuickAdapter<RouterItem, QuickViewHolder> = RouterRecyclerAdapter(arrayListOf())

    override fun canRefresh(): Boolean = false

    override fun canLoadMore(): Boolean = false

    override fun initView(view: View?, state: Bundle?) {
        super.initView(view, state)

        val items = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelableArrayList("router", RouterItem::class.java)
        } else {
            @Suppress("deprecation")
            arguments?.getParcelableArrayList("router")
        }
        onDataSuccess(items)
    }

    override fun onClick(adapter: BaseQuickAdapter<RouterItem, *>, view: View, position: Int) {
        super.onClick(adapter, view, position)
        val item = adapter.items[position]
        val path = item.routerPath
        if (path.isNullOrEmpty()) {
            return
        }
        val navigator = AppNavigator.build(path)
        item.params.forEach { (key, value) ->
            navigator.withString(key, value)
        }
        navigator.navigation(requireActivity())
    }

    class RouterRecyclerAdapter(data: ArrayList<RouterItem>) : BaseQuickAdapter<RouterItem, QuickViewHolder>(data) {

        override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: RouterItem?) {
            holder.setText(R.id.item_textView, item?.routerName)
        }

        override fun onCreateViewHolder(
            context: Context,
            parent: ViewGroup,
            viewType: Int,
        ): QuickViewHolder = QuickViewHolder(R.layout.shared_item_recycler, parent)
    }
}
