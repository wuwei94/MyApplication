package com.example.william.my.module.widget.activity

import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.widget.alertdialog.IosAlertDialog
import com.example.william.my.core.widget.alertdialog.IosAlertItemDialog
import com.example.william.my.module.widget.dialog.MyBottomSheetDialog

/**
 * 自定义 AlertDialog 示例
 */
@Route(path = RouterPath.Widget.AlertDialog)
class AlertDialogActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项展示不同样式的 Dialog")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "IosAlertDialog",
            "IosAlertItemDialog",
            "ViewPagerBottomSheetDialog"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> {
                IosAlertDialog(this@AlertDialogActivity).builder()
                    .setTitle("标题")
                    .setMsg(Gravity.CENTER, "内容")
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setLeftButton("左", View.OnClickListener {
                        appendLog("点击了 IosAlertDialog 左按钮")
                    })
                    .setRightButton("右", View.OnClickListener {
                        appendLog("点击了 IosAlertDialog 右按钮")
                    })
                    .show()
                appendLog("展示 IosAlertDialog")
            }

            1 -> {
                IosAlertItemDialog(this@AlertDialogActivity).builder()
                    .setTitle("标题")
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .addAlertItem("ITEM 1") { which ->
                        appendLog("点击了 IosAlertItemDialog: ITEM 1 (which: $which)")
                    }
                    .addAlertItem("ITEM 2") { which ->
                        appendLog("点击了 IosAlertItemDialog: ITEM 2 (which: $which)")
                    }
                    .addAlertItem(
                        "ITEM 3",
                        com.example.william.my.basic.basic_shared.R.color.shared_color_primary
                    ) { which ->
                        appendLog("点击了 IosAlertItemDialog: ITEM 3 (which: $which)")
                    }
                    .show()
                appendLog("展示 IosAlertItemDialog")
            }

            2 -> {
                val dialogFragment3 = MyBottomSheetDialog()
                dialogFragment3.show(supportFragmentManager, dialogFragment3.tag)
                appendLog("展示 ViewPagerBottomSheetDialog")
            }
        }
    }
}