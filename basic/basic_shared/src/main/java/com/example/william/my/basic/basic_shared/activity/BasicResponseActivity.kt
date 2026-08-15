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
import com.example.william.my.basic.basic_shared.utils.JsonFormatter

abstract class BasicResponseActivity : BasicRecyclerActivity() {

    protected lateinit var mBinding: SharedLayoutResponseRecyclerBinding

    private val mLog = SpannableStringBuilder()
    private val mUpdatingLogs = linkedMapOf<String, String>()

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
     * 居中显示页面初始说明。
     *
     * 首次追加或更新日志后，说明会被运行日志替换。
     */
    protected fun showDescription(description: String) {
        runOnUiThread {
            mBinding.basicsResponse.text = description
            mBinding.basicsResponse.gravity = Gravity.CENTER
        }
    }

    /**
     * 显示响应内容，居中显示（用于初始化说明）
     */
    @Deprecated("响应替换展示已废弃，请使用 appendLog 或 appendFormatLog")
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
        runOnUiThread {
            mLog.appendLine(message)
            renderLogs()
        }
    }

    /**
     * 追加格式化日志到 TextView（JSON 格式化）
     */
    protected fun appendFormatLog(prefix: String, message: String) {
        appendLog("$prefix${JsonFormatter.format(message)}")
    }

    /**
     * 追加带颜色的日志到 TextView
     */
    protected fun appendLog(message: String, color: Int) {
        runOnUiThread {
            val start = mLog.length
            mLog.appendLine(message)
            mLog.setSpan(
                ForegroundColorSpan(color),
                start,
                mLog.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            renderLogs()
        }
    }

    /**
     * 更新指定运行状态；相同 key 的内容会被替换，不进入历史日志。
     */
    protected fun updateLog(key: String, message: String) {
        runOnUiThread {
            mUpdatingLogs[key] = message
            renderLogs()
        }
    }

    /** 移除指定运行状态。 */
    protected fun removeUpdatingLog(key: String) {
        runOnUiThread {
            mUpdatingLogs.remove(key)
            renderLogs()
        }
    }

    /** 清空全部运行状态，保留历史日志。 */
    protected fun clearUpdatingLogs() {
        runOnUiThread {
            mUpdatingLogs.clear()
            renderLogs()
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
        runOnUiThread {
            mLog.clear()
            mUpdatingLogs.clear()
            renderLogs()
        }
    }

    private fun renderLogs() {
        val content = SpannableStringBuilder(mLog)
        mUpdatingLogs.values.forEach { message -> content.appendLine(message) }
        mBinding.basicsResponse.text = content
        mBinding.basicsResponse.gravity = Gravity.TOP
    }
}
