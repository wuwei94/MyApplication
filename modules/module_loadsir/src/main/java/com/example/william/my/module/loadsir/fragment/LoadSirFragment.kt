package com.example.william.my.module.loadsir.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutRecyclerLayoutBinding
import com.example.william.my.module.loadsir.R
import com.example.william.my.module.loadsir.callback.DefaultCallback
import com.example.william.my.module.loadsir.callback.ErrorCallback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir

/**
 * LoadSir — Fragment 中的多状态页面
 *
 * 演示 LoadSir 在 Fragment 场景下的无侵入状态管理。布局复用与
 * [com.example.william.my.basic.basic_shared.activity.BasicLayoutActivity]
 * 相同的 `shared_layout_recycler_layout`：上方 ConstraintLayout 内容容器 +
 * 下方 RecyclerView 操作列表，操作语义与 LoadSirActivity 完全一致：
 * 1. 切换为默认/加载中状态（DefaultCallback）
 * 2. 切换为错误重试状态（ErrorCallback）
 * 3. 恢复真实内容/成功状态（showSuccess）
 */
class LoadSirFragment : Fragment() {

    private lateinit var mBinding: SharedLayoutRecyclerLayoutBinding
    private lateinit var loadService: LoadService<Any>

    private val mAdapter: RecyclerAdapter by lazy {
        RecyclerAdapter()
    }
    private val mAdapterHelper: QuickAdapterHelper by lazy {
        QuickAdapterHelper.Builder(mAdapter).build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = SharedLayoutRecyclerLayoutBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTargetContent()
        initRecycler()
    }

    private fun initTargetContent() {
        // 第一步：获取目标内容 View（复用成功内容布局）
        val targetContentView = layoutInflater
            .inflate(R.layout.loadsir_layout_target, mBinding.basicsResponseContainer, false)

        // 第二步：注册 LoadSir，无侵入包裹目标 View
        loadService = LoadSir.getDefault().register(targetContentView) {
            // 点击重试回调
            loadService.showSuccess()
        }

        // 第三步：将 LoadSir 包装后的 root 挂载至上方展示容器
        mBinding.basicsResponseContainer.removeAllViews()
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mBinding.basicsResponseContainer.addView(loadService.loadLayout, params)
    }

    private fun initRecycler() {
        mAdapter.submitList(buildList())
        mAdapter.setOnItemClickListener { adapter, _, position ->
            onRecyclerClick(position, adapter.items[position])
        }
        mBinding.basicsRecycler.layoutManager = LinearLayoutManager(requireContext())
        mBinding.basicsRecycler.adapter = mAdapterHelper.adapter
    }

    private fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 切换为默认/加载中状态（DefaultCallback）",
            "2. 切换为错误重试状态（ErrorCallback）",
            "3. 恢复真实内容/成功状态（showSuccess）"
        )
    }

    private fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> loadService.showCallback(DefaultCallback::class.java)
            1 -> loadService.showCallback(ErrorCallback::class.java)
            2 -> loadService.showSuccess()
        }
    }

    class RecyclerAdapter(data: ArrayList<String> = arrayListOf()) :
        BaseQuickAdapter<String, QuickViewHolder>(data) {

        override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: String?) {
            holder.setText(com.example.william.my.basic.basic_shared.R.id.item_textView, item)
        }

        override fun onCreateViewHolder(
            context: Context,
            parent: ViewGroup,
            viewType: Int
        ): QuickViewHolder {
            return QuickViewHolder(com.example.william.my.basic.basic_shared.R.layout.shared_item_recycler, parent)
        }
    }
}
