package com.example.william.my.core.base.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * ViewBinding DialogFragment 基类
 *
 * 通过 [getViewBinding] 绑定视图，并在视图销毁时自动释放 binding 引用。
 */
abstract class BaseVBDialogFragment<VB : ViewBinding?>(
    windowAnimationsRes: Int = 0,
) : BaseDialogFragment(
    layout = 0,
    windowAnimationsRes = windowAnimationsRes,
) {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected abstract fun getViewBinding(): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = getViewBinding()
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
