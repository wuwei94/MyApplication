package com.example.william.my.basic.basic_shared.activity

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutResponseRecyclerBinding

abstract class BasicResponseActivity : BasicRecyclerActivity() {

    protected lateinit var mBinding: SharedLayoutResponseRecyclerBinding

    private val mLog = SpannableStringBuilder()

    override fun initViewBinding() {
        mBinding = SharedLayoutResponseRecyclerBinding.inflate(layoutInflater)
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
     * 追加日志到 TextView，使用默认颜色
     */
    protected fun appendLog(message: String) {
        mLog.appendLine(message)
        runOnUiThread {
            mBinding.basicsResponse.text = mLog
            mBinding.basicsResponse.gravity = Gravity.TOP
        }
    }

    /**
     * 追加格式化日志到 TextView（JSON 格式化）
     */
    protected fun appendFormatLog(prefix: String, message: String) {
        appendLog("$prefix${message.formatString()}")
    }

    /**
     * 追加带颜色的日志到 TextView
     */
    protected fun appendLog(message: String, color: Int) {
        val start = mLog.length
        mLog.appendLine(message)
        mLog.setSpan(
            ForegroundColorSpan(color),
            start,
            mLog.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        runOnUiThread {
            mBinding.basicsResponse.text = mLog
            mBinding.basicsResponse.gravity = Gravity.TOP
        }
    }

    /**
     * 追加 accent 颜色的日志到 TextView
     */
    protected fun appendLogAccent(message: String) {
        appendLog(message, ContextCompat.getColor(this, R.color.shared_color_accent))
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
