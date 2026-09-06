package com.example.william.my.basic.basic_shared.router.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.william.my.basic.basic_shared.router.fragment.RouterRecyclerFragment
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.core.base.ui.activity.BaseFragmentActivity

/**
 * 路由列表 Activity 基类
 *
 * 通过 [buildRouter] 构建路由项列表，交由 [RouterRecyclerFragment] 展示与跳转。
 */
abstract class RouterRecyclerActivity : BaseFragmentActivity() {

    override fun setFragment(): Fragment {
        val bundle = Bundle()
        bundle.putParcelableArrayList("router", buildRouter())
        val fragment = RouterRecyclerFragment()
        fragment.arguments = bundle
        return fragment
    }

    protected open fun buildRouter(): ArrayList<RouterItem> = arrayListOf()
}
