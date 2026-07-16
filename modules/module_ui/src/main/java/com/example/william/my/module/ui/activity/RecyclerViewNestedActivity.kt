package com.example.william.my.module.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.ui.adapter.RecyclerNestedAdapter
import com.example.william.my.module.ui.databinding.UiActivityRecyclerViewNestedBinding

/**
 * 嵌套 RecyclerView 示例
 * 外层 RecyclerView 每个 item 内包含一个内层 RecyclerView
 * 关键点：内层 RecyclerView 使用自定义 NestedRecyclerView，重写了 onMeasure()
 * 解决嵌套滚动冲突，确保内层 RecyclerView 能正确测量和滚动
 */
@Route(path = RouterPath.UI.RecyclerViewNested)
class RecyclerViewNestedActivity : BaseVBActivity<UiActivityRecyclerViewNestedBinding>() {

    override fun getViewBinding(): UiActivityRecyclerViewNestedBinding {
        return UiActivityRecyclerViewNestedBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initRecycleView()
    }

    private fun initRecycleView() {
        val data = (1..20).map { "POSITION $it" }

        mBinding.recycleView.layoutManager = LinearLayoutManager(this)
        mBinding.recycleView.adapter = RecyclerNestedAdapter(data)
    }
}
