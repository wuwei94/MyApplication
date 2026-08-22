package com.example.william.my.module.widget.dialog

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.core.base.dialog.BaseDialogFragment

/**
 * DialogFragment 方式2：使用布局文件，通过 onCreateView() 返回自定义 View
 */
class CustomViewDialogFragment : BaseDialogFragment(R.layout.shared_layout_response) {

    override fun setAttributes(params: WindowManager.LayoutParams) {
        super.setAttributes(params)
        params.width = resources.getDimensionPixelSize(R.dimen.shared_dp_dialog_width)
        params.height = resources.getDimensionPixelSize(R.dimen.shared_dp_dialog_height)
    }

    override fun initView(view: View?, state: Bundle?) {
        super.initView(view, state)
        view?.findViewById<TextView>(R.id.basics_response)?.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.shared_color_primary)
        )
    }
}
