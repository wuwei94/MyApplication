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
 * AlertDialog — 自定义对话框
 *
 * 自定义对话框组件，支持多种样式和交互方式。
 *
 * 核心组件：
 * 1. IosAlertDialog：iOS 风格的对话框，支持标题、内容、左右按钮
 * 2. IosAlertItemDialog：iOS 风格的列表对话框，支持多个选项
 * 3. MyBottomSheetDialog：底部弹出对话框，支持 ViewPager
 *
 * 核心特性：
 * 1. 自定义样式：支持自定义标题、内容、按钮样式
 * 2. 多种交互：支持点击、选择等多种交互方式
 * 3. 动画效果：支持弹出、关闭动画
 * 4. 灵活配置：支持取消、点击外部关闭等配置
 *
 * 基本用法：
 * ```kotlin
 * // IosAlertDialog
 * IosAlertDialog(context)
 *     .builder()
 *     .setTitle("标题")
 *     .setMsg("内容")
 *     .setLeftButton("取消") { /* 处理点击 */ }
 *     .setRightButton("确定") { /* 处理点击 */ }
 *     .show()
 *
 * // IosAlertItemDialog
 * IosAlertItemDialog(context)
 *     .builder()
 *     .setTitle("标题")
 *     .addAlertItem("选项1") { /* 处理选择 */ }
 *     .addAlertItem("选项2") { /* 处理选择 */ }
 *     .show()
 * ```
 *
 * 适用场景：
 * - 确认对话框、提示对话框
 * - 列表选择对话框
 * - 底部弹出对话框
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