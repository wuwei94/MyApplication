package com.example.william.my.core.base.ui.activity

import android.os.Bundle
import androidx.viewbinding.ViewBinding

/**
 * ViewBinding Activity 基类
 *
 * 通过 [getViewBinding] 绑定视图，并在销毁时自动释放 mBinding 引用。
 */
abstract class BaseVBActivity<VB : ViewBinding?> : BaseActivity() {

    private var _binding: VB? = null
    protected val mBinding get() = _binding!!

    protected abstract fun getViewBinding(): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initViewBinding() {
        super.initViewBinding()
        _binding = getViewBinding()
        setContentView(mBinding.root)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
