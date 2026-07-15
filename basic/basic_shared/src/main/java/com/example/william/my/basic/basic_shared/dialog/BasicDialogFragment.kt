package com.example.william.my.basic.basic_shared.dialog

import com.example.william.my.basic.basic_shared.databinding.SharedLayoutDialogBinding
import com.example.william.my.core.base.R
import com.example.william.my.core.base.dialog.BaseVBDialogFragment

class BasicDialogFragment :
    BaseVBDialogFragment<SharedLayoutDialogBinding>(R.style.base_Dialog_Translate_Slide_Alpha) {

    override fun getViewBinding(): SharedLayoutDialogBinding {
        return SharedLayoutDialogBinding.inflate(layoutInflater)
    }

    fun showMessage(message: String?) {
        mBinding.dialog.basicsResponse.text = message
    }
}