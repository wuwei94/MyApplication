package com.example.william.my.module.ui.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.utils.DisplayUtils
import com.example.william.my.module.ui.dialog.AlertDialogDialogFragment
import com.example.william.my.module.ui.dialog.CustomViewDialogFragment
import java.util.Calendar

@Route(path = RouterPath.UI.Dialog)
class DialogActivity : BasicResponseActivity() {

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "普通对话框",
            "列表对话框",
            "单选对话框",
            "日期对话框",
            "时间对话框",
            "自定义对话框 setView",
            "自定义对话框 setContentView",
            "AlertDialogDialogFragment",
            "CustomViewDialogFragment",
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showResponse("点击下方列表项查看不同 Dialog 效果")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                //普通对话框：标题 + 内容 + 确定/取消按钮
                AlertDialog.Builder(this@DialogActivity)
                    .setIcon(R.drawable.shared_ic_launcher)
                    .setTitle("标题")
                    .setMessage("内容")
                    .setPositiveButton("确定") { _, _ ->
                        appendLog("【普通对话框】点击了确定")
                    }
                    .setNegativeButton("取消") { _, _ ->
                        appendLog("【普通对话框】点击了取消")
                    }
                    .create()
                    .show()
            }

            1 -> {
                //列表对话框：点击列表项
                val items = arrayOf("item1", "item2")
                AlertDialog.Builder(this@DialogActivity)
                    .setIcon(R.drawable.shared_ic_launcher)
                    .setTitle("标题")
                    .setItems(items) { _, which ->
                        appendLog("【列表对话框】选择了：${items[which]}")
                    }
                    .show()
            }

            2 -> {
                //单选对话框：单选列表
                val items = arrayOf("item1", "item2")
                AlertDialog.Builder(this@DialogActivity)
                    .setSingleChoiceItems(items, 0) { _, which ->
                        appendLog("【单选对话框】选择了：${items[which]}")
                    }
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定") { _, _ -> }
                    .show()
            }

            3 -> {
                //日期对话框：选择日期
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    this@DialogActivity,
                    { _, year, month, dayOfMonth ->
                        appendLog("【日期对话框】选择了：${year}-${month + 1}-${dayOfMonth}")
                    },
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH],
                    calendar[Calendar.DAY_OF_MONTH]
                ).show()
            }

            4 -> {
                //时间对话框：选择时间
                val calendar = Calendar.getInstance()
                TimePickerDialog(
                    this@DialogActivity,
                    { _, hourOfDay, minute ->
                        appendLog("【时间对话框】选择了：${hourOfDay}:${minute}")
                    },
                    calendar[Calendar.HOUR_OF_DAY],
                    calendar[Calendar.MINUTE],
                    true
                ).show()
            }

            5 -> {
                //自定义对话框：setView 在 show() 之前使用
                val view = layoutInflater.inflate(
                    R.layout.shared_layout_response,
                    window.decorView as ViewGroup,
                    false
                )
                //布局中 TextView 高度为 0dp（match_constraint），dialog 中约束不生效，需要手动设置高度
                view.findViewById<TextView>(R.id.basics_response).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        resources.getDimensionPixelSize(R.dimen.shared_dp_dialog_height)
                    )
                    setBackgroundColor(
                        ContextCompat.getColor(
                            this@DialogActivity,
                            R.color.shared_color_primary
                        )
                    )
                }
                val dialog = AlertDialog.Builder(this@DialogActivity)
                    .setView(view)
                    .create()
                dialog.show()
                val params = dialog.window!!.attributes
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = resources.getDimensionPixelSize(R.dimen.shared_dp_dialog_height)
                dialog.window!!.attributes = params
                appendLog("【自定义对话框】setView")
            }

            6 -> {
                //自定义对话框：setContentView 在 show() 之后使用
                /*
                 * setContentView 为 Dialog 的方法，对应整个对话框窗口的 view
                 * setView 是 AlertDialog 的方法，对应的是 CustomView 的部分而不是整个窗体
                 */
                val view = layoutInflater.inflate(
                    R.layout.shared_layout_response,
                    window.decorView as ViewGroup,
                    false
                )
                //布局中 TextView 高度为 0dp（match_constraint），dialog 中约束不生效，需要手动设置高度
                view.findViewById<TextView>(R.id.basics_response).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        resources.getDimensionPixelSize(R.dimen.shared_dp_dialog_height)
                    )
                    setBackgroundColor(
                        ContextCompat.getColor(
                            this@DialogActivity,
                            R.color.shared_color_primary
                        )
                    )
                }
                val dialog = AlertDialog.Builder(this@DialogActivity)
                    .create()
                dialog.show()
                dialog.setContentView(view)
                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                appendLog("【自定义对话框】setContentView")
            }

            7 -> {
                //AlertDialogDialogFragment
                val dialogFragment = AlertDialogDialogFragment()
                dialogFragment.show(supportFragmentManager, dialogFragment.tag)
                appendLog("【AlertDialogDialogFragment】重写 onCreateDialog")
            }

            8 -> {
                //CustomViewDialogFragment
                val dialogFragment = CustomViewDialogFragment()
                dialogFragment.show(supportFragmentManager, dialogFragment.tag)
                appendLog("【CustomViewDialogFragment】重写 onCreateView")
            }

            else -> {}
        }
    }
}
