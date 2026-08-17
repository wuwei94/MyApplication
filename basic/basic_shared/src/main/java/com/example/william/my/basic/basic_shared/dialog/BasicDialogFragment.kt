package com.example.william.my.basic.basic_shared.dialog

import android.os.Bundle
import android.view.View
import com.example.william.my.basic.basic_shared.databinding.SharedLayoutDialogBinding
import com.example.william.my.core.base.R
import com.example.william.my.core.base.dialog.BaseVBDialogFragment

class BasicDialogFragment :
    BaseVBDialogFragment<SharedLayoutDialogBinding>(R.style.base_Dialog_Translate_Slide_Alpha) {

    override fun getViewBinding(): SharedLayoutDialogBinding {
        return SharedLayoutDialogBinding.inflate(layoutInflater)
    }

    override fun initView(view: View?, state: Bundle?) {
        super.initView(view, state)
        val message = arguments?.getString(KEY_MESSAGE)
        if (!message.isNullOrEmpty()) {
            mBinding.dialog.basicsResponse.text = message
        }
    }

    fun showMessage(message: String?) {
        if (isAdded && view != null) {
            mBinding.dialog.basicsResponse.text = message
        } else {
            arguments = (arguments ?: Bundle()).apply {
                putString(KEY_MESSAGE, message)
            }
        }
    }

    companion object {
        private const val KEY_MESSAGE = "dialog_message"

        fun newInstance(message: String): BasicDialogFragment {
            return BasicDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_MESSAGE, message)
                }
            }
        }
    }
}