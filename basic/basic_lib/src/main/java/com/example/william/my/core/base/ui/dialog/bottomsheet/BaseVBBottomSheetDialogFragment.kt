package com.example.william.my.core.base.ui.dialog.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * ViewBinding 底部弹窗（BottomSheet）基类
 *
 * 通过 [getViewBinding] 绑定视图，并在视图销毁时自动释放 mBinding 引用。
 */
abstract class BaseVBBottomSheetDialogFragment<VB : ViewBinding?>(
    windowAnimationsRes: Int = 0,
) : BaseBottomSheetDialogFragment(
    layout = 0,
    windowAnimationsRes = windowAnimationsRes,
) {

    private var _binding: VB? = null
    protected val mBinding get() = _binding!!

    protected abstract fun getViewBinding(): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = getViewBinding()
        return mBinding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
