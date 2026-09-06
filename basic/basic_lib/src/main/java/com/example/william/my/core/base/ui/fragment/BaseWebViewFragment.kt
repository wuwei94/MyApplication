package com.example.william.my.core.base.ui.fragment

import com.example.william.my.core.base.R
import com.example.william.my.core.base.databinding.BaseFragmentWebViewBinding

/**
 * WebView Fragment 基类（绑定通用的 WebView 布局）
 */
class BaseWebViewFragment : BaseVBFragment<BaseFragmentWebViewBinding>(R.layout.base_fragment_web_view) {
    override fun getViewBinding(): BaseFragmentWebViewBinding = BaseFragmentWebViewBinding.inflate(layoutInflater)
}
