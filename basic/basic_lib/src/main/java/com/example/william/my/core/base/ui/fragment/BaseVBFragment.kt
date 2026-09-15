package com.example.william.my.core.base.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * ViewBinding Fragment 基类
 *
 * 通过 [getViewBinding] 绑定视图，并在视图销毁时自动释放 binding 引用。
 */
abstract class BaseVBFragment<VB : ViewBinding?>(layout: Int = 0) : BaseFragment(layout) {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected abstract fun getViewBinding(): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = getViewBinding()
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
