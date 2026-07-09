package com.example.william.my.module.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_module.router.path.RouterPath
import com.example.william.my.lib.activity.BaseVBActivity
import com.example.william.my.module.ui.adapter.RecyclerNestedAdapter
import com.example.william.my.module.ui.databinding.UiActivityRecyclerNestedBinding

@Route(path = RouterPath.UI.RecyclerViewNested)
class RecyclerViewNestedActivity : BaseVBActivity<UiActivityRecyclerNestedBinding>() {

    override fun getViewBinding(): UiActivityRecyclerNestedBinding {
        return UiActivityRecyclerNestedBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initRecycleView()
    }

    private fun initRecycleView() {
        val data = arrayListOf<String>()
        for (i in 1..20) {
            data.add("POSITION $i")
        }

        mBinding.recycleView.layoutManager = LinearLayoutManager(this)
        mBinding.recycleView.adapter = RecyclerNestedAdapter(data)
    }
}
