package com.example.william.my.basic.basic_shared.router.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.exception.HandlerException
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.recycler.BaseRecyclerFragment

@Route(path = RouterPath.Fragment.FragmentBasicRecycler)
class RouterRecyclerFragment : BaseRecyclerFragment<RouterItem>() {

    override fun initRecyclerAdapter(): BaseQuickAdapter<RouterItem, QuickViewHolder> {
        return RouterRecyclerAdapter(arrayListOf())
    }

    override fun canRefresh(): Boolean {
        return false
    }

    override fun canLoadMore(): Boolean {
        return false
    }

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
        val path = item.mRouterPath
        if (path.isNullOrEmpty()) {
            return
        }
        try {
            ARouter.getInstance().build(path).navigation()
        } catch (e: HandlerException) {
            e.printStackTrace()
        }
    }

    class RouterRecyclerAdapter(data: ArrayList<RouterItem>) :
        BaseQuickAdapter<RouterItem, QuickViewHolder>(data) {

        override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: RouterItem?) {
            holder.setText(R.id.item_textView, item?.mRouterName)
        }

        override fun onCreateViewHolder(
            context: Context,
            parent: ViewGroup,
            viewType: Int
        ): QuickViewHolder {
            return QuickViewHolder(R.layout.shared_item_recycler, parent)
        }
    }
}