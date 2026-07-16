package com.example.william.my.module.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.william.my.basic.basic_shared.R

/**
 * DialogFragment 方式1：重写 onCreateDialog()，返回一个 AlertDialog
 * 使用 setContentView 设置自定义布局，覆盖整个窗口
 */
class AlertDialogDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity()).create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val view = layoutInflater.inflate(
                R.layout.shared_layout_response,
                dialog.window?.decorView as? ViewGroup,
                false
            )
            view.findViewById<TextView>(R.id.basics_response)?.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.shared_color_primary)
            )
            dialog.setContentView(view)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.shared_dp_dialog_height)
            )
        }
    }
}
