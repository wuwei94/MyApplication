package com.example.william.my.basic.basic_shared.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutRecyclerBinding
import com.example.william.my.basic.basic_shared.dialog.BasicDialogFragment
import com.example.william.my.lib.activity.BaseActivity

abstract class BasicRecyclerActivity : BaseActivity(),
    BaseQuickAdapter.OnItemClickListener<String> {

    private val mAdapter: RecyclerAdapter by lazy {
        RecyclerAdapter()
    }
    private val mAdapterHelper: QuickAdapterHelper by lazy {
        QuickAdapterHelper.Builder(mAdapter).build()
    }

    private val mBasicDialogFragment: BasicDialogFragment by lazy {
        BasicDialogFragment()
    }

    protected lateinit var binding: SharedLayoutRecyclerBinding
    protected lateinit var mRecycler: RecyclerView

    override fun initViewBinding() {
        super.initViewBinding()
        binding = SharedLayoutRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mRecycler = binding.basicsRecycler
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        initRecycler()
    }

    private fun initRecycler() {
        mAdapter.submitList(buildList())
        mAdapter.setOnItemClickListener(this)
        mRecycler.layoutManager = LinearLayoutManager(this)
        mRecycler.adapter = mAdapterHelper.adapter
    }

    protected open fun buildList(): ArrayList<String> {
        return arrayListOf()
    }

    override fun onClick(adapter: BaseQuickAdapter<String, *>, view: View, position: Int) {
        onRecyclerClick(position, adapter.items[position])
    }

    protected open fun onRecyclerClick(position: Int, string: String) {

    }

    protected fun String.formatString(): String {
        this.let {
            val json = StringBuilder()
            val indentString = StringBuilder()
            for (element in it) {
                when (element) {
                    '{', '[' -> {
                        json.append(indentString).append(element).append("\n")
                        indentString.append("\t")
                        json.append(indentString)
                    }

                    '}', ']' -> {
                        indentString.deleteCharAt(indentString.length - 1)
                        json.append("\n").append(indentString).append(element)
                    }

                    ',' -> json.append(element).append("\n").append(indentString)
                    else -> json.append(element)
                }
            }
            return json.toString()
        }
    }

    protected fun showEventMessage(msg: String?) {
        runOnUiThread {
            msg?.let {
                mAdapter.add(it)
            }
        }
    }

    protected open fun showResponse(response: String?) {
        runOnUiThread {
            mBasicDialogFragment.show(supportFragmentManager, mBasicDialogFragment.tag)
            mBasicDialogFragment.showMessage(response?.formatString())
        }
    }

    protected fun showFailure(response: String?) {
        runOnUiThread {
            mBasicDialogFragment.show(supportFragmentManager, mBasicDialogFragment.tag)
            mBasicDialogFragment.showMessage(response)
        }
    }

    class RecyclerAdapter(data: ArrayList<String> = arrayListOf()) :
        BaseQuickAdapter<String, QuickViewHolder>(data) {

        override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: String?) {
            holder.setText(R.id.item_textView, item)
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