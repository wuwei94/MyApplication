package com.example.william.my.basic.basic_shared.activity

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import com.chad.library.adapter4.BaseQuickAdapter
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutRecyclerResponseBinding
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonParser

/**
 * 通信/调度类示例 Activity 基类。
 *
 * 布局结构：
 * - 上方展示：TextView 内联日志/响应展示区（[mBinding.basicsResponse]）
 * - 下方列表：RecyclerView 操作列表（通过 [buildList] 与 [onRecyclerClick] 触发操作）
 *
 * 约定与规范：
 * 1. 禁止点击上方 TextView 触发操作，所有演示行为必须由下方列表项触发。
 * 2. 页面初始说明使用 [showDescription] 居中展示。
 * 3. 离散事件（开始、成功、失败、取消等）使用 [appendLog] 追加单行日志（不覆盖历史）。
 * 4. 高频进度或运行状态使用 [updateLog] 原位更新对应 key，避免频繁刷屏。
 * 5. 底部列表末尾自动附加“清空日志”选项，点击可清空展示区日志。
 */
abstract class BasicResponseActivity : BasicControlActivity() {

    protected lateinit var mBinding: SharedLayoutRecyclerResponseBinding

    private val mLog = SpannableStringBuilder()
    private val mUpdatingLogs = linkedMapOf<String, String>()

    override fun initViewBinding() {
        mBinding = SharedLayoutRecyclerResponseBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mRecycler = mBinding.basicsRecycler
    }

    override fun buildRecyclerList(): ArrayList<String> {
        val list = ArrayList(buildList())
        if (list.none { it.contains(ACTION_CLEAR_LOG) }) {
            list.add(ACTION_CLEAR_LOG)
        }
        return list
    }

    override fun onClick(adapter: BaseQuickAdapter<String, *>, view: View, position: Int) {
        val item = adapter.items.getOrNull(position)
        if (item == ACTION_CLEAR_LOG) {
            clearLog()
        } else {
            super.onClick(adapter, view, position)
        }
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
     * 追加单行日志到展示区（默认颜色）。
     */
    protected fun appendLog(message: String) {
        runOnUiThread {
            mLog.appendLine(message)
            renderLogs()
        }
    }

    /**
     * 追加 JSON 格式化单行日志到展示区。
     */
    protected fun appendFormatLog(prefix: String, message: String) {
        appendLog("$prefix${formatJson(message)}")
    }

    /**
     * 追加指定颜色的单行日志到展示区。
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
     * 原位更新指定 key 的运行状态；相同 key 的内容会被替换，不累加历史日志。
     */
    protected fun updateLog(key: String, message: String) {
        runOnUiThread {
            mUpdatingLogs[key] = message
            renderLogs()
        }
    }

    /**
     * 移除指定 key 的运行状态。
     */
    protected fun removeUpdatingLog(key: String) {
        runOnUiThread {
            mUpdatingLogs.remove(key)
            renderLogs()
        }
    }

    /**
     * 清空全部运行状态，保留历史日志。
     */
    protected fun clearUpdatingLogs() {
        runOnUiThread {
            mUpdatingLogs.clear()
            renderLogs()
        }
    }

    /**
     * 追加强调色 (accent) 的单行日志到展示区。
     */
    protected fun appendLogAccent(message: String) {
        appendLog(message, ContextCompat.getColor(this, R.color.shared_color_accent))
    }

    /**
     * 清空全部历史日志与运行状态。
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

    companion object {
        private const val ACTION_CLEAR_LOG = "清空日志"

        private val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

        /**
         * 格式化 JSON 对象或数组；非 JSON 以及解析失败的内容保持原样。
         */
        internal fun formatJson(value: String): String {
            val content = value.trim()
            if (!content.startsWith("{") && !content.startsWith("[")) {
                return value
            }
            return try {
                gson.toJson(JsonParser.parseString(content))
            } catch (_: JsonParseException) {
                value
            }
        }
    }
}
