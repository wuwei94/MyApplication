package com.example.william.my.basic.basic_shared.activity

import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.example.william.my.basic.basic_shared.databinding.BasicsLayoutResponseRecyclerBinding

abstract class BasicResponseActivity : BasicRecyclerActivity() {

    protected lateinit var mBinding: BasicsLayoutResponseRecyclerBinding

    private val mLog = StringBuilder()

    override fun initViewBinding() {
        mBinding = BasicsLayoutResponseRecyclerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mRecycler = mBinding.basicsRecycler
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.basicsResponse.setOnClickListener {
            onResponseClick(it)
        }
    }

    protected open fun onResponseClick(view: View) {

    }

    /**
     * 显示响应内容，居中显示（用于初始化说明）
     */
    override fun showResponse(response: String?) {
        runOnUiThread {
            response?.let {
                mBinding.basicsResponse.text = it
                mBinding.basicsResponse.gravity = Gravity.CENTER
            }
        }
    }

    /**
     * 追加日志到 TextView，日志从左上角显示，保留历史记录
     */
    protected fun appendLog(message: String) {
        mLog.appendLine(message)
        runOnUiThread {
            mBinding.basicsResponse.text = mLog.toString()
            mBinding.basicsResponse.gravity = Gravity.TOP
        }
    }

    /**
     * 清空日志
     */
    protected fun clearLog() {
        mLog.clear()
        runOnUiThread {
            mBinding.basicsResponse.text = ""
        }
    }
}
